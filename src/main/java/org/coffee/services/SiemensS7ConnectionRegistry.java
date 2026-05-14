package org.coffee.services;

import jakarta.annotation.PreDestroy;
import org.apache.plc4x.java.DefaultPlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.coffee.domain.models.database.DeviceTable;
import org.coffee.domain.models.database.PlcSiemensS7ConfigTable;
import org.coffee.domain.models.database.PlcTable;
import org.coffee.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry singleton que mantem conexoes Siemens S7 persistentes,
 * indexadas pelo ID do dispositivo.
 *
 * Por que existir: o handshake S7 (TCP + ISO-on-TCP + COTP + S7 setup)
 * custa ~100ms por conexao. A
 *
 * Modelo de uso explicito: o cliente deve chamar openConnection(deviceId)
 * antes de qualquer leitura/escrita. Tentativas de operar sem conexao
 * aberta resultam em IllegalStateException (que o controller mapeia
 * para HTTP 409). Esta foi uma decisao de design: ter ciclo de vida
 * de conexao explicito facilita debug e da controle ao consumidor.
 *
 * Thread-safety: o mapa de conexoes e sincronizado. As proprias
 * PlcConnection da PLC4X sao thread-safe para leituras concorrentes.
 *
 * Driver: usa o "s7-light" da PLC4X (mesma dependencia Maven plc4j-driver-s7,
 * apenas schema diferente na connection string). O driver padrao "s7://"
 * apresenta um bug conhecido sob carga concorrente (NullPointerException
 * em ChannelOutboundBuffer.addFlush por entry.promise null - issue #2394
 * do apache/plc4x), causado por race condition no RequestTransactionManager
 * interno. O s7-light foi criado pelo proprio mantenedor da PLC4X
 * especificamente para contornar esse problema em S7-1200/1500, removendo
 * suporte a subscriptions (que nao usamos - nosso modelo e polling via
 * request/response).
 */
@Service
public class SiemensS7ConnectionRegistry {

    // Mapa deviceId -> conexao PLC4X ativa
    // synchronized em todas as operacoes que tocam o mapa porque a fronteira
    // entre "ja existe?" e "criar" precisa ser atomica.
    private final Map<Long, PlcConnection> connections = new HashMap<>();
    private final Object lock = new Object();

    private final DeviceRepository deviceRepository;

    @Autowired
    public SiemensS7ConnectionRegistry(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * Abre conexao S7 para o device informado. Idempotente: se ja existe
     * conexao ativa, retorna sem fazer nada.
     *
     * Le do banco: device -> plc -> siemensS7Config para montar a connection
     * string PLC4X.
     *
     * @return ConnectionResult com flag indicando se conexao ja estava aberta
     *         (true) ou se foi criada agora (false). Util para log/UX.
     */
    public ConnectionResult openConnection(Long deviceId) {
        synchronized (lock) {
            // Idempotencia: se ja existe e esta saudavel, nao reabre.
            PlcConnection existing = connections.get(deviceId);
            if (existing != null && existing.isConnected()) {
                return new ConnectionResult(true, existing);
            }

            // Existia mas morreu (stale) - limpa antes de reabrir
            if (existing != null) {
                closeQuietly(existing);
                connections.remove(deviceId);
            }

            // Le dados do banco para montar a connection string
            DeviceTable device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Device " + deviceId + " nao encontrado"));

            PlcTable plc = device.getPlc();
            if (plc == null) {
                throw new IllegalArgumentException(
                        "Device " + deviceId + " nao esta vinculado a nenhum PLC");
            }

            PlcSiemensS7ConfigTable s7Config = plc.getSiemensS7Config();
            if (s7Config == null) {
                throw new IllegalArgumentException(
                        "PLC '" + plc.getName() + "' (id=" + plc.getId() + ") nao "
                                + "possui configuracao Siemens S7. Cadastre rack e slot via "
                                + "POST /api/plc/" + plc.getId() + "/siemens-s7-config antes "
                                + "de abrir a conexao.");
            }

            // controller-type hardcoded em S7_1200 (unico modelo no kit atual).
            // TODO quando houver suporte a outros modelos, mover para
            // PlcSiemensS7ConfigTable junto com rack/slot. Valores aceitos pela
            // PLC4X: S7_300, S7_400, S7_1200, S7_1500, LOGO.
            String connectionString = String.format(
                    "s7-light://%s?remote-rack=%d&remote-slot=%d&controller-type=S7_1200",
                    plc.getIp(), s7Config.getRack(), s7Config.getSlot());

            try {
                PlcConnection connection = new DefaultPlcDriverManager()
                        .getConnectionManager()
                        .getConnection(connectionString);

                if (!connection.getMetadata().isReadSupported()) {
                    closeQuietly(connection);
                    throw new RuntimeException(
                            "Conexao S7 estabelecida mas nao suporta leitura");
                }

                connections.put(deviceId, connection);
                System.out.println("S7 connection opened for device " + deviceId
                        + " at " + plc.getIp());
                return new ConnectionResult(false, connection);

            } catch (Exception e) {
                throw new RuntimeException(
                        "Falha ao conectar ao CLP S7 do device " + deviceId
                                + ": " + e.getMessage(), e);
            }
        }
    }

    /**
     * Fecha a conexao do device. Idempotente: se nao existe conexao, nao faz nada.
     *
     * @return true se uma conexao foi efetivamente fechada, false se nao havia
     *         conexao aberta
     */
    public boolean closeConnection(Long deviceId) {
        synchronized (lock) {
            PlcConnection connection = connections.remove(deviceId);
            if (connection == null) {
                return false;
            }
            closeQuietly(connection);
            System.out.println("S7 connection closed for device " + deviceId);
            return true;
        }
    }

    /**
     * Retorna a conexao ativa para o device. Lanca IllegalStateException
     * se nao houver conexao aberta - chamadores devem garantir openConnection
     * antes de operar.
     *
     * Tambem detecta e descarta conexoes "stale": se a conexao no mapa
     * existe mas isConnected() retorna false (PLC reiniciou, rede caiu, etc.),
     * removemos do mapa e lancamos como se nao houvesse conexao - o cliente
     * deve chamar /connect novamente para reabrir.
     */
    public PlcConnection getActiveConnection(Long deviceId) {
        synchronized (lock) {
            PlcConnection connection = connections.get(deviceId);
            if (connection == null) {
                throw new IllegalStateException(
                        "Conexao S7 nao esta aberta para o device " + deviceId
                                + ". Abra a conexao primeiro com POST /api/operations/"
                                + "siemens-s7/connect/device/" + deviceId);
            }
            if (!connection.isConnected()) {
                // Conexao morta - limpa do mapa para forcar reconexao explicita
                closeQuietly(connection);
                connections.remove(deviceId);
                throw new IllegalStateException(
                        "Conexao S7 para o device " + deviceId + " caiu (provavelmente "
                                + "o CLP reiniciou ou houve perda de rede). Reabra com "
                                + "POST /api/operations/siemens-s7/connect/device/" + deviceId);
            }
            return connection;
        }
    }

    /**
     * Verifica status sem lancar excecao. Util para o endpoint /status.
     */
    public ConnectionStatus getStatus(Long deviceId) {
        synchronized (lock) {
            PlcConnection connection = connections.get(deviceId);
            if (connection == null) {
                return ConnectionStatus.CLOSED;
            }
            return connection.isConnected() ? ConnectionStatus.OPEN : ConnectionStatus.STALE;
        }
    }

    /**
     * Fecha TODAS as conexoes ativas. Chamado no shutdown da aplicacao
     * (@PreDestroy) para evitar conexoes orfas no lado do CLP, especialmente
     * importante com Spring DevTools (restarts frequentes).
     */
    @PreDestroy
    public void closeAll() {
        synchronized (lock) {
            if (connections.isEmpty()) {
                return;
            }
            System.out.println("Closing " + connections.size() + " S7 connection(s) "
                    + "before shutdown...");
            for (Map.Entry<Long, PlcConnection> entry : connections.entrySet()) {
                closeQuietly(entry.getValue());
            }
            connections.clear();
        }
    }

    private void closeQuietly(PlcConnection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                System.err.println("Erro ao fechar conexao S7 (ignorado): " + e.getMessage());
            }
        }
    }

    // Tipos auxiliares

    /** Resultado de uma operacao de abertura - distingue "abriu agora" de "ja estava aberta". */
    public static class ConnectionResult {
        private final boolean alreadyOpen;
        private final PlcConnection connection;

        public ConnectionResult(boolean alreadyOpen, PlcConnection connection) {
            this.alreadyOpen = alreadyOpen;
            this.connection = connection;
        }

        public boolean isAlreadyOpen() {
            return alreadyOpen;
        }

        public PlcConnection getConnection() {
            return connection;
        }
    }

    public enum ConnectionStatus {
        OPEN,    // conexao aberta e saudavel
        STALE,   // existe no mapa mas isConnected()=false (sera descartada na proxima operacao)
        CLOSED   // nao existe conexao para este device
    }
}

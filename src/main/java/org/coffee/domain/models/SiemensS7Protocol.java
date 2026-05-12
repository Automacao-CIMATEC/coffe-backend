package org.coffee.domain.models;

import org.apache.plc4x.java.DefaultPlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.coffee.domain.abstracts.AbstractProtocol;

import java.util.concurrent.TimeUnit;

/**
 * Implementacao do protocolo Siemens S7 usando Apache PLC4X.
 *
 * Substitui o microservico Python que era consumido via HTTP pelo
 * SiemensS7Controller original. Mantem o mesmo padrao arquitetural
 * de ModbusProtocol: estende AbstractProtocol e encapsula uma
 * conexao com o CLP via lib externa.
 *
 * Por que nao usa as assinaturas legadas readDataInt(int address) /
 * writeData(int address, ...) de AbstractProtocol: o protocolo S7
 * enderece variaveis por (DB, byteOffset, bitOffset), nao por um
 * inteiro unico como Modbus. Os metodos da superclasse foram mantidos
 * como no-op para nao quebrar o contrato, e a leitura/escrita real
 * acontece pelos metodos publicos especificos abaixo.
 *
 * Formato da connection string PLC4X:
 *   s7://{ip}?remote-rack={rack}&remote-slot={slot}
 *
 * Formato dos enderecos de tag (sintaxe PLC4X / S7):
 *   BOOL:   %DB{db}.DBX{byte}.{bit}:BOOL
 *   BYTE:   %DB{db}.DBB{byte}:BYTE
 *   WORD:   %DB{db}.DBW{byte}:WORD       (16 bits unsigned)
 *   DWORD:  %DB{db}.DBD{byte}:DWORD      (32 bits unsigned)
 *   INT:    %DB{db}.DBW{byte}:INT        (16 bits signed)
 *   DINT:   %DB{db}.DBD{byte}:DINT       (32 bits signed)
 *   STRING: %DB{db}.DBB{byte}:STRING
 */
public class SiemensS7Protocol extends AbstractProtocol {

    // Porta padrao S7 - reservada pelo protocolo ISO-on-TCP, fixa.
    // Nao deve ser exposta como parametro, S7 sempre usa 102.
    public static final int S7_DEFAULT_PORT = 102;

    // Timeout para operacoes de leitura/escrita - 10 segundos cobre
    // bem cenarios de rede lenta sem travar o controller por tempo demais.
    private static final long REQUEST_TIMEOUT_SECONDS = 10;

    private final int rack;
    private final int slot;

    private PlcConnection connection;
    private boolean isConnected = false;

    public SiemensS7Protocol(String ip, int rack, int slot) {
        super(ip, S7_DEFAULT_PORT);
        this.rack = rack;
        this.slot = slot;
    }

    @Override
    public void openConnection() {
        try {
            String connectionString = String.format(
                    "s7://%s?remote-rack=%d&remote-slot=%d",
                    getIp(), rack, slot
            );

            connection = new DefaultPlcDriverManager()
                    .getConnectionManager()
                    .getConnection(connectionString);

            if (!connection.getMetadata().isReadSupported()) {
                throw new RuntimeException("Conexao S7 estabelecida mas nao suporta leitura");
            }

            isConnected = true;
            System.out.println("Connected to S7 PLC at " + getIp()
                    + " (rack=" + rack + ", slot=" + slot + ")");
        } catch (Exception e) {
            System.err.println("S7 connection error: " + e.getMessage());
            isConnected = false;
            // Re-lanca para o controller decidir o status HTTP de erro
            throw new RuntimeException("Falha ao conectar ao CLP S7: " + e.getMessage(), e);
        }
    }

    @Override
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("S7 connection closed");
            } catch (Exception e) {
                System.err.println("Erro ao fechar conexao S7: " + e.getMessage());
            } finally {
                isConnected = false;
                connection = null;
            }
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected && connection != null && connection.isConnected();
    }

    // METODOS ESPECIFICOS S7 - leitura por (db, byteOffset, bitOffset)

    /**
     * Le um valor BOOL de uma DB.
     *
     * @param dbNumber  numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @param bitOffset bit dentro do byte (0 a 7)
     * @return valor lido como boolean
     */
    public boolean readBool(int dbNumber, int byteOffset, int bitOffset) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBX%d.%d:BOOL",
                dbNumber, byteOffset, bitOffset);
        return executeRead("value", tagAddress).getBoolean("value");
    }

    /**
     * Le um valor INT (16 bits signed) de uma DB.
     * @param dbNumber  numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como Int
     */
    public int readInt(int dbNumber, int byteOffset) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBW%d:INT", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getInteger("value");
    }

    /**
     * Le um valor DINT (32 bits signed) de uma DB.
     * @param dbNumber  numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como DInt
     */
    public int readDInt(int dbNumber, int byteOffset) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBD%d:DINT", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getInteger("value");
    }

    /**
     * Le um valor REAL (float 32 bits) de uma DB.
     * @param dbNumber  numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como Real
     */
    public float readReal(int dbNumber, int byteOffset) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBD%d:REAL", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getFloat("value");
    }

    /**
     * Le um valor STRING de uma DB.
     * O driver S7 le ate 254 caracteres por padrao.
     * @param dbNumber  numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como String
     */
    public String readString(int dbNumber, int byteOffset) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBB%d:STRING", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getString("value");
    }

    /**
     * Le um BYTE (unsigned 8 bits) de uma DB.
     * Retornado como short para evitar problemas de sinal.
     * @param dbNumber  numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como Short
     */
    public short readByte(int dbNumber, int byteOffset) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBB%d:BYTE", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getShort("value");
    }

    /**
     * Le uma WORD (unsigned 16 bits) de uma DB.
     * Retornado como int para evitar problemas de sinal.
     * @param dbNumber  numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como inteiro
     */
    public int readWord(int dbNumber, int byteOffset) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBW%d:WORD", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getInteger("value");
    }

    /**
     * Le uma DWORD (unsigned 32 bits) de uma DB.
     * Retornado como long para evitar problemas de sinal.
     * @param dbNumber  numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como Long
     */
    public long readDWord(int dbNumber, int byteOffset) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBD%d:DWORD", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getLong("value");
    }

    // METODOS ESPECIFICOS S7 - escrita por (db, byteOffset, bitOffset)

    public void writeBool(int dbNumber, int byteOffset, int bitOffset, boolean value) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBX%d.%d:BOOL",
                dbNumber, byteOffset, bitOffset);
        executeWrite(tagAddress, value);
    }

    public void writeInt(int dbNumber, int byteOffset, int value) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBW%d:INT", dbNumber, byteOffset);
        executeWrite(tagAddress, value);
    }

    public void writeDInt(int dbNumber, int byteOffset, int value) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBD%d:DINT", dbNumber, byteOffset);
        executeWrite(tagAddress, value);
    }

    public void writeReal(int dbNumber, int byteOffset, float value) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBD%d:REAL", dbNumber, byteOffset);
        executeWrite(tagAddress, value);
    }

    public void writeString(int dbNumber, int byteOffset, String value) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBB%d:STRING", dbNumber, byteOffset);
        executeWrite(tagAddress, value);
    }

    public void writeByte(int dbNumber, int byteOffset, byte value) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBB%d:BYTE", dbNumber, byteOffset);
        executeWrite(tagAddress, value);
    }

    public void writeWord(int dbNumber, int byteOffset, int value) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBW%d:WORD", dbNumber, byteOffset);
        executeWrite(tagAddress, value);
    }

    public void writeDWord(int dbNumber, int byteOffset, long value) {
        validateConnected();
        String tagAddress = String.format("%%DB%d.DBD%d:DWORD", dbNumber, byteOffset);
        executeWrite(tagAddress, value);
    }

    // HELPERS INTERNOS

    private PlcReadResponse executeRead(String tagName, String tagAddress) {
        try {
            PlcReadRequest request = connection.readRequestBuilder()
                    .addTagAddress(tagName, tagAddress)
                    .build();

            PlcReadResponse response = request.execute()
                    .get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            PlcResponseCode code = response.getResponseCode(tagName);
            if (code != PlcResponseCode.OK) {
                throw new RuntimeException("Leitura S7 falhou com codigo: " + code
                        + " (tag: " + tagAddress + ")");
            }

            return response;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro na leitura S7 (tag: " + tagAddress
                    + "): " + e.getMessage(), e);
        }
    }

    private void executeWrite(String tagAddress, Object value) {
        String tagName = "value";
        try {
            PlcWriteRequest.Builder builder = connection.writeRequestBuilder();
            builder.addTagAddress(tagName, tagAddress, value);
            PlcWriteRequest request = builder.build();

            PlcWriteResponse response = request.execute()
                    .get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            PlcResponseCode code = response.getResponseCode(tagName);
            if (code != PlcResponseCode.OK) {
                throw new RuntimeException("Escrita S7 falhou com codigo: " + code
                        + " (tag: " + tagAddress + ")");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro na escrita S7 (tag: " + tagAddress
                    + "): " + e.getMessage(), e);
        }
    }

    private void validateConnected() {
        if (!isConnected()) {
            throw new IllegalStateException(
                    "Conexao S7 nao esta aberta. Chame openConnection() antes.");
        }
    }

    public int getRack() {
        return rack;
    }

    public int getSlot() {
        return slot;
    }

    // METODOS DE AbstractProtocol - mantidos por compatibilidade de contrato
    //
    // S7 enderece por (db, byteOffset, bitOffset), nao por um int unico,
    // entao essas sobrecargas legadas nao tem como funcionar diretamente.
    // Lanca UnsupportedOperationException de forma explicita para que
    // chamadores acidentais saibam que devem usar os metodos S7-especificos.

    private static final String LEGACY_API_MSG =
            "S7 utiliza enderecamento por (DB, byteOffset, bitOffset). "
            + "Use readBool/readInt/readReal/etc. em vez dos metodos genericos.";

    @Override
    public int readDataInt(int parameter) {
        throw new UnsupportedOperationException(LEGACY_API_MSG);
    }

    @Override
    public float readDataFloat(int parameter) {
        throw new UnsupportedOperationException(LEGACY_API_MSG);
    }

    @Override
    public boolean readDataBoolean(int param) {
        throw new UnsupportedOperationException(LEGACY_API_MSG);
    }

    @Override
    public String readDataString() {
        throw new UnsupportedOperationException(LEGACY_API_MSG);
    }

    @Override
    public String readDataString(int startingAddress, int registerCount) {
        throw new UnsupportedOperationException(LEGACY_API_MSG);
    }

    @Override
    public void writeData(int data) {
        throw new UnsupportedOperationException(LEGACY_API_MSG);
    }

    @Override
    public void writeData(float data) {
        throw new UnsupportedOperationException(LEGACY_API_MSG);
    }

    @Override
    public void writeData(boolean data) {
        throw new UnsupportedOperationException(LEGACY_API_MSG);
    }

    @Override
    public void writeData(String data) {
        throw new UnsupportedOperationException(LEGACY_API_MSG);
    }

    @Override
    public void writeData(int address, int data) {
        throw new UnsupportedOperationException(LEGACY_API_MSG);
    }

    @Override
    public void writeData(int address, float data) {
        throw new UnsupportedOperationException(LEGACY_API_MSG);
    }

    @Override
    public void writeData(int address, boolean data) {
        throw new UnsupportedOperationException(LEGACY_API_MSG);
    }

    @Override
    public void writeData(int startingAddress, String data) {
        throw new UnsupportedOperationException(LEGACY_API_MSG);
    }
}

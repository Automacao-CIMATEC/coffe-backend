package org.coffee.domain.models;

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
 * MUDANCA DE DESIGN (v2): Esta classe nao gerencia mais o lifecycle
 * da conexao. Ela e um wrapper "burro" de operacoes sobre uma
 * PlcConnection ja aberta, recebida no construtor.
 *
 * O gerenciamento de conexao foi movido para SiemensS7ConnectionRegistry,
 * que mantem conexoes persistentes indexadas por deviceId.
 *
 * Os metodos openConnection/closeConnection sao mantidos como no-op
 * porque AbstractProtocol exige - mas o lifecycle real esta no Registry.
 */
public class SiemensS7Protocol extends AbstractProtocol {

    // S7 sempre na porta 102 (ISO-on-TCP) - parametro herdado de AbstractProtocol.
    public static final int S7_DEFAULT_PORT = 102;

    private static final long REQUEST_TIMEOUT_SECONDS = 10;

    private final PlcConnection connection;
    private final int rack;
    private final int slot;

    /**
     * @param ip         IP do CLP (informativo - a conexao ja esta aberta)
     * @param rack       rack do CPU (informativo)
     * @param slot       slot do CPU (informativo)
     * @param connection conexao ativa fornecida pelo SiemensS7ConnectionRegistry
     */
    public SiemensS7Protocol(String ip, int rack, int slot, PlcConnection connection) {
        super(ip, S7_DEFAULT_PORT);
        this.rack = rack;
        this.slot = slot;
        this.connection = connection;
    }

    /**
     * No-op: o lifecycle de conexao agora e responsabilidade do
     * SiemensS7ConnectionRegistry. Mantido por compatibilidade com
     * o contrato de AbstractProtocol.
     */
    @Override
    public void openConnection() {
        // intencional: conexao ja vem aberta do Registry
    }

    /**
     * No-op pelo mesmo motivo de openConnection. Para realmente fechar
     * a conexao, use SiemensS7ConnectionRegistry.closeConnection(deviceId).
     */
    @Override
    public void closeConnection() {
        // intencional: lifecycle gerenciado pelo Registry
    }

    @Override
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    // METODOS ESPECIFICOS S7 - leitura por (db, byteOffset, bitOffset)

    /**
     * Le um valor BOOL de uma DB.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @param bitOffset  bit dentro do byte (0 a 7)
     * @return valor lido como boolean
     */
    public boolean readBool(int dbNumber, int byteOffset, int bitOffset) {
        String tagAddress = String.format("%%DB%d.DBX%d.%d:BOOL",
                dbNumber, byteOffset, bitOffset);
        return executeRead("value", tagAddress).getBoolean("value");
    }

    /**
     * Le um valor INT (16 bits signed) de uma DB.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como int
     */
    public int readInt(int dbNumber, int byteOffset) {
        String tagAddress = String.format("%%DB%d.DBW%d:INT", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getInteger("value");
    }

    /**
     * Le um valor DINT (32 bits signed) de uma DB.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como int
     */
    public int readDInt(int dbNumber, int byteOffset) {
        String tagAddress = String.format("%%DB%d.DBD%d:DINT", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getInteger("value");
    }

    /**
     * Le um valor REAL (float 32 bits) de uma DB.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como float
     */
    public float readReal(int dbNumber, int byteOffset) {
        String tagAddress = String.format("%%DB%d.DBD%d:REAL", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getFloat("value");
    }

    /**
     * Le um valor STRING de uma DB.
     * O driver S7 le ate 254 caracteres por padrao.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como String
     */
    public String readString(int dbNumber, int byteOffset) {
        String tagAddress = String.format("%%DB%d.DBB%d:STRING", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getString("value");
    }

    /**
     * Le um BYTE (unsigned 8 bits) de uma DB.
     * Retornado como short para evitar problemas de sinal em Java.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como short (0 a 255)
     */
    public short readByte(int dbNumber, int byteOffset) {
        String tagAddress = String.format("%%DB%d.DBB%d:BYTE", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getShort("value");
    }

    /**
     * Le uma WORD (unsigned 16 bits) de uma DB.
     * Retornado como int para evitar problemas de sinal em Java.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como int (0 a 65535)
     */
    public int readWord(int dbNumber, int byteOffset) {
        String tagAddress = String.format("%%DB%d.DBW%d:WORD", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getInteger("value");
    }

    /**
     * Le uma DWORD (unsigned 32 bits) de uma DB.
     * Retornado como long para evitar problemas de sinal em Java.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @return valor lido como long (0 a 4294967295)
     */
    public long readDWord(int dbNumber, int byteOffset) {
        String tagAddress = String.format("%%DB%d.DBD%d:DWORD", dbNumber, byteOffset);
        return executeRead("value", tagAddress).getLong("value");
    }

    // METODOS ESPECIFICOS S7 - escrita por (db, byteOffset, bitOffset)

    /**
     * Escreve um valor BOOL em uma DB.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @param bitOffset  bit dentro do byte (0 a 7)
     * @param value      valor a ser escrito
     */
    public void writeBool(int dbNumber, int byteOffset, int bitOffset, boolean value) {
        String tagAddress = String.format("%%DB%d.DBX%d.%d:BOOL",
                dbNumber, byteOffset, bitOffset);
        executeWrite(tagAddress, value);
    }

    /**
     * Escreve um valor INT (16 bits signed) em uma DB.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @param value      valor a ser escrito (sera convertido para short)
     */
    public void writeInt(int dbNumber, int byteOffset, int value) {
        String tagAddress = String.format("%%DB%d.DBW%d:INT", dbNumber, byteOffset);
        // PLC4X espera short para tipo INT (16 bits signed)
        executeWrite(tagAddress, (short) value);
    }

    /**
     * Escreve um valor DINT (32 bits signed) em uma DB.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @param value      valor a ser escrito
     */
    public void writeDInt(int dbNumber, int byteOffset, int value) {
        String tagAddress = String.format("%%DB%d.DBD%d:DINT", dbNumber, byteOffset);
        executeWrite(tagAddress, value);
    }

    /**
     * Escreve um valor REAL (float 32 bits) em uma DB.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @param value      valor a ser escrito
     */
    public void writeReal(int dbNumber, int byteOffset, float value) {
        String tagAddress = String.format("%%DB%d.DBD%d:REAL", dbNumber, byteOffset);
        executeWrite(tagAddress, value);
    }

    /**
     * Escreve um valor STRING em uma DB.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @param value      valor a ser escrito (ate 254 caracteres)
     */
    public void writeString(int dbNumber, int byteOffset, String value) {
        String tagAddress = String.format("%%DB%d.DBB%d:STRING", dbNumber, byteOffset);
        executeWrite(tagAddress, value);
    }

    /**
     * Escreve um BYTE (unsigned 8 bits) em uma DB.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @param value      valor a ser escrito
     */
    public void writeByte(int dbNumber, int byteOffset, byte value) {
        String tagAddress = String.format("%%DB%d.DBB%d:BYTE", dbNumber, byteOffset);
        executeWrite(tagAddress, value);
    }

    /**
     * Escreve uma WORD (unsigned 16 bits) em uma DB.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @param value      valor a ser escrito (0 a 65535)
     */
    public void writeWord(int dbNumber, int byteOffset, int value) {
        String tagAddress = String.format("%%DB%d.DBW%d:WORD", dbNumber, byteOffset);
        executeWrite(tagAddress, value);
    }

    /**
     * Escreve uma DWORD (unsigned 32 bits) em uma DB.
     *
     * @param dbNumber   numero da DB (ex: 1 para DB1)
     * @param byteOffset offset do byte dentro da DB
     * @param value      valor a ser escrito (0 a 4294967295)
     */
    public void writeDWord(int dbNumber, int byteOffset, long value) {
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

    public int getRack() {
        return rack;
    }

    public int getSlot() {
        return slot;
    }

    // METODOS LEGADOS DE AbstractProtocol
    // S7 enderece por (db, byteOffset, bitOffset), nao por int unico.

    private static final String LEGACY_API_MSG =
            "S7 utiliza enderecamento por (DB, byteOffset, bitOffset). "
                    + "Use readBool/readInt/readReal/etc. em vez dos metodos genericos.";

    @Override public int readDataInt(int parameter) { throw new UnsupportedOperationException(LEGACY_API_MSG); }
    @Override public float readDataFloat(int parameter) { throw new UnsupportedOperationException(LEGACY_API_MSG); }
    @Override public boolean readDataBoolean(int param) { throw new UnsupportedOperationException(LEGACY_API_MSG); }
    @Override public String readDataString() { throw new UnsupportedOperationException(LEGACY_API_MSG); }
    @Override public String readDataString(int startingAddress, int registerCount) { throw new UnsupportedOperationException(LEGACY_API_MSG); }
    @Override public void writeData(int data) { throw new UnsupportedOperationException(LEGACY_API_MSG); }
    @Override public void writeData(float data) { throw new UnsupportedOperationException(LEGACY_API_MSG); }
    @Override public void writeData(boolean data) { throw new UnsupportedOperationException(LEGACY_API_MSG); }
    @Override public void writeData(String data) { throw new UnsupportedOperationException(LEGACY_API_MSG); }
    @Override public void writeData(int address, int data) { throw new UnsupportedOperationException(LEGACY_API_MSG); }
    @Override public void writeData(int address, float data) { throw new UnsupportedOperationException(LEGACY_API_MSG); }
    @Override public void writeData(int address, boolean data) { throw new UnsupportedOperationException(LEGACY_API_MSG); }
    @Override public void writeData(int startingAddress, String data) { throw new UnsupportedOperationException(LEGACY_API_MSG); }
}
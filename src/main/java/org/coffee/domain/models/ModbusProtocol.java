package org.coffee.domain.models;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.*;
import com.digitalpetri.modbus.responses.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.coffee.domain.abstracts.AbstractProtocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Implementação do protocolo Modbus TCP para comunicação industrial.
 * Utiliza a biblioteca digitalpetri para operações Modbus.
 */
public class ModbusProtocol extends AbstractProtocol {

    /** Instância do cliente Modbus TCP */
    private ModbusTcpMaster m_server;

    /** Identificador da unidade Modbus */
    private final int unitId;

    /** Flag indicando o estado da conexão */
    private boolean isConnected = false;

    /**
     * Construtor do protocolo Modbus.
     *
     * @param ip Endereço IP do servidor Modbus
     * @param port Porta de comunicação
     * @param unitId Identificador da unidade Modbus
     */
    public ModbusProtocol(String ip, int port, int unitId) {
        super(ip, port);
        this.unitId = unitId;
    }

    /**
     * Estabelece conexão com o servidor Modbus TCP.
     * Configura e inicializa o cliente Modbus.
     */
    @Override
    public void openConnection() {
        try {
            ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(getIp())
                    .setPort(getPort())
                    .build();

            m_server = new ModbusTcpMaster(config);
            m_server.connect().get();
            setConnected(true);
            System.out.println("Connected to Controller at " + getIp() + ":" + getPort());
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
            setConnected(false);
        }
    }

    /**
     * Encerra a conexão com o servidor Modbus TCP.
     * Libera recursos de rede utilizados.
     */
    @Override
    public void closeConnection() {
        if (m_server != null) {
            m_server.disconnect();
            setConnected(false);
            System.out.println("Connection closed");
        } else {
            System.out.println("Connection already down");
        }
    }

    /**
     * Lê um valor inteiro (16 bits) de um registrador Modbus.
     *
     * @param registerAddress Endereço do registrador a ser lido
     * @return Valor inteiro lido ou 0 se não conectado
     */
    @Override
    public int readDataInt(int registerAddress) {
        if(!isConnected()) return 0;

        ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(registerAddress, 1);
        CompletableFuture<ReadHoldingRegistersResponse> future = m_server.sendRequest(request, unitId);

        try {
            ReadHoldingRegistersResponse response = future.get();
            return response.getRegisters().readShort();
        } catch (Exception e) {
            System.err.println("Read int error: " + e.getMessage());
            return -1;
        } finally {
            ReferenceCountUtil.release(request);
        }
    }

    /**
     * Lê um valor ponto flutuante (32 bits) de dois registradores consecutivos.
     *
     * @param registerAddress Endereço inicial dos registradores
     * @return Valor float lido ou 0 se não conectado
     */
    @Override
    public float readDataFloat(int registerAddress) {
        if(!isConnected()) return 0;

        ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(registerAddress, 2);
        CompletableFuture<ReadHoldingRegistersResponse> future = m_server.sendRequest(request, unitId);

        try {
            ReadHoldingRegistersResponse response = future.get();
            ByteBuf registers = response.getRegisters();
            byte[] bytes = new byte[4];
            registers.readBytes(bytes);
            return ByteBuffer.wrap(bytes).getFloat();
        } catch (Exception e) {
            System.err.println("Read float error: " + e.getMessage());
            return -1;
        } finally {
            ReferenceCountUtil.release(request);
        }
    }

    /**
     * Lê o estado de uma bobina (coil) Modbus.
     *
     * @param coilAddress Endereço da bobina a ser lida
     * @return Estado da bobina (true/false) ou false se não conectado
     */
    @Override
    public boolean readDataBoolean(int coilAddress) {
        if(!isConnected()) return false;

        ReadCoilsRequest request = new ReadCoilsRequest(coilAddress, 1);
        CompletableFuture<ReadCoilsResponse> future = m_server.sendRequest(request, unitId);

        try {
            ReadCoilsResponse response = future.get();
            ByteBuf coilStatus = response.getCoilStatus();
            return (coilStatus.readByte() & 0x01) != 0;
        } catch (Exception e) {
            System.err.println("Read boolean error: " + e.getMessage());
            return false;
        } finally {
            ReferenceCountUtil.release(request);
        }
    }

    /**
     * Método não implementado para leitura de string sem parâmetros.
     *
     * @return String vazia
     */
    @Override
    public String readDataString() {
        return "";
    }

    /**
     * Lê uma string de múltiplos registradores consecutivos.
     * Cada registrador contém 2 bytes da string.
     *
     * @param startingAddress Endereço inicial dos registradores
     * @param registerCount Quantidade de registradores a ler
     * @return String lida ou vazia se não conectado
     */
    @Override
    public String readDataString(int startingAddress, int registerCount) {
        if(!isConnected()) return "";

        ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(startingAddress, registerCount);
        CompletableFuture<ReadHoldingRegistersResponse> future = m_server.sendRequest(request, unitId);

        try {
            ReadHoldingRegistersResponse response = future.get();
            ByteBuf registers = response.getRegisters();
            byte[] bytes = new byte[registerCount * 2];
            registers.readBytes(bytes);
            return new String(bytes, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            System.err.println("Read string error: " + e.getMessage());
            return "";
        } finally {
            ReferenceCountUtil.release(request);
        }
    }

    /**
     * Método não implementado para escrita de inteiro sem endereço.
     *
     * @param data Valor a ser escrito
     */
    @Override
    public void writeData(int data) {
        // Não implementado - use writeData(int address, int data)
    }

    /**
     * Escreve um valor inteiro em um registrador específico.
     *
     * @param address Endereço do registrador
     * @param data Valor inteiro a ser escrito
     */
    @Override
    public void writeData(int address, int data) {
        if(!isConnected()) return;

        WriteSingleRegisterRequest request = new WriteSingleRegisterRequest(address, data);
        CompletableFuture<?> future = m_server.sendRequest(request, unitId);

        try {
            future.get();
            System.out.println("Successfully wrote INT to address: " + address);
        } catch (Exception e) {
            System.err.println("Write int error: " + e.getMessage());
        } finally {
            ReferenceCountUtil.release(request);
        }
    }

    /**
     * Método não implementado para escrita de float sem endereço.
     *
     * @param data Valor a ser escrito
     */
    @Override
    public void writeData(float data) {
        // Não implementado - use writeData(int address, float data)
    }

    /**
     * Escreve um valor ponto flutuante em dois registradores consecutivos.
     *
     * @param address Endereço inicial dos registradores
     * @param data Valor float a ser escrito
     */
    @Override
    public void writeData(int address, float data) {
        if(!isConnected()) return;

        byte[] bytes = ByteBuffer.allocate(4).putFloat(data).array();
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);

        WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(
                address,
                2,
                buffer
        );

        CompletableFuture<?> future = m_server.sendRequest(request, unitId);

        try {
            future.get();
            System.out.println("Successfully wrote FLOAT to address: " + address);
        } catch (Exception e) {
            System.err.println("Write float error: " + e.getMessage());
        } finally {
            ReferenceCountUtil.release(request);
            ReferenceCountUtil.release(buffer);
        }
    }

    /**
     * Método não implementado para escrita de boolean sem endereço.
     *
     * @param data Valor a ser escrito
     */
    @Override
    public void writeData(boolean data) {
        // Não implementado - use writeData(int address, boolean data)
    }

    /**
     * Escreve um valor booleano em uma bobina (coil) específica.
     *
     * @param address Endereço da bobina
     * @param data Valor booleano a ser escrito
     */
    @Override
    public void writeData(int address, boolean data) {
        if(!isConnected()) return;

        WriteSingleCoilRequest request = new WriteSingleCoilRequest(address, data);
        CompletableFuture<?> future = m_server.sendRequest(request, unitId);

        try {
            future.get();
            System.out.println("Successfully wrote BOOLEAN to address: " + address);
        } catch (Exception e) {
            System.err.println("Write boolean error: " + e.getMessage());
        } finally {
            ReferenceCountUtil.release(request);
        }
    }

    /**
     * Método não implementado para escrita de string sem endereço.
     *
     * @param data String a ser escrita
     */
    @Override
    public void writeData(String data) {
        // Não implementado - use writeData(int startingAddress, String data)
    }

    /**
     * Escreve uma string em múltiplos registradores consecutivos.
     * Adiciona padding com zero se o comprimento for ímpar.
     *
     * @param startingAddress Endereço inicial dos registradores
     * @param data String a ser escrita
     */
    @Override
    public void writeData(int startingAddress, String data) {
        if(!isConnected()) return;

        byte[] stringBytes = data.getBytes(StandardCharsets.UTF_8);
        int registerCount = (int) Math.ceil(stringBytes.length / 2.0);
        ByteBuf buffer = Unpooled.buffer(registerCount * 2);
        buffer.writeBytes(stringBytes);

        // Adiciona padding com zero se houver número ímpar de bytes
        if (stringBytes.length % 2 != 0) {
            buffer.writeByte(0);
        }

        WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(
                startingAddress,
                registerCount,
                buffer
        );

        CompletableFuture<?> future = m_server.sendRequest(request, unitId);

        try {
            future.get();
            System.out.println("Successfully wrote STRING to address: " + startingAddress);
        } catch (Exception e) {
            System.err.println("Write string error: " + e.getMessage());
        } finally {
            ReferenceCountUtil.release(request);
            ReferenceCountUtil.release(buffer);
        }
    }

    /**
     * Retorna o identificador da unidade Modbus.
     *
     * @return Unit ID configurado
     */
    public int getUnitId() {
        return unitId;
    }

    /**
     * Verifica se a conexão está ativa.
     *
     * @return true se conectado, false caso contrário
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Define o estado da conexão.
     *
     * @param connected Novo estado da conexão
     */
    private void setConnected(boolean connected) {
        isConnected = connected;
    }
}
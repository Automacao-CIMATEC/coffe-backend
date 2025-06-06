package org.coffee.modeling;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.*;
import com.digitalpetri.modbus.responses.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class ModbusProtocolCOFFE extends ControllerProtocol {
    private ModbusTcpMaster m_server;
    private final int unitId;
    private boolean isConnected = false;

    public ModbusProtocolCOFFE(String ip, int port, int unitId) {
        super(ip, port);
        this.unitId = unitId;
    }

    @Override
    public void openConnection() {
        try {
            ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(getM_ip())
                    .setPort(getM_port())
                    .build();

            m_server = new ModbusTcpMaster(config);
            m_server.connect().get();
            setConnected(true);
            System.out.println("Connected to Controller at " + getM_ip() + ":" + getM_port());
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
            setConnected(false);
        }
    }

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

    // READ METHODS ===========================================================

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

    @Override
    public String readDataString() {
        return "";
    }

    @Override
    public void writeData(int data) {

    }

    @Override
    public void writeData(float data) {

    }

    @Override
    public void writeData(boolean data) {

    }

    @Override
    public void writeData(String data) {

    }

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

    // WRITE METHODS ==========================================================

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

    @Override
    public void writeData(int startingAddress, String data) {
        if(!isConnected()) return;

        byte[] stringBytes = data.getBytes(StandardCharsets.UTF_8);
        int registerCount = (int) Math.ceil(stringBytes.length / 2.0);
        ByteBuf buffer = Unpooled.buffer(registerCount * 2);
        buffer.writeBytes(stringBytes);

        // Pad with zero if odd number of bytes
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

    // GETTERS AND SETTERS ====================================================
    public int getUnitId() {
        return unitId;
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void setConnected(boolean connected) {
        isConnected = connected;
    }
}








//package org.coffee.modeling;
//
///**
// * Imports from Modbus reference Lib.
// */
//import com.digitalpetri.modbus.master.ModbusTcpMaster;
//import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
//import com.digitalpetri.modbus.requests.*;
//
///**
// * Imports from IO functionality
// */
//import com.digitalpetri.modbus.responses.ReadCoilsResponse;
//import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//
///**
// * Imports from JAVA utility Libs.
// */
//import java.nio.ByteBuffer;
//import java.util.concurrent.CompletableFuture;
//
//public class ModbusProtocolCOFFE extends ControllerProtocol
//{
//    /**
//     * ATTRIBUTES:
//     */
//
//    private ModbusTcpMaster m_server;
//    private final int unitId;
//    private boolean isConnected = false;
//
//    /**
//     * METHODS:
//     */
//
//    /**
//     * @brief Class Constructor.
//     * @param[in]   ip
//     * @param[in]   port
//     * @param[in]   unitId  Modbus device address
//     */
//    public ModbusProtocolCOFFE(String ip, int port, int unitId)
//    {
//        super(ip, port);
//        this.unitId = unitId;
//    }
//
//    @Override
//    public void openConnection()
//    {
//        try
//        {
//            ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(getM_ip())
//                    .setPort(getM_port())
//                    .build();
//
//            m_server = new ModbusTcpMaster(config);
//
//            m_server.connect().get();
//
//            setConnected(true);
//
//            System.out.println("Connected to Controller at " + getM_ip() + ":" + getM_port());
//        }
//        catch (Exception e)
//        {
//            System.err.println("Connection error: " + e.getMessage());
//            setConnected(false);
//        }
//    }
//
//    @Override
//    public void closeConnection()
//    {
//        if (m_server != null)
//        {
//            m_server.disconnect();
//
//            setConnected(false);
//
//            System.out.println("Connection closed");
//        }
//        else
//        {
//            System.out.println("Connection already down");
//        }
//    }
//
//    @Override
//    public int readDataInt(int registerAddress)
//    {
//        if(!isConnected()) /**< Check if Controller is connected */
//        {
//            return 0;
//        }
//
//        /**
//         * Create a request to read a specific register in a known position
//         */
//        ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(registerAddress, 1);
//
//        /**
//         * Create an instance and send the request through the connection.
//         */
//        CompletableFuture<ReadHoldingRegistersResponse> future = m_server.sendRequest(request, getUnitId());
//
//        try
//        {
//            /**
//             * Receive the answer and store in a 'ReadHoldingRegistersResponse' type
//             */
//            ReadHoldingRegistersResponse response = future.get();
//
//            return response.getRegisters().readShort();
//        }
//        catch (Exception e)
//        {
//            System.err.println("Read int error: " + e.getMessage());
//
//            return -1;
//        }
//    }
//
//    @Override
//    public float readDataFloat(int registerAddress)
//    {
//        if(!isConnected()) /**< Check if Controller is connected */
//        {
//            return 0;
//        }
//
//        /**
//         * Create a request to read a specific register in a known position
//         */
//        ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(registerAddress, 1);
//
//        /**
//         * Create an instance and send the request through the connection.
//         */
//        CompletableFuture<ReadHoldingRegistersResponse> future = m_server.sendRequest(request, getUnitId());
//
//        try
//        {
//            /**
//             * Receive the answer and store in a 'ReadHoldingRegistersResponse' type
//             */
//            ReadHoldingRegistersResponse response = future.get();
//
//            /**
//             * Store registers from response
//             */
//            ByteBuf registers = response.getRegisters();
//
//            /**
//             * Combine two 16-bit registers into a 32-bit float
//             */
//            byte[] bytes = new byte[4];
//            registers.readBytes(bytes, 0, 4);
//
//            /**
//             * Altus PLC typically uses big-endian byte order (most significant byte first).
//             * Implement modeling structure to identify PLC in use
//             */
//            return ByteBuffer.wrap(bytes).getFloat();
//        }
//        catch (Exception e)
//        {
//            System.err.println("Read float error: " + e.getMessage());
//
//            return -1;
//        }
//    }
//
//    @Override
//    public boolean readDataBoolean(int coilAddress)
//    {
//        if(!isConnected()) /**< Check if Controller is connected */
//        {
//            return false;
//        }
//
//        /**
//         * Create a request to read a specific coil in a known position
//         */
//        ReadCoilsRequest request = new ReadCoilsRequest(coilAddress, 1);
//
//        /**
//         * Create an instance and send the request through the connection.
//         */
//        CompletableFuture<ReadCoilsResponse> future = m_server.sendRequest(request, unitId);
//
//        try
//        {
//            /**
//             * Receive the answer and store in a 'ReadCoilsResponse' type
//             */
//            ReadCoilsResponse response = future.get();
//
//            ByteBuf coilStatus = response.getCoilStatus();
//
//            return (coilStatus.readByte() & 0x01) != 0;
//        }
//        catch (Exception e)
//        {
//            System.err.println("Read boolean error: " + e.getMessage());
//
//            return false;
//        }
//    }
//
//    @Override
//    public String readDataString()
//    {
//        return "";
//    }
//
//    @Override
//    public void writeData(int data)
//    {
//
//    }
//
//    @Override
//    public void writeData(float data)
//    {
//
//    }
//
//    @Override
//    public void writeData(boolean data)
//    {
//
//    }
//
//    @Override
//    public void writeData(String data)
//    {
//
//    }
//
//    /**
//     * Getters and Setters
//     */
//
//    /**< Encapsulation for 'unitId' */
//    public int getUnitId()
//    {
//        return unitId;
//    }
//
//    /**< Encapsulation for 'isConnected' */
//    public boolean isConnected()
//    {
//        return isConnected;
//    }
//    /**< Encapsulation for 'isConnected' */
//    private void setConnected(boolean connected)
//    {
//        isConnected = connected;
//    }
//}

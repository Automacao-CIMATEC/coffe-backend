package org.coffee.testes.modbus;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.*;
import com.digitalpetri.modbus.responses.*;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class ModbusClientReader {

    public static void main(String[] args) {
        // PLC configuration
        String plcIp = "192.168.1.25";
        int plcPort = 502;
        int unitId = 1; // Modbus address

        // Variable addresses - using the absolute addresses from your mapping
        // Note: Modbus protocol uses 0-based addressing, so we subtract 1 from the absolute addresses
        int coilAddress = 0;         // modbusBool (Coil 1 → 0 in protocol)
        int intRegisterAddress = 0;   // modbusInt (Holding Register 400001 → 0 in protocol)
        int floatRegisterAddress = 1; // modbusFloat (Holding Register 400002 → 1 in protocol)

        // Configure the Modbus TCP master
        ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(plcIp)
                .setPort(plcPort)
                .build();

        ModbusTcpMaster master = null;
        try {
            // Create the Modbus TCP master
            master = new ModbusTcpMaster(config);

            // Connect to the PLC
            master.connect().get();
            System.out.println("Connected to PLC at " + plcIp + ":" + plcPort);

            // 1. Read boolean coil (modbusBool)
            ReadCoilsRequest coilRequest = new ReadCoilsRequest(coilAddress, 1);
            CompletableFuture<ReadCoilsResponse> coilFuture = master.sendRequest(coilRequest, unitId);
            ReadCoilsResponse coilResponse = coilFuture.get();
            try {
                ByteBuf coilStatus = coilResponse.getCoilStatus();
                boolean coilValue = (coilStatus.readByte() & 0x01) != 0;
                System.out.println("modbusBool (Coil " + (coilAddress+1) + ") value: " + coilValue);
            } finally {
                ReferenceCountUtil.release(coilResponse);
            }

            // 2. Read integer from holding register (modbusInt)
            ReadHoldingRegistersRequest intRequest = new ReadHoldingRegistersRequest(intRegisterAddress, 1);
            CompletableFuture<ReadHoldingRegistersResponse> intFuture = master.sendRequest(intRequest, unitId);
            ReadHoldingRegistersResponse intResponse = intFuture.get();
            try {
                int intValue = intResponse.getRegisters().readShort(); // Using readShort() for INT (16-bit signed)
                System.out.println("modbusInt (Holding Register " + (intRegisterAddress+400001) + ") value: " + intValue);
            } finally {
                ReferenceCountUtil.release(intResponse);
            }

            // 3. Read float from holding registers (modbusFloat)
            ReadHoldingRegistersRequest floatRequest = new ReadHoldingRegistersRequest(floatRegisterAddress, 2);
            CompletableFuture<ReadHoldingRegistersResponse> floatFuture = master.sendRequest(floatRequest, unitId);
            ReadHoldingRegistersResponse floatResponse = floatFuture.get();
            try {
                ByteBuf registers = floatResponse.getRegisters();
                // Combine two 16-bit registers into a 32-bit float
                byte[] floatBytes = new byte[4];
                registers.readBytes(floatBytes, 0, 4);

                // Altus PLC typically uses big-endian byte order (most significant byte first)
                float floatValue = ByteBuffer.wrap(floatBytes).getFloat();
                System.out.println("modbusFloat (Holding Registers " + (floatRegisterAddress+400001) + "-" +
                        (floatRegisterAddress+400002) + ") value: " + floatValue);
            } finally {
                ReferenceCountUtil.release(floatResponse);
            }

        } catch (Exception e) {
            System.err.println("Error communicating with PLC: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Manually disconnect and clean up
            if (master != null) {
                master.disconnect();
            }
        }
    }
}
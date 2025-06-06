package org.coffee.testes.modbus;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class ModbusClientWriter {

    public static void main(String[] args) {
        // PLC configuration
        String plcIp = "192.168.1.25";
        int plcPort = 502;
        int unitId = 1; // Modbus address

        // Variable addresses (0-based as per Modbus protocol)
        int coilAddress = 0;         // modbusBool (Coil 1 → 0 in protocol)
        int intRegisterAddress = 0;   // modbusInt (Holding Register 400001 → 0 in protocol)
        int floatRegisterAddress = 1; // modbusFloat (Holding Register 400002 → 1 in protocol)

        // Values to write
        boolean boolValue = true;     // Example boolean value
        int intValue = 567567;         // Example integer value
        float floatValue = 5.234f;  // Example float value

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

            // 1. Write boolean coil (modbusBool)
            WriteSingleCoilRequest coilRequest = new WriteSingleCoilRequest(coilAddress, boolValue);
            CompletableFuture<?> coilFuture = master.sendRequest(coilRequest, unitId);
            coilFuture.get();
            System.out.println("Successfully wrote to modbusBool (Coil " + (coilAddress+1) + ") value: " + boolValue);

            // 2. Write integer to holding register (modbusInt)
            // Convert to unsigned short (0-65535) since Modbus registers are 16-bit unsigned
            int registerValue = intValue & 0xFFFF;
            WriteSingleRegisterRequest intRequest = new WriteSingleRegisterRequest(intRegisterAddress, registerValue);
            CompletableFuture<?> intFuture = master.sendRequest(intRequest, unitId);
            intFuture.get();
            System.out.println("Successfully wrote to modbusInt (Holding Register " + (intRegisterAddress+400001) + ") value: " + intValue);

            // 3. Write float to holding registers (modbusFloat)
            byte[] floatBytes = ByteBuffer.allocate(4).putFloat(floatValue).array();
            // Create buffer with the 4 bytes (2 registers)
            ByteBuf floatBuffer = Unpooled.buffer(4);
            floatBuffer.writeBytes(floatBytes);

            WriteMultipleRegistersRequest floatRequest = new WriteMultipleRegistersRequest(
                    floatRegisterAddress,
                    2, // Number of registers (2 for float)
                    floatBuffer
            );
            CompletableFuture<?> floatFuture = master.sendRequest(floatRequest, unitId);
            floatFuture.get();
            System.out.println("Successfully wrote to modbusFloat (Holding Registers " + (floatRegisterAddress+400001) + "-" +
                    (floatRegisterAddress+400002) + ") value: " + floatValue);

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
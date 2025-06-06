package org.coffee.testes.modeling;

import org.coffee.modeling.*;

public class ModbusReader
{
    public static void main(String[] args)
    {
        // Create class
//        ModbusProtocolCOFFE servidor = new ModbusProtocolCOFFE("192.168.1.25",502, 1);
//
//        servidor.openConnection();

        // Create protocol instance
        ModbusProtocolCOFFE modbus = new ModbusProtocolCOFFE("192.168.1.25", 502, 1);

// Connect to PLC
        modbus.openConnection();

// Read values
        boolean status = modbus.readDataBoolean(0);
        int value = modbus.readDataInt(0);
        float temperature = modbus.readDataFloat(1);
//        String name = modbus.readDataString(10, 5); // Starting address 10, 5 registers

// Write values
        modbus.writeData(0, true);               // Write boolean to coil 0
        modbus.writeData(0, 12345);              // Write int to register 0
        modbus.writeData(1, 23.45f);             // Write float to registers 1-2
//        modbus.writeData(10, "Sensor1");         // Write string starting at register 10

// Disconnect
        modbus.closeConnection();
    }
}

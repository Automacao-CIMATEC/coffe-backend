package org.coffee.testes.modeling;

import org.coffee.modeling.*;

public class ModbusReader
{
    public static void main(String[] args)
    {
        // Create class
        ModbusProtocolCOFFE servidor = new ModbusProtocolCOFFE("192.168.1.25",502, 1);

        servidor.openConnection();
    }
}

package org.coffee.modeling;

/**
 * Imports from Modbus reference Lib.
 */
import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.*;

/**
 * Imports from IO functionality
 */
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Imports from JAVA utility Libs.
 */
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class ModbusProtocolCOFFE extends ControllerProtocol
{
    /**
     * ATTRIBUTES:
     */

    private ModbusTcpMaster m_server;
    private final int unitId;
    private boolean isConnected = false;

    /**
     * METHODS:
     */

    /**
     * @brief Class Constructor.
     * @param[in]   ip
     * @param[in]   port
     * @param[in]   unitId  Modbus device address
     */
    public ModbusProtocolCOFFE(String ip, int port, int unitId)
    {
        super(ip, port);
        this.unitId = unitId;
    }

    @Override
    public void openConnection()
    {
        try
        {
            ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(getM_ip())
                    .setPort(getM_port())
                    .build();

            m_server = new ModbusTcpMaster(config);

            m_server.connect().get();

            setConnected(true);

            System.out.println("Connected to Controller at " + getM_ip() + ":" + getM_port());
        }
        catch (Exception e)
        {
            System.err.println("Connection error: " + e.getMessage());
            setConnected(false);
        }
    }

    @Override
    public void closeConnection()
    {
        if (m_server != null)
        {
            m_server.disconnect();

            setConnected(false);

            System.out.println("Connection closed");
        }
        else
        {
            System.out.println("Connection already down");
        }
    }

    @Override
    public int readDataInt(int registerAddress)
    {
        if(!isConnected())
        {
            return 0;
        }

        try
        {
            ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(registerAddress, 1);

            CompletableFuture<ReadHoldingRegistersResponse> future = m_server.sendRequest(request, getUnitId());

            return future.get().getRegisters().readShort();
        }
        catch (Exception e)
        {
            System.err.println("Read int error: " + e.getMessage());

            return -1;
        }
    }

    @Override
    public float readDataFloat()
    {
        return 0;
    }

    @Override
    public boolean readDataBoolean()
    {
        return false;
    }

    @Override
    public String readDataString()
    {
        return "";
    }

    @Override
    public void writeData(int data)
    {

    }

    @Override
    public void writeData(float data)
    {

    }

    @Override
    public void writeData(boolean data)
    {

    }

    @Override
    public void writeData(String data)
    {

    }

    /**
     * Getters and Setters
     */

    /**< Encapsulation for 'unitId' */
    public int getUnitId()
    {
        return unitId;
    }

    /**< Encapsulation for 'isConnected' */
    public boolean isConnected()
    {
        return isConnected;
    }
    /**< Encapsulation for 'isConnected' */
    public void setConnected(boolean connected)
    {
        isConnected = connected;
    }
}

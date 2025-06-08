package org.coffee.domain.abstracts;

/*
 * @brief This is the abstract class from witch all protocol classes will inherit
 */
public abstract class AbstractProtocol
{
    /**
     * ATTRIBUTES:
     */
    protected String ip;    // IP address of protocol instance
    protected int port;  // IP Port of protocol instance

    /**
     * METHODS:
     */

    /**
     * @brief Class Constructor
     * @param[in]   ip
     * @param[in]   port
     */
    public AbstractProtocol(String ip, int port)
    {
        this.ip = ip;
        this.port = port;
    }

    /**
     * @brief This method opens the connection between Server/Client
     * @return This method returns a 'ReturnType' Enum
     */
    public abstract void openConnection();

    /**
     * @brief This method closes the connection between Server/Client
     * @return This method returns a 'ReturnType' Enum
     */
    public abstract void closeConnection();

    /**
     * @brief This method requests content from the Server/Client
     * @param[out] dataList     An 'ArrayList' where the received message will be parsed
     * @return This method returns an 'int' type
     */
    public abstract int readDataInt(int parameter);

    /**
     * @brief This method requests content from the Server/Client
     * @param[out] dataList     An 'ArrayList' where the received message will be parsed
     * @return This method returns a 'float' type
     */
    public abstract float readDataFloat(int parameter);

    /**
     * @brief This method requests content from the Server/Client
     * @param[out] dataList     An 'ArrayList' where the received message will be parsed
     * @return This method returns a 'boolean' type
     */
    public abstract boolean readDataBoolean(int param);

    /**
     * @brief This method requests content from the Server/Client
     * @param[out] dataList     An 'ArrayList' where the received message will be parsed
     * @return This method returns a 'String' type
     */
    public abstract String readDataString();

    /**
     * @brief This method sends a message to the Server/Client
     * @param[in] data  A 'int' type containing the sent message
     */
    public abstract void writeData(int data);

    /**
     * @brief This method sends a message to the Server/Client
     * @param[in] data  A 'float' type containing the sent message
     */
    public abstract void writeData(float data);

    /**
     * @brief This method sends a message to the Server/Client
     * @param[in] data  A 'boolean' type containing the sent message
     */
    public abstract void writeData(boolean data);

    /**
     * @brief This method sends a message to the Server/Client
     * @param[in] data  A 'String' type containing the sent message
     */
    public abstract void writeData(String data);

    /**
     * GETTERS and SETTERS
     */

    /**< Encapsulation for 'ip' */
    public void setIp(String ip)
    {
        this.ip = ip;
    }
    /**< Encapsulation for 'ip' */
    public String getIp()
    {
        return ip;
    }

    /**< Encapsulation for 'port' */
    public int getPort()
    {
        return port;
    }
    /**< Encapsulation for 'port' */
    public void setPort(int port)
    {
        this.port = port;
    }

    public abstract String readDataString(int startingAddress, int registerCount);

    public abstract void writeData(int address, int data);

    public abstract void writeData(int address, float data);

    public abstract void writeData(int address, boolean data);

    public abstract void writeData(int startingAddress, String data);

    // GETTERS AND SETTERS ====================================================
    public abstract boolean isConnected();
}

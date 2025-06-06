package org.coffee.domain.abstracts;

/*
 * @brief This is the abstract class from witch all protocol classes will inherit
 */
public abstract class AbstractProtocol
{
    /**
     * ATTRIBUTES:
     */
    public String m_ip;    // IP address of protocol instance
    private int m_port;  // IP Port of protocol instance

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
        this.m_ip = ip;
        this.m_port = port;
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

    /**< Encapsulation for 'm_ip' */
    public void setM_ip(String m_ip)
    {
        this.m_ip = m_ip;
    }
    /**< Encapsulation for 'm_ip' */
    public String getM_ip()
    {
        return m_ip;
    }

    /**< Encapsulation for 'm_port' */
    public int getM_port()
    {
        return m_port;
    }
    /**< Encapsulation for 'm_port' */
    public void setM_port(int m_port)
    {
        this.m_port = m_port;
    }

    public abstract String readDataString(int startingAddress, int registerCount);

    public abstract void writeData(int address, int data);

    public abstract void writeData(int address, float data);

    public abstract void writeData(int address, boolean data);

    public abstract void writeData(int startingAddress, String data);
}

package org.coffee.modeling;

import org.coffee.modeling.ReturnType;

import java.util.ArrayList;
import java.util.List;

/*
 * @brief This is the abstract class from witch all protocol classes will inherit
 */
public abstract class ControllerProtocol
{
    /*
     * ATTRIBUTES:
     */
    public Integer m_ip;    // IP address of protocol instance
    public Integer m_port;  // IP Port of protocol instance

    /*
     * METHODS:
     */

    /*
     * @brief Class Constructor
     * @param[in]   ip
     * @param[in]   port
     */
    public ControllerProtocol(Integer ip, Integer port)
    {
        this.m_ip = ip;
        this.m_port = port;
    }

    /*
     * @brief This method opens the connection between Server/Client
     * @return This method returns a 'ReturnType' Enum
     */
    public ReturnType openConnection()
    {
        ReturnType ret = ReturnType.IN_PROGRESS;

        /* Operation */

        return ret;
    }

    /*
     * @brief This method closes the connection between Server/Client
     * @return This method returns a 'ReturnType' Enum
     */
    public ReturnType closeConnection()
    {
        ReturnType ret = ReturnType.IN_PROGRESS;

        /* Operation */

        return ret;
    }

    /*
     * @brief This method requests content from the Server/Client
     * @param[out] dataList     An 'ArrayList' where the received message will be parsed
     * @return This method returns a 'ReturnType' Enum
     */
    public ReturnType readData(ArrayList dataList)
    {
        ReturnType ret = ReturnType.IN_PROGRESS;

        /* Operation with ArrayList */

        return ret;
    }

    /*
     * @brief This method sends a message to the Server/Client
     * @param[in] dataList      An 'ArrayList' containing the sent message
     * @return This method returns a 'ReturnType' Enum
     */
    public ReturnType writeData(ArrayList dataList)
    {
        ReturnType ret = ReturnType.IN_PROGRESS;

        /* Operation with ArrayList */

        return ret;
    }
}

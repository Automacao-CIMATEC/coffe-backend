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
     * ATTRIBUTES
     */
    public Integer m_ip;
    public Integer m_port;

    /*
     * METHODS:
     */

    /*
     * @brief Class Constructor
     */
    public ControllerProtocol(Integer ip, Integer port)
    {
        this.m_ip = ip;
        this.m_port = port;
    }

    /*
     * @brief
     */
    public ReturnType openConnection()
    {
        ReturnType ret = ReturnType.IN_PROGRESS;

        /* Operation */

        return ret;
    }

    /*
     * @brief
     */
    public ReturnType closeConnection()
    {
        ReturnType ret = ReturnType.IN_PROGRESS;

        /* Operation */

        return ret;
    }

    /*
     * @brief
     */
    public ReturnType readData(ArrayList dataList)
    {
        ReturnType ret = ReturnType.IN_PROGRESS;

        /* Operation with ArrayList */

        return ret;
    }

    /*
     * @brief Class Constructor
     */
    public ReturnType writeData(ArrayList dataList)
    {
        ReturnType ret = ReturnType.IN_PROGRESS;

        /* Operation with ArrayList */

        return ret;
    }
}

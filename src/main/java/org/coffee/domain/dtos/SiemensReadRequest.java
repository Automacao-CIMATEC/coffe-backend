package org.coffee.domain.dtos;

import org.coffee.domain.enums.PlcDataType;
import org.coffee.domain.models.SiemensTag;

public class SiemensReadRequest {
    private String ip;
    private PlcDataType dataType;
    private SiemensTag tag;

    // Getters and Setters
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public PlcDataType getDataType() {
        return dataType;
    }

    public void setDataType(PlcDataType dataType) {
        this.dataType = dataType;
    }

    public SiemensTag getTag() {
        return tag;
    }

    public void setTag(SiemensTag tag) {
        this.tag = tag;
    }
}
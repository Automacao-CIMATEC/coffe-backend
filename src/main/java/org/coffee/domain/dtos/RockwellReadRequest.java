package org.coffee.domain.dtos;

import org.coffee.domain.enums.PlcDataType;
import org.coffee.domain.models.RockwellTag;

public class RockwellReadRequest {
    private String ip;
    private PlcDataType dataType;
    private RockwellTag tag;
    private String tagName;

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

    public RockwellTag getTag() {
        return tag;
    }

    public void setTag(RockwellTag tag) {
        this.tag = tag;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
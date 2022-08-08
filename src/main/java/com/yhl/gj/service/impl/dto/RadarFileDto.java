package com.yhl.gj.service.impl.dto;

import lombok.Data;

/**
 * 雷达数据文件名转换DTO
 */
@Data
public class RadarFileDto {

    private String name;
    private String path;

    public RadarFileDto(String name, String path) {
        this.name = name;
        this.path = path;
    }
}

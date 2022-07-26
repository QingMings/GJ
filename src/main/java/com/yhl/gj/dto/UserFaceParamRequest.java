package com.yhl.gj.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserFaceParamRequest implements ParamRequest{

    private Integer taskType ;
    private String pathSatelliteTextArea;
    private String pathSatelliteFilePath;
    private List<String> pathTargetOrbitList;
    private List<String> pathTargetRadarList;
    private String pathTargetLaser;
    private String pathObsGtw;
    private String pathObsEph;
    private Date startTimeUtc;
    private Integer days;


}

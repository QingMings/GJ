package com.yhl.gj.commons.constant;

public interface Constants {

    boolean SUCCESS = true;
    boolean FAIL = false;
    String DEFAULT_CHARSET = "UTF-8";
    String TOKEN = "token";
    String COMMA = ",";
    String FILE_SEPARATOR="/";
    String PARAM_USER_ID = "userId";
    String PARAM_USER_NAME="userName";
    Integer ZERO = 0;
    Integer ONE = 1;
    Integer TWO = 2;
    Long ZERO_L = 0L;
    String ZERO_STR = "0";
    String ONE_STR = "1";
    Integer PAGE_SIZE_10 = 10;

    Integer PAGE_SIZE_20 = 20;

    Integer IS_ROOT = 1;
    Integer NOT_ROOT = 2;


    Integer IS_CORRECT = 1;
    String TASK_FINISHED_FILE_FLAG = "";
    Integer RUNNING = 1;
    Integer FINISHED = 0;
    Integer DEFAULT_CONFIG = 1;
    // ============ 构建 inputFiles 时候的json key
    String PATH_SATELLITE = "path_satellite";
    String TARGET_ORBITS = "path_target_orbit_list";
    String TARGET_RADARS = "path_target_radar_list";
    String TARGET_LASERS = "path_target_laser_list";
    String OBS_GTW = "path_obs_gtw";
    String OBS_EPH = "path_obs_eph";

    String SAT_ID = "satid";
    String PATHS = "paths";

    // ============ 配置参数路径分类标识 =================
    String LEAP = "LEAP";
    String EOP = "EOP";
    String SWD = "SWD";
    String ERR = "ERR";

    // =========== 默认参数文件名 ======================
    String DEFAULT_PARAM = "defaultParam.json";

    //============= 构建输出 的 jsonKey ===========

    String Detail = "detail";
    String Path = "path";

    String MAX_GJ = "max_GJ";
    String PinGu = "pinGu";
    String TargetLaser = "targetLaser";
    String STRATEGY = "strategy";
    String TargetOrbit = "targetOrbit";


}

package com.yhl.gj.service;

import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.StrategyDTO;
import com.yhl.gj.model.TaskResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yhl.gj.param.ResultQueryRequest;

public interface TaskResultService extends IService<TaskResult> {


    Response queryResultByCondition(ResultQueryRequest request);


    Response getOneWithBlobs(Long id);

    Response getSatellites();

    StrategyDTO getMovesById(Long id);

    String getPathGbclXml(Long id);
}





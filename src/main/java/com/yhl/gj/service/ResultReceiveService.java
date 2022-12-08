package com.yhl.gj.service;

import com.alibaba.fastjson.JSONObject;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.OrderDTO;
import com.yhl.gj.param.ResultQueryRequest;

import java.io.IOException;

public interface ResultReceiveService {


    Response  onReceiveData(JSONObject receiveData);

    Response manualExecuteTask(OrderDTO orderRequest);

    Response queryResultByCondition(ResultQueryRequest request);

    Response listDir(String dir) throws IOException;

    Response getOne(Long id);

    Response getSatellites();

    Response getDefaultParam() ;

    Response getMovesToFiles(Long id, String type);

    Response sendXmlToMq(Long id);
}

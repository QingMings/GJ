package com.yhl.gj.service;

import com.alibaba.fastjson.JSONObject;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.OrderDTO;
import com.yhl.gj.param.ResultQueryRequest;

public interface ResultReceiveService {


    Response  onReceiveData(JSONObject receiveData);

    Response manualExecuteTask(OrderDTO orderRequest);

    Response queryResultByCondition(ResultQueryRequest request);
}

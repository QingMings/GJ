package com.yhl.gj.service;

import com.alibaba.fastjson.JSONObject;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.model.Task;
import com.yhl.gj.param.OrderRequest;

public interface CallWarningService {

    Response<Integer> call(OrderRequest request);

    Response<Integer> executeTask(Task task, JSONObject param);

    JSONObject flushDefaultConfigToDB();

    JSONObject loadDefaultParams();
}

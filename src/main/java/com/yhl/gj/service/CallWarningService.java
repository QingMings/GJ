package com.yhl.gj.service;

import com.alibaba.fastjson.JSONObject;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.dto.ParamRequest;
import com.yhl.gj.model.Task;

public interface CallWarningService {


    Response call(ParamRequest request) ;

    Response executeTask(Task task,JSONObject param);
}

package com.yhl.gj.service.impl;

import com.yhl.gj.commons.base.Response;
import com.yhl.gj.component.CallWarningProgramTask;
import com.yhl.gj.component.PyLogProcessComponent;
import com.yhl.gj.config.pyconfig.PyCmdParamConfig;
import com.yhl.gj.dto.DataDriverParamRequest;
import com.yhl.gj.dto.ParamRequest;
import com.yhl.gj.dto.UserFaceParamRequest;
import com.yhl.gj.service.CallWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class CallWarningServiceImpl implements CallWarningService {
    ThreadFactory callWarningThreadFactory = new CustomizableThreadFactory("call-warning-thread-pool-");
    ExecutorService executorService = Executors.newFixedThreadPool(9, callWarningThreadFactory);


    @Resource
    private PyLogProcessComponent pyLogProcessComponent;
    @Resource
    private PyCmdParamConfig pyCmdParamConfig;

    @Override
    public Response call(ParamRequest request) {
        if (request instanceof DataDriverParamRequest) {
            log.info("dataDriver");
            log.info(request.toString());

            CallWarningProgramTask callWarningProgramTask = new CallWarningProgramTask(buildCmd(request), pyLogProcessComponent);
            executorService.submit(callWarningProgramTask);
        } else if (request instanceof UserFaceParamRequest) {
            log.info("userFace");
            log.info(request.toString());
        }
        return null;
    }

    private String[] buildCmd(ParamRequest request) {
        List<String> cmd = pyCmdParamConfig.getCmd();
        if (request instanceof DataDriverParamRequest) {
            cmd.add(((DataDriverParamRequest) request).getOrderXmlPath());
        }
        return cmd.toArray(new String[0]);
    }
}

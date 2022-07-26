package com.yhl.gj.service.impl;

import com.yhl.gj.commons.base.Response;
import com.yhl.gj.commons.constant.CallPyModel;
import com.yhl.gj.component.CallWarningProgramTask;
import com.yhl.gj.component.PyLogProcessComponent;
import com.yhl.gj.config.pyconfig.PyCmdParamConfig;
import com.yhl.gj.dto.DataDriverParamRequest;
import com.yhl.gj.dto.ParamRequest;
import com.yhl.gj.dto.UserFaceParamRequest;
import com.yhl.gj.service.CallWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
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
    @Resource
    private org.springframework.core.io.Resource pyWork;

    @Override
    public Response call(ParamRequest request)  {
        try {
        if (request instanceof DataDriverParamRequest) {
            log.info("dataDriver");
            log.info(request.toString());

            CallWarningProgramTask callWarningProgramTask = null;

                callWarningProgramTask = new CallWarningProgramTask(buildCmd(request), pyLogProcessComponent,pyWork.getFile(), CallPyModel.DATA_DRIVER);

            executorService.submit(callWarningProgramTask);
        } else if (request instanceof UserFaceParamRequest) {
            log.info("userFace");
            log.info(request.toString());
        }
        } catch (IOException e) {
            throw new RuntimeException(e);
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

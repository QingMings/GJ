package com.yhl.gj.config.pyconfig;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.yhl.gj.commons.base.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class OrderRunParamConfig {
    @Value("${task.orderRunParam.savePath}")
    private String orderRunParamSavePath;

    @Value("${task.orderRunParam.createDirByDay}")
    private boolean createDirByDay;
    @Value("${task.orderRunParam.createDirByTask}")
    private boolean createDirByTask;

    public Response<File> writeRunParams(String fileName, String content) {
        String orderRunParamSavePathNew = ObjectUtil.clone(orderRunParamSavePath);
        Response<File> response = new Response<>();
        try {
            orderRunParamSavePathNew = createDirByDay(orderRunParamSavePathNew);
            orderRunParamSavePathNew = createDirByTask(orderRunParamSavePathNew, fileName);
            Resource resource = new FileUrlResource(appendFileName(orderRunParamSavePathNew, fileName));
            FileUtil.writeUtf8String(content, resource.getFile());
            response.setSuccess(true);
            response.setData(resource.getFile());
            return response;
        } catch (IOException e) {
            log.error("写入运行参数到磁盘出错：{}", e.getMessage());
            response.setMessage(e.getMessage());
        }
        return response;
    }

    private String createDirByDay(String orderRunParamSavePath) {
        if (createDirByDay) {
            orderRunParamSavePath = orderRunParamSavePath.concat(StrUtil.SLASH).concat(DateUtil.today());
        }
        return orderRunParamSavePath;
    }

    private String createDirByTask(String orderRunParamSavePath, String fileName) {
        if (createDirByTask) {
            int index = fileName.indexOf(StrUtil.UNDERLINE);
            orderRunParamSavePath = orderRunParamSavePath.concat(StrUtil.SLASH).concat(fileName.substring(0, index));
        }
        return orderRunParamSavePath;
    }

    private String appendFileName(String orderRunParamSavePath, String fileName) {
        return orderRunParamSavePath.concat(StrUtil.SLASH).concat(fileName).concat(".json");
    }
}

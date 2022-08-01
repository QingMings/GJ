package com.yhl.gj.config.pyconfig;

import cn.hutool.core.io.FileUtil;
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
    @Value("${task.orderRunParamSavePath}")
    private String orderRunParamSavePath;


    public Response<File> writeRunParams(String fileName, String content) {
        Response<File> response = new Response<>();
        try {
            Resource resource = new FileUrlResource(orderRunParamSavePath.concat("/").concat(fileName).concat(".json"));
            FileUtil.writeUtf8String(content,resource.getFile());
            response.setSuccess(true);
            response.setData(resource.getFile());
            return response;
        }catch (IOException e){
            log.error("写入运行参数到磁盘出错：{}",e.getMessage());
            response.setMessage(e.getMessage());
        }
        return response;
    }
}

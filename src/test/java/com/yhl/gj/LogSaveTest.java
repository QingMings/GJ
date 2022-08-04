package com.yhl.gj;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import com.yhl.gj.component.PyLogProcessComponent;
import com.yhl.gj.model.TaskDetails;
import com.yhl.gj.service.TaskDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class LogSaveTest {

    @Resource
    private PyLogProcessComponent logProcessComponent;

    @Test
    public void logTest() {

        String file = "logtest.txt";
        List<String> lines = FileUtil.readUtf8Lines(file);
        Assert.notNull(lines);
        lines.forEach(t -> {
//             logProcessComponent.pythonPrintProcess(t, CallPyModel.DATA_DRIVER);
        });

    }
    @Resource
    private TaskDetailsService detailsService;
    @Test
    public void select(){
        List<TaskDetails> taskDetails = detailsService.list();
        taskDetails.forEach(t -> {
            System.out.println(t);
        });
    }

    @Test
    public void select2()
    {
       TaskDetails taskDetails = detailsService.findLastTaskDetails(40L);
        System.out.println(taskDetails);
    }
}

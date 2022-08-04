package com.yhl.gj;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import com.yhl.gj.component.PyLogProcessComponent;
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
}

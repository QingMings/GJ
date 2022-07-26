package com.yhl.gj.component;

import cn.hutool.core.util.ObjectUtil;
import com.yhl.gj.config.pyconfig.PyLogRegexConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

@Slf4j
public class CallWarningProgramTask extends Thread {

    // cmd 命令参数
    private final String[] cmdArray;
    private final PyLogProcessComponent logProcessComponent;
    private final File workDir;

    public CallWarningProgramTask(String[] cmdArray, PyLogProcessComponent logProcessComponent, File workDir,String model) {
        super("call-warning-thread");
        this.cmdArray = cmdArray;
        this.logProcessComponent = logProcessComponent;
        this.workDir = workDir;
    }

    @Override
    public void run() {
        log.info("run callWarningProgramTask ");
        Process proc = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(cmdArray);
            pb.redirectErrorStream(true);
            pb.directory(workDir);
            proc = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                logProcessComponent.pythonPrintProcess(line);
            }
            in.close();
            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ObjectUtil.isNotNull(proc)) {
                proc.destroy();
            }
        }
        log.info("end callWarningProgramTask ");
    }
    // 处理 python 输出的日志

}

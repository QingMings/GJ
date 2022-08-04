package com.yhl.gj.component;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.system.SystemUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CallWarningProgramTask extends Thread {

    // cmd 命令参数
    private final String[] cmdArray;
    private final PyLogProcessComponent logProcessComponent;
    private final File workDir;
    private final String model;
    private final String logTrackId;

    public CallWarningProgramTask(String[] cmdArray, PyLogProcessComponent logProcessComponent, File workDir,String trackId,String model) {
        super("call-warning-thread");
        this.cmdArray = cmdArray;
        this.logProcessComponent = logProcessComponent;
        this.logTrackId = trackId;
        this.workDir = workDir;
        this.model = model;
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
            String encoding = SystemUtil.getOsInfo().isWindows()? "gbk": "utf-8";
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), encoding));
            List<String> pylogs = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null) {
                pylogs.add(line);
            }
            logProcessComponent.pythonLogHandle(pylogs, logTrackId, model);
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

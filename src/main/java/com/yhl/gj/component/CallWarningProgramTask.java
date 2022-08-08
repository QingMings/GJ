package com.yhl.gj.component;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.system.SystemUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        log.info("run callWarningProgramTask:{} ",logTrackId);
        StopWatch stopWatch = new StopWatch(logTrackId);

        Process proc = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(cmdArray);
            pb.redirectErrorStream(true);
            pb.directory(workDir);
            stopWatch.start("执行任务:"+logTrackId);
            proc = pb.start();
            String encoding = SystemUtil.getOsInfo().isWindows()? "gbk": "utf-8";
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), encoding));
            List<String> pylogs = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null) {
                pylogs.add(line);
                logProcessComponent.code151Handle(line,logTrackId,model); // 151的及时上报，不等其他日志和结果
                logProcessComponent.code150Handle(line,logTrackId,model); // 150 及时更新到数据库，不等策略日志输出
            }
            stopWatch.stop();
            stopWatch.start("日志处理:"+logTrackId);
            logProcessComponent.pythonLogHandle(pylogs, logTrackId, model);
            stopWatch.stop();
            in.close();
            proc.waitFor();
        } catch (Exception e) {
            logProcessComponent.errorSendToMQ(e.getCause().getMessage(),logTrackId);
            e.printStackTrace();
        } finally {
            if (ObjectUtil.isNotNull(proc)) {
                proc.destroy();
            }
        }

        log.info("end callWarningProgramTask :{},\n耗時：{}",logTrackId,stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }
    // 处理 python 输出的日志

}

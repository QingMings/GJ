package com.yhl.gj.component;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.system.SystemUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CallWarningV3Task extends Thread {
    // cmd 命令参数
    private final String[] cmdArray;
    private final File workDir;

    public CallWarningV3Task(String[] cmdArray, File workDir) {
        super("call-warningV3-thread");
        this.cmdArray = cmdArray;
        this.workDir = workDir;
    }

    @Override
    public void run() {
        log.info("run call warningV3 ");
        Process proc = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(cmdArray);
            pb.redirectErrorStream(true);
            pb.directory(workDir);
            proc = pb.start();
            String encoding = SystemUtil.getOsInfo().isWindows() ? "gbk" : "utf-8";
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), encoding));

            String line;
            while ((line = in.readLine()) != null) {
               log.info(line);
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
    }
}

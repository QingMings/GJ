package com.yhl.gj.service.impl;

import com.yhl.gj.fileWatch.FileListenerFactory;
import com.yhl.gj.fileWatch.StartThread;
import com.yhl.gj.service.FileListenerService;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
@Service
public class FileListenerServiceImpl implements FileListenerService {
    @Resource
    FileListenerFactory fileListenerFactory;
    @Override
    public void startListener(Map<String, FileAlterationMonitor> runingPool, String e) {
        if(null != runingPool.get(e)){
            return;
        }
        FileAlterationMonitor monitor = fileListenerFactory.getMonitor(e);
        try {
            monitor.start();
            //放入执行池
            StartThread.runingPool.put(e,monitor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

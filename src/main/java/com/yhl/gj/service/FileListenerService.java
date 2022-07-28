package com.yhl.gj.service;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
public interface FileListenerService {


    void startListener(Map<String, FileAlterationMonitor> runingPool, String e);
}

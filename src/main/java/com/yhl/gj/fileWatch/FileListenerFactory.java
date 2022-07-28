package com.yhl.gj.fileWatch;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class FileListenerFactory {


    public FileAlterationMonitor getMonitor(String path){
        FileListener fileListener = new FileListener(path);
        System.out.println(path);
        long interval = 3*1000;
        // 创建文件过滤器
        // 前缀过滤器
        IOFileFilter prefixFileFilter = FileFilterUtils.prefixFileFilter("hello");
        // 后缀过滤器
        IOFileFilter suffixFileFilter = FileFilterUtils.suffixFileFilter(".txt");
        // 俩者满足其一
        IOFileFilter filter = FileFilterUtils.or(prefixFileFilter, suffixFileFilter);
        // 装配过滤器
        FileAlterationObserver observer = new FileAlterationObserver(new File(path), filter);
        //  向监听者添加监听器
        observer.addListener(fileListener);
        // 返回监听者
        return new FileAlterationMonitor(interval, observer);
    }
}

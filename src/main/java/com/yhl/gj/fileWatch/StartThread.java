package com.yhl.gj.fileWatch;

import com.yhl.gj.service.FileListenerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
public class StartThread implements DisposableBean,Runnable {
    private Thread thread;

    private volatile boolean someCondition = true;
    //执行池 + redis锁 实际代码中执行池应该在service
    public static Map<String, FileAlterationMonitor> runingPool = Collections.synchronizedMap(new HashMap<>());
    @Resource
    FileListenerService fileListenerService;

    @Autowired
    public StartThread() {
        this.thread = new Thread(this);
        this.thread.setDaemon(true);
//        this.thread.start();
//        System.out.print("线程启动");
    }
    static List<String> paths = Arrays.asList("E:/data1","E:/data2");
    @Override
    public void run() {
        int i = 0;
        while (someCondition) {
            //TODO 如果是多集群 可以使用redis分布式锁 确保监听不会被多集群节点重复启用
            i ++;
//            if(runingPool.size()  == 0){
//                paths.stream().forEach(e->{
//                    //启动监听任务
//                    fileListenerService.startListener(runingPool,e);
//                });
//            }
            try {
                TimeUnit.SECONDS.sleep(1);
//                System.out.println(runingPool.toString());
                //每10次销毁一次
//                if (i % 10 == 0){
//                    String path  = null;
//                    for (String s : runingPool.keySet()) {
//                        //获取执行池中的监听任务并销毁
//                        runingPool.get(s).stop();
//                        log.info("{} stop",s);
//                        path = s;
//                        break;
//                    }
//                    if (StringUtils.hasText(path)){
//                        log.info("{} remove",path);
//                        runingPool.remove(path);
//                    }
//
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void destroy() throws Exception {
        System.out.println("守护线程终止");
        someCondition = false;
    }
}

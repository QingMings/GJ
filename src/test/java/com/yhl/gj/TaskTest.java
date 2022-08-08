package com.yhl.gj;

import com.yhl.gj.model.Task;
import com.yhl.gj.model.TaskDetails;
import com.yhl.gj.service.TaskDetailsService;
import com.yhl.gj.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class TaskTest {

    @Resource
    private TaskService taskService;

    @Test
     void testInsertTask(){
        String taskName= "479055";
        String orderPath = "/Users/shishifanbuxie/FTP/temp";
        Task task = new Task(taskName,orderPath);
        taskService.save(task);
    }

    @Resource
    private TaskDetailsService taskDetailsService;
    @Test
    void testInsertTaskDetails() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            Thread.sleep(2000);
            TaskDetails taskDetails = new TaskDetails(1L, "/Users/shishifanbuxie/FTP/temp", "479055", 1);
            taskDetailsService.save(taskDetails);
        }

    }

}

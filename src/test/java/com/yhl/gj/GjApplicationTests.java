package com.yhl.gj;

import com.yhl.gj.service.TestService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class GjApplicationTests {

    @Resource
    private TestService testService;

    @Test
    void contextLoads() {

        com.yhl.gj.model.Test test = new com.yhl.gj.model.Test();
        test.setName("Test");
        test.setEmail("Test@qq.com");

        testService.save(test);

      List<com.yhl.gj.model.Test> testList= testService.list();
      testList.forEach(m -> {
          System.out.println(m.toString());
      });
    }

}

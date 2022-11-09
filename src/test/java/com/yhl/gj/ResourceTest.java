package com.yhl.gj;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.MalformedURLException;

public class ResourceTest {

    public static void main(String[] args) throws IOException {
        Resource resource = new FileUrlResource("/Users/shishifanbuxie/IdeaProjects/GJ/warningProgram/core/input/order-warning.param.xml");
        System.out.println(resource.getFile().getAbsoluteFile());
    }


    @Test
    public void test2() throws IOException {
        String rootPath = "warningProgram/core-v3/";
        String rPath = "./DiskArray/GEOGJ/N202209181234//L3/GJXX//BACKEND/N20220829000601_mv_before_p9701_s46611.json";
    FileUrlResource fileUrlResource = new FileUrlResource(rootPath);
        System.out.println(fileUrlResource.createRelative(rPath).getFile().getAbsoluteFile());

    }

    @Test
    public  void test3(){
        String str = String.format("_m%02d",1);
        System.out.println(str);
    }
}

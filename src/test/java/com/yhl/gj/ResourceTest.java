package com.yhl.gj;

import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.MalformedURLException;

public class ResourceTest {

    public static void main(String[] args) throws IOException {
        Resource resource = new FileUrlResource("/Users/shishifanbuxie/IdeaProjects/GJ/warningProgram/core/input/order-warning.param.xml");
        System.out.println(resource.getFile().getAbsoluteFile());
    }
}

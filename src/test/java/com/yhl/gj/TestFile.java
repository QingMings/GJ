package com.yhl.gj;

import cn.hutool.core.io.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

public class TestFile {


    public static void main(String[] args) throws FileNotFoundException {

//        String path = "/Users/shishifanbuxie/FTP/temp";
//        AndFileFilter andFileFilter = new AndFileFilter();
//        IOFileFilter txtAndJsonFileFilter = FileFilterUtils.and(
//                FileFileFilter.INSTANCE,
//                FileFilterUtils.or(FileFilterUtils.suffixFileFilter(".txt"),FileFilterUtils.suffixFileFilter(".json")));
//
//        ;
//        List<File> files = FileUtil.loopFiles(path, txtAndJsonFileFilter);
//        files.forEach(t-> {
//            System.out.println(t.getAbsolutePath());
//        });

        String name = "defaultParam.json";
        String content = FileUtil.readUtf8String(name);
        File file = ResourceUtils.getFile(name);
        System.out.println(file.getAbsolutePath());
    }

    @Test
    private void load() throws FileNotFoundException {

    }

}

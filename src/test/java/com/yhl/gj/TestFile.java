package com.yhl.gj;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.tree.TreeUtil;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestFile {


    public static void main(String[] args) {

        String path = "/Users/shishifanbuxie/FTP/temp";
        AndFileFilter andFileFilter = new AndFileFilter();
        IOFileFilter and = FileFilterUtils.and(
                FileFileFilter.INSTANCE,
                FileFilterUtils.suffixFileFilter(".txt"));
        ;
        List<File> files = FileUtil.loopFiles(path, FileFilterUtils.or(and,FileFilterUtils.suffixFileFilter(".zip")));
        files.forEach(t-> {
            System.out.println(t.getAbsolutePath());
        });
    }


}

package com.yhl.gj;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.yhl.gj.util.FileNameAgeFileFilter;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class NameFileComparatorTest {


    @Test
    public void test() {
        String orderPath = "/Users/shishifanbuxie/yhlWork/GJ项目/GjDist/Orders/order1";
        IOFileFilter satFileFilter = FileFilterUtils.suffixFileFilter(".sat");
        List<File> fileList = FileUtil.loopFiles(orderPath, satFileFilter);
        List<File> sortedFiles = CollectionUtil.sort(fileList, NameFileComparator.NAME_REVERSE);
        for (File file : sortedFiles) {
            System.out.println(file.getName());
        }
    }

    @Test
    public void test2() {
        String orderPath = "/Users/shishifanbuxie/yhlWork/GJ项目/GjDist/Orders/order1/46610_2020_11_28_12_12_00.0.sat";
        System.out.println(Paths.get(orderPath).getFileName().toString());
    }

    @Test
    public void test3() {
        String orderPath = "/Users/shishifanbuxie/yhlWork/GJ项目/GjDist/Orders/order1/111111_2020_11_28_12_00_00.0.rae";

        String fileName = FileUtil.mainName(Paths.get(orderPath).toFile());
        int index = fileName.indexOf("_");
        String timePart = fileName.substring(index + 1);
        System.out.println(timePart);
        DateTime dateTime = DateUtil.parse(timePart, "yyyy_MM_dd_HH_mm_ss.S");
        System.out.println(dateTime);
    }

    @Test
    public void test4() {
        Path dir = Paths.get("/Users/shishifanbuxie/yhlWork/GJ项目/GjDist/Orders/order1/");
        // We are interested in files older than one day
        long cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

        String[] files = dir.toFile().list(FileFilterUtils.and(FileFilterUtils.suffixFileFilter(".sat"), new FileNameAgeFileFilter(cutoff, false)));
        for (String file : files) {
            System.out.println(file);
        }
    }
}

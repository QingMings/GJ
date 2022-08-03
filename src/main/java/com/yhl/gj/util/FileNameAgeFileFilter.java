package com.yhl.gj.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import org.apache.commons.io.filefilter.AbstractFileFilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Objects;

/**
 * 根据文件名中的时间部分过滤文件名
 * 文件名格式 46610_2020_11_28_12_03_00.0.sat 、111111_2020_11_28_12_00_00.0.rae
 */
public class FileNameAgeFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = 5111488652911324270L;

    private final boolean acceptOlder;

    private final long cutoffMillis;

    /**
     * 文件名过滤器
     * 过滤早于指定日期的文件，根据文件名
     * 文件名示例 46610_2020_11_28_12_00_00.0.sat
     */
    public FileNameAgeFileFilter(final Date cutoffDate) {
        this(cutoffDate, true);
    }

    /**
     * 文件名过滤器
     *
     * @param cutoffDate
     * @param acceptOlder true 过滤更早的文件，false 过滤更晚的文件
     */
    public FileNameAgeFileFilter(final Date cutoffDate, final boolean acceptOlder) {
        this(cutoffDate.getTime(), acceptOlder);
    }

    /**
     * 使用一个参考文件 构造文件名过滤器 ，过滤更早的文件
     *
     * @param cutoffReference
     */
    public FileNameAgeFileFilter(final File cutoffReference) {
        this(cutoffReference, true);
    }

    /**
     * 文件名过滤器
     *
     * @param cutoffMillis
     * @param acceptOlder  true 过滤更早的文件  false 过滤更晚的文件
     */
    public FileNameAgeFileFilter(final long cutoffMillis, final boolean acceptOlder) {
        this.acceptOlder = acceptOlder;
        this.cutoffMillis = cutoffMillis;
    }

    /**
     * 根据参考文件 过滤文件名
     *
     * @param cutoffReference
     * @param acceptOlder     true 过滤更早的文件  false 过滤更晚的文件
     */
    public FileNameAgeFileFilter(final File cutoffReference, final boolean acceptOlder) {
        this(parseFileNameDate(cutoffReference), acceptOlder);
    }

    /**
     * 文件名过滤器 过滤更早的文件
     *
     * @param cutoffMillis
     */
    public FileNameAgeFileFilter(final long cutoffMillis) {
        this(cutoffMillis, true);
    }

    private static long parseFileNameDate(File cutoffReference) {
        String fileName = FileUtil.mainName(cutoffReference);
        int index = fileName.indexOf("_");
        String timePart = fileName.substring(index + 1);
        DateTime dateTime = DateUtil.parse(timePart, "yyyy_MM_dd_HH_mm_ss.S");
        return dateTime.getTime();
    }

    static FileVisitResult toFileVisitResult(final boolean accept, final Path path) {
        return accept ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
    }

    private static boolean isNewer(final Path file, final long timeMillis) {
        Objects.requireNonNull(file, "file");
        if (Files.notExists(file)) {
            return false;
        }
        return parseFileNameDate(file.toFile()) > timeMillis;
    }

    private static boolean isFileNameTimePartNewer(final File file, final long timeMillis) {
        Objects.requireNonNull(file, "file");
        return file.exists() && parseFileNameDate(file) > timeMillis;
    }

    @Override
    public boolean accept(final File file) {
        final boolean newer = isFileNameTimePartNewer(file, cutoffMillis);
        return acceptOlder != newer;
    }

    @Override
    public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
        final boolean newer;
        try {

            newer = isNewer(file, cutoffMillis);
        } catch (Exception e) {
            return handle(e);
        }
        return toFileVisitResult(acceptOlder != newer, file);
    }
}

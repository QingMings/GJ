package com.yhl.gj.fileWatch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileListener extends FileAlterationListenerAdaptor {

    private String taskName;

    @Override
    public void onStart(FileAlterationObserver observer) {

        log.info("onstart");
    }


    @Override
    public void onStop(FileAlterationObserver observer) {
        log.info("onStop");
    }

    @Override
    public void onFileCreate(File file) {
        log.info("onFileCreate");
    }

    @Override
    public void onFileDelete(File file) {
        log.info("onFileDelete");
    }

    @Override
    public void onFileChange(File file) {
        log.info("onFileChange");
    }
}

package com.yhl.gj.service;

import com.yhl.gj.commons.base.Response;
import com.yhl.gj.model.WarnResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yhl.gj.param.HistoryWarnResultRequest;
import com.yhl.gj.param.WarnResultRequest;

public interface WarnResultService extends IService<WarnResult> {


    Response warnResultQuery(WarnResultRequest request);

    Response historyWarnResultQuery(HistoryWarnResultRequest request);

    Response markedWarnResultToHistory(Long warnId);

    Response markedAllWarnResultToHistory();
}


package com.yhl.gj.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.param.HistoryWarnResultRequest;
import com.yhl.gj.param.WarnResultRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yhl.gj.mapper.WarnResultMapper;
import com.yhl.gj.model.WarnResult;
import com.yhl.gj.service.WarnResultService;

import java.util.List;

@Service
public class WarnResultServiceImpl extends ServiceImpl<WarnResultMapper, WarnResult> implements WarnResultService {

    @Override
    public Response warnResultQuery(WarnResultRequest request) {
        List<WarnResult> warnResults = this.lambdaQuery().in(WarnResult::getWarnLevel, request.getWarnLevels())
                .eq(WarnResult::getWarnStatus, 0)
                .orderByDesc(WarnResult::getWarnTimeUtc)
                .list();
        return Response.buildSucc(warnResults);
    }

    @Override
    public Response historyWarnResultQuery(HistoryWarnResultRequest request) {

        PageHelper.startPage(request.getCurrentPage(), request.getPageSize());
        List<WarnResult> warnResults = this.lambdaQuery().eq(WarnResult::getWarnStatus, 1)
                .like(StrUtil.isNotBlank(request.getTaskName()), WarnResult::getOrderId, request.getTaskName())
                .in(CollectionUtils.isNotEmpty(request.getSatelliteNames()), WarnResult::getSatelliteId, request.getSatelliteNames())
                .gt(ObjectUtil.isNotNull(request.getStartTime()), WarnResult::getWarnTimeUtc, request.getStartTime())
                .lt(ObjectUtil.isNotNull(request.getEndTime()), WarnResult::getWarnTimeUtc, request.getEndTime())
                .and(CollectionUtils.isNotEmpty(request.getLaserWarnLevels()) || CollectionUtils.isNotEmpty(request.getOrbitWarnLevels()), i ->
                        i.and(CollectionUtils.isNotEmpty(request.getLaserWarnLevels())
                                        , t -> t.eq(WarnResult::getWarnType, "laser")
                                                .in(WarnResult::getWarnLevel, request.getLaserWarnLevels()))
                                .or(CollectionUtils.isNotEmpty(request.getOrbitWarnLevels())
                                        , t2 -> t2.eq(WarnResult::getWarnType, "orbit")
                                                .in(WarnResult::getWarnLevel, request.getOrbitWarnLevels())))
                .orderByDesc(WarnResult::getWarnTimeUtc)
                .list();


        PageInfo<WarnResult> warnResultPage = PageInfo.of(warnResults);
        return Response.buildSucc(warnResultPage);
    }

    @Override
    public Response markedWarnResultToHistory(Long warnId) {
        boolean update = this.lambdaUpdate().set(WarnResult::getWarnStatus, 1)
                .eq(WarnResult::getId, warnId).update();
        return Response.buildSucc(update);
    }
}

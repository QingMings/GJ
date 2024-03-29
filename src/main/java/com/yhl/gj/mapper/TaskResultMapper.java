package com.yhl.gj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yhl.gj.model.TaskResult;
import com.yhl.gj.param.ResultQueryRequest;import org.apache.ibatis.annotations.Mapper;import org.apache.ibatis.annotations.Param;import java.util.List;

@Mapper
public interface TaskResultMapper extends BaseMapper<TaskResult> {
    List<TaskResult> queryTaskResultByCondition(@Param("param") ResultQueryRequest request);

    TaskResult getOneWithBlobs(@Param("id") Long id);

    List<String> getSatellites();

    String getMovesById(@Param("id") Long id);

    String getPathGbclXml(@Param("id") Long id);
}
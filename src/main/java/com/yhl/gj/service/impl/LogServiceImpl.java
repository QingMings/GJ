package com.yhl.gj.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yhl.gj.mapper.LogMapper;
import com.yhl.gj.model.Log;
import com.yhl.gj.service.LogService;
@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements LogService{

}

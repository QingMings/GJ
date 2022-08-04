package com.yhl.gj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yhl.gj.mapper.LogMapper;
import com.yhl.gj.model.Log;
import com.yhl.gj.service.LogService;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements LogService {

}




package com.yhl.gj.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yhl.gj.model.Test;
import com.yhl.gj.mapper.TestMapper;
import com.yhl.gj.service.TestService;

@Service
public class TestServiceImpl extends ServiceImpl<TestMapper, Test> implements TestService {

}


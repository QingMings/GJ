package com.yhl.gj.aop.reportlog;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

//@Aspect
//@Component
public class LogAspect {

    ThreadLocal<Long> currentTime = new ThreadLocal<>();
    @Pointcut("@within(com.yhl.gj.annotation.ReportLog)")
    public void logPointCut(){

    }

    @Around("logPointCut()")
    public Object logAround() throws  Throwable{
        currentTime.set(System.currentTimeMillis());
//        Object result =
        return "";
    }
}

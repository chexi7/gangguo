/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.mall4j.springboot.starter.log.aspect;

import cn.hutool.core.date.DateUtil;
import cn.mall4j.springboot.starter.log.annotation.MLog;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Optional;

import static com.alibaba.fastjson2.JSONWriter.Feature.MapSortField;

/**
 * {@link MLog} 日志打印 AOP 切面
 *
 * @author chen.ma
 * @github https://github.com/longtai-cn
 */
@Aspect
public class MLogPrintAspect {
    
    /**
     * 打印类或方法上的 {@link MLog}
     */
    @Around("@within(cn.mall4j.springboot.starter.log.annotation.MLog) || @annotation(cn.mall4j.springboot.starter.log.annotation.MLog)")
    public Object printMLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Logger log = LoggerFactory.getLogger(methodSignature.getDeclaringType());
        long startTime = System.currentTimeMillis();
        String beginTime = DateUtil.now();
        Object result = null;
        try {
            result = joinPoint.proceed();
        } finally {
            Method targetMethod = joinPoint.getTarget().getClass().getDeclaredMethod(methodSignature.getName(), methodSignature.getMethod().getParameterTypes());
            MLog logAnnotation = Optional.ofNullable(targetMethod.getAnnotation(MLog.class)).orElse(joinPoint.getTarget().getClass().getAnnotation(MLog.class));
            if (logAnnotation != null) {
                MLogPrint logPrint = new MLogPrint();
                logPrint.setBeginTime(beginTime);
                if (logAnnotation.input()) {
                    logPrint.setInputParams(buildInput(joinPoint));
                }
                if (logAnnotation.output()) {
                    logPrint.setOutputParams(result);
                }
                log.info("[{}] executeTime: {}ms, info: {}", joinPoint.getSignature().getName(), System.currentTimeMillis() - startTime, JSON.toJSONString(logPrint, MapSortField));
            }
        }
        return result;
    }
    
    private Object[] buildInput(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Object[] printArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if ((args[i] instanceof HttpServletRequest) || args[i] instanceof HttpServletResponse) {
                continue;
            }
            if (args[i] instanceof byte[]) {
                printArgs[i] = "byte array";
            } else if (args[i] instanceof MultipartFile) {
                printArgs[i] = "file";
            } else {
                printArgs[i] = args[i];
            }
        }
        return printArgs;
    }
    
    @Data
    private class MLogPrint {
        
        @JSONField
        private String beginTime;
        
        @JSONField(ordinal = 1)
        private Object[] inputParams;
        
        @JSONField(ordinal = 2)
        private Object outputParams;
    }
}

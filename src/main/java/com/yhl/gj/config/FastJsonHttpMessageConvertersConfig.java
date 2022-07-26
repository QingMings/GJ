package com.yhl.gj.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

@Configuration
public class FastJsonHttpMessageConvertersConfig {
    @Bean
    public FastJsonConfig fastJsonConfig() {
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        SerializerFeature writeMapNullValue = SerializerFeature.WriteMapNullValue;
        SerializerFeature WriteNullStringAsEmpty = SerializerFeature.WriteNullStringAsEmpty;
        SerializerFeature WriteNullNumberAsZero = SerializerFeature.WriteNullNumberAsZero;
        SerializerFeature WriteNullListAsEmpty = SerializerFeature.WriteNullListAsEmpty;
        fastJsonConfig.setSerializerFeatures(writeMapNullValue, WriteNullStringAsEmpty,
                WriteNullNumberAsZero, WriteNullListAsEmpty);
        return fastJsonConfig;
    }

    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters(
            @Qualifier("fastJsonConfig") FastJsonConfig fastJsonConfig) {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        fastConverter.setFastJsonConfig(fastJsonConfig);
        HttpMessageConverter<?> converter = fastConverter;
        return new HttpMessageConverters(converter);
    }
}
package com.yhl.gj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

@Configuration
public class RestTemplateConfig {
//    private StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8"));
//    private MappingJackson2HttpMessageConverter j = new MappingJackson2HttpMessageConverter();


}
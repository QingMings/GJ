package com.yhl.gj.interceptor;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.yhl.gj.commons.base.Response;
import com.yhl.gj.service.CommonBaseService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 校验token是否过期，token用户是有有效
 */
@Component
public class JWTInterceptor implements HandlerInterceptor {

    @Resource
    private CommonBaseService commonBaseService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        Response result = null;
        if (StrUtil.isNotEmpty(token)) {
            // 校验token是否过期
            DecodedJWT decode = JWT.decode(token);
            if (DateUtil.compare(decode.getExpiresAt(), DateUtil.date()) < 0) {
                result = Response.buildFail(401, "token过期");
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().print(JSON.toJSONString(result));
            }else {
                // 校验token ,从第三方获取用户信息。
                result = commonBaseService.verifyUser(token);
                if (result.isSuccess()) {
                    return true;
                }
            }
        }else {
            result = Response.buildFail(401, "token必填，请先登录");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(JSON.toJSONString(result));
        }
        return false;
    }
}

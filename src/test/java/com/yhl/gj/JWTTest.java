package com.yhl.gj;

import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

public class JWTTest {


    private String tokenExample = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1Nzc0NDk0ODksInVzZXJuYW1lIjoiYWRtaW4ifQ.CZFthevbrovUU1r-kho1tDGE6fKNCKuEXzdIi04B3gU";
    @Test
    public void   decodeTokenInfo(){
        DecodedJWT decode = JWT.decode(tokenExample);

        String dateTime = DateUtil.formatDateTime(decode.getExpiresAt());
        System.out.println(dateTime);

    }
}


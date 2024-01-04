package com.microservice.apigateway.helper;


import com.microservice.apigateway.constant.Constants;
import com.microservice.apigateway.filter.PrePostFilter;
import com.microservice.apigateway.utils.CommonUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
@Component
public class PreFilterCookieRefresher {
    private CommonUtils commonUtils;


    public void refreshSessionIfNeeded(String requestUrl,
                                       ServerHttpRequest serverHttpRequest,
                                       ServerHttpResponse serverHttpResponse) throws Exception {
        if (!PrePostFilter.isJwtByPassedUrl().contains(requestUrl)) {
            HttpCookie sessionCookie = CommonUtils.getSessionCookie(serverHttpRequest);
            HttpCookie jwtCookie = CommonUtils.getJwtCookie(serverHttpRequest);
            Jws<Claims> claims = CommonUtils.getJwtClaims(jwtCookie.getValue(), Constants.JWT_SECRET);
            if(claims != null && CommonUtils.isValidJwt(claims)){
                String newJwt = commonUtils.createJWTToken(Constants.JWT_SECRET,Constants.JWT_TIMEOUT, claims);

                serverHttpResponse.addCookie(CommonUtils.getSessionCookie(sessionCookie.getValue(), Constants.COOKIE_TIMEOUT));
                serverHttpResponse.addCookie(CommonUtils.getJwtCookie(newJwt, Constants.COOKIE_TIMEOUT));
            }
//            if(cookieDeleteAllowed(requestUrl)){
//                serverHttpResponse.addCookie(CommonUtils.getSessionCookie(sessionCookie.getValue(), 0));
//                serverHttpResponse.addCookie(CommonUtils.getJwtCookie(jwtCookie.getValue(), 0));
//            }
        } else {
            throw new Exception("UNAUTHORIZED");
        }


        }



}

package com.microservice.apigateway.filter;


import com.microservice.apigateway.helper.PreFilterCookieRefresher;
import com.microservice.apigateway.helper.SessionHelper;
import com.microservice.apigateway.utils.CommonUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;


@Component
public class PrePostFilter implements GlobalFilter, Ordered {
    private SessionHelper sessionHelper;
   // private JWTHelper jwtHelper;
    private CommonUtils commonUtils;
    private final PreFilterCookieRefresher preFilterCookieRefresher;


    @Autowired
    public PrePostFilter(SessionHelper sessionHelper,
                         //JWTHelper jwtHelper,
                         PreFilterCookieRefresher preFilterCookieRefresher) {
        this.sessionHelper = sessionHelper;
        //this.jwtHelper = jwtHelper;
        this.preFilterCookieRefresher=preFilterCookieRefresher;
    }

    public static boolean isWhiteListedUrl(String requestUrl) {
        List<String> whiteListed = new ArrayList<>();
        whiteListed.add("http://localhost:8084/api/passwordmanager-authservice/auth/user/register");
        whiteListed.add("http://localhost:8084/api/passwordmanager-authservice/auth/user/sendOtp");
       // return whiteListed;
        return whiteListed.stream().anyMatch(requestUrl::contains);
    }

    public static List<String> isJwtByPassedUrl() {
        List<String> jwtByPassUrls = new ArrayList<>();
        jwtByPassUrls.add("http://localhost:8084/api/passwordmanager-authservice/auth/user/validateOtp");
        return jwtByPassUrls;
    }

    @Override
    @SneakyThrows
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        String requestUrl = serverHttpRequest.getURI().toString();
        if (isWhiteListedUrl(requestUrl)) {
            serverHttpRequest = sessionHelper.createSession(serverHttpRequest);
            ServerWebExchange finalExchange = exchange.mutate().request(serverHttpRequest).build();
            return chain.filter(finalExchange);
        } else {
            validateRequest(serverHttpRequest, requestUrl);
            ServerHttpResponse serverHttpResponse = exchange.getResponse();
            preFilterCookieRefresher.refreshSessionIfNeeded(requestUrl, serverHttpRequest, serverHttpResponse);

            ServerWebExchange finalExchange =
                    exchange.mutate().request(serverHttpRequest).response(serverHttpResponse).build();
            return chain.filter(finalExchange);

        }
    }



    @Override
    public int getOrder() {
        return -1;
    }

    public void validateRequest(ServerHttpRequest serverHttpRequest, String requestUrl) throws Exception {
        HttpCookie sessionCookie =  CommonUtils.getSessionCookie(serverHttpRequest);
        validateSession(sessionCookie);
        if (!isJwtByPassedUrl().contains(requestUrl)) {
            HttpCookie jwtCookie = CommonUtils.getJwtCookie(serverHttpRequest);
            validateJwt(jwtCookie);
        }


    }

    private void validateJwt(HttpCookie jwtCookie) throws Exception {
        if (jwtCookie.equals(null) || StringUtils.isEmpty(jwtCookie.getValue())) {
            throw new Exception("Invalid JWT Cookie");
        }
    }


    private void validateSession(HttpCookie sessionCookie) throws Exception {
        if (sessionCookie.equals(null) || StringUtils.isEmpty(sessionCookie.getValue())) {
            throw new Exception("Invalid session Cookie");
        }

    }




}

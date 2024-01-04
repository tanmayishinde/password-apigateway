package com.microservice.apigateway.helper;

import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SessionHelper {

    public ServerHttpRequest createSession(ServerHttpRequest serverHttpRequest) {
        UUID uuid = UUID.randomUUID();
        String sessionId = uuid.toString();

        serverHttpRequest.mutate().headers((httpHeaders) -> {
            String session = new HttpCookie("session-id", sessionId).toString();
            httpHeaders.set("Cookie", session);
        }).build();
        return serverHttpRequest;
    }


}

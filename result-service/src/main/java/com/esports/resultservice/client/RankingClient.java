package com.esports.resultservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Component
public class RankingClient {
    private final RestTemplate http;
    private final String url;
    public RankingClient(@Value("${ranking.service.url:http://localhost:8089}") String url) {
        this.url = url;
        var factory = new SimpleClientHttpRequestFactory(); factory.setConnectTimeout(3000); factory.setReadTimeout(15000);
        http = new RestTemplate(factory);
    }
    public void recalcular(Long torneoId, String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) throw new IllegalArgumentException("Autenticación requerida");
        var headers = new HttpHeaders(); headers.set("Authorization", authorization);
        http.exchange(url + "/api/rankings/torneo/" + torneoId + "/recalcular", HttpMethod.POST, new HttpEntity<>(headers), Void.class);
    }
}

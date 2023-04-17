package com.eme22.bolo.utils;

import com.eme22.bolo.tenor.model.TenorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GifSearcher {

    private final String API_ENDPOINT = "https://tenor.googleapis.com/v2/search";
    private final String API_KEY;
    private final String CLIENT_KEY;

    private final RestTemplate restTemplate;

    public GifSearcher(RestTemplateBuilder restTemplateBuilder, @Value("${tenor.key}") String tenorApi, @Value("${tenor.user}") String tenorClientKey) {
        this.restTemplate = restTemplateBuilder.build();
        this.API_KEY = tenorApi;
        this.CLIENT_KEY = tenorClientKey;
    }

    public TenorResponse searchGifs(String query, int limit) {
        String url = API_ENDPOINT + "?q=" + query + "&key=" + API_KEY + "&client_key=" + CLIENT_KEY + "&limit=" + limit;
        return restTemplate.getForObject(url, TenorResponse.class);
    }

}
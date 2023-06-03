package com.eme22.bolo.controller;

import com.eme22.bolo.model.ServerStats;
import com.eme22.bolo.model.Stats;
import org.reactivestreams.Publisher;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {

    private final RSocketRequester rSocketRequester;

    public StatsController(RSocketRequester rSocketRequester) {
        this.rSocketRequester = rSocketRequester;
    }

    @GetMapping(value = "/stats/server/{id}")
    public Publisher<ServerStats> getServerStats(@PathVariable("id") Long id) {
        return rSocketRequester
          .route("serverStatsById.{id}", id)
          .retrieveMono(ServerStats.class);
    }

    @GetMapping(value = "/stats/server/all")
    public Publisher<ServerStats> getAllServersStats() {
        return rSocketRequester
                .route("serverStats.get")
                .retrieveFlux(ServerStats.class);
    }

    @GetMapping(value = "/stats/global/all")
    public Publisher<Stats> getGlobalStats() {
        return rSocketRequester
                .route("globalStats.get")
                .retrieveFlux(Stats.class);
    }

    @GetMapping(value = "/stats/global/{name}")
    public Publisher<Stats> getGlobalStatsByName(@PathVariable("name") String name) {
        return rSocketRequester
                .route("globalStatsByName.{name}", name)
                .retrieveMono(Stats.class);
    }
}
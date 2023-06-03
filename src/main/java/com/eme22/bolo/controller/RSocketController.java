package com.eme22.bolo.controller;

import com.eme22.bolo.model.ServerStats;
import com.eme22.bolo.model.Stats;
import com.eme22.bolo.repository.ServerStatsRepository;
import com.eme22.bolo.repository.StatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class RSocketController {

    private final ServerStatsRepository serverStatsRepository;
    private final StatsRepository statsRepository;

    @Autowired
    public RSocketController(ServerStatsRepository serverStatsRepository, StatsRepository statsRepository) {
        this.serverStatsRepository = serverStatsRepository;
        this.statsRepository = statsRepository;
    }

    @MessageMapping("serverStatsById.{id}")
    public Mono<ServerStats> getServerStatsById(@PathVariable Long id) {
        return Mono.justOrEmpty(serverStatsRepository.findById(id));
    }

    @MessageMapping("serverStats.get")
    public Flux<ServerStats> getServerStats() {
        return Flux.fromIterable(serverStatsRepository.findAll());
    }

    @MessageMapping("globalStats.get")
    public Flux<Stats> getGlobalStats() {
        return Flux.fromIterable(statsRepository.findAll());
    }
    @MessageMapping("globalStatsByName.{name}")
    public Mono<Stats> getGlobalStatsByName(@PathVariable String name) {
        return Mono.justOrEmpty(statsRepository.findById(name));
    }
}

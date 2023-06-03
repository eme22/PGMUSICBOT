package com.eme22.bolo.stats;

import com.eme22.bolo.model.ServerStats;
import com.eme22.bolo.model.Stats;
import com.eme22.bolo.repository.ServerStatsRepository;
import com.eme22.bolo.repository.StatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class StatsService {

    private final StatsRepository statsRepository;

    private final ServerStatsRepository serverStatsRepository;

    @Autowired
    public StatsService(StatsRepository statsRepository, ServerStatsRepository serverStatsRepository) {
        this.statsRepository = statsRepository;
        this.serverStatsRepository = serverStatsRepository;
    }

    public Mono<Stats> saveGlobalStat(String name, Long value) {
        return Mono.justOrEmpty(statsRepository.save(new Stats(name, value)));
    }

    public Optional<Stats> getGlobalStat(String name) {
            return statsRepository.findById(name);
    }

    public Flux<Stats> getGlobalStats() {
        return Flux.fromIterable(statsRepository.findAll());
    }

    public void updateCommandsUsed(Long guildId) {
        try {
            serverStatsRepository.updateCommandsUsedById(guildId);
            statsRepository.updateStat("COMMANDS_USED");
        } catch (Exception ignored) {}
    }

    public void updateImagesSend(Long guildId) {
        try {
            serverStatsRepository.updateImagesSendById(guildId);
            statsRepository.updateStat("IMAGES_SEND");
        } catch (Exception ignored) {}
    }

    public void updateMemesSend(Long guildId) {
        try {
            serverStatsRepository.updateMemesSendById(guildId);
            statsRepository.updateStat("MEMES_SEND");
        } catch (Exception ignored) {}
    }

    public void updateSongsPlayed(Long guildId) {
        try {
            serverStatsRepository.updateSongsPlayedById(guildId);
            statsRepository.updateStat("SONGS_PLAYED");
        } catch (Exception ignored) {}
    }

    public void updateAnals(Long guildId) {
        try {
            serverStatsRepository.updateAnalById(guildId);
            statsRepository.updateStat("ANAL");
        } catch (Exception ignored) {}
    }

    public void updateKisses(Long guildId) {
        try {
            serverStatsRepository.updateKissesById(guildId);
            statsRepository.updateStat("KISS");
        } catch (Exception ignored) {}
    }

    public void updateSlaps(Long guildId) {
        try {
            serverStatsRepository.updateSlapsById(guildId);
            statsRepository.updateStat("SLAPS");
        } catch (Exception ignored) {}
    }

    public Optional<ServerStats> getServerStat(Long guildId) {
        return serverStatsRepository.findById(guildId);
    }

}

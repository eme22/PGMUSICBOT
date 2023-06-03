package com.eme22.bolo.repository;

import com.eme22.bolo.model.ServerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ServerStatsRepository extends JpaRepository<ServerStats, Long> {
    @Transactional
    @Modifying
    @Query("update EMBOT_SERVER_STATS e set e.imagesSend = e.imagesSend + 1 where e.id = ?1")
    int updateImagesSendById(Long id);
    @Transactional
    @Modifying
    @Query("update EMBOT_SERVER_STATS e set e.memesSend = e.memesSend + 1 where e.id = ?1")
    int updateMemesSendById(Long id);
    @Transactional
    @Modifying
    @Query("update EMBOT_SERVER_STATS e set e.songsPlayed = e.songsPlayed + 1 where e.id = ?1")
    int updateSongsPlayedById(Long id);
    @Transactional
    @Modifying
    @Query("update EMBOT_SERVER_STATS e set e.anal = e.anal + 1 where e.id = ?1")
    int updateAnalById(Long id);
    @Transactional
    @Modifying
    @Query("update EMBOT_SERVER_STATS e set e.kisses = e.kisses + 1 where e.id = ?1")
    int updateKissesById(Long id);
    @Transactional
    @Modifying
    @Query("update EMBOT_SERVER_STATS e set e.slaps = e.slaps + 1 where e.id = ?1")
    int updateSlapsById(Long id);
    @Transactional
    @Modifying
    @Query("update EMBOT_SERVER_STATS e set e.commandsUsed = e.commandsUsed + 1 where e.id = ?1")
    void updateCommandsUsedById(Long id);
}
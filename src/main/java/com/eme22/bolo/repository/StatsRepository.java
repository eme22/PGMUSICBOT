package com.eme22.bolo.repository;

import com.eme22.bolo.model.Stats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface StatsRepository extends JpaRepository<Stats, String> {

    @Transactional
    @Modifying
    @Query("update EMBOT_STATS e set e.value = e.value + 1 where e.name = ?1")
    int updateStat(String name);

    @Transactional
    @Modifying
    @Query("update EMBOT_STATS e set e.value = ?2 where e.name = ?1")
    int updateStat(String name, Long value);

}
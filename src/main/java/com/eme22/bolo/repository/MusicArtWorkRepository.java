package com.eme22.bolo.repository;

import com.eme22.bolo.model.MusicArtWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface MusicArtWorkRepository extends JpaRepository<MusicArtWork, Long> {
    @Transactional
    @Modifying
    @Query("update EMBOT_MUSIC_ARTWORKS e set e.url = ?1 where upper(e.artist) = upper(?2)")
    int updateUrlByArtistIgnoreCase(String url, String artist);
    long deleteByArtistIgnoreCase(@NonNull String artist);

    Optional<MusicArtWork> findByArtistIgnoreCase(String artist);

}

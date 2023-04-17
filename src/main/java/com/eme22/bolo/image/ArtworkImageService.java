package com.eme22.bolo.image;

import com.eme22.bolo.model.MusicArtWork;
import com.eme22.bolo.repository.MusicArtWorkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArtworkImageService {

    private final MusicArtWorkRepository repository;


    @Autowired
    public ArtworkImageService(MusicArtWorkRepository repository) {
        this.repository = repository;
    }

    public Optional<MusicArtWork> getArtwork(String artist) {
        return repository.findByArtistIgnoreCase(artist);
    }

    public MusicArtWork addArtWork(MusicArtWork artist) {
        return repository.save(artist);
    }

    public void updateArtWork(MusicArtWork artist) {
        repository.updateUrlByArtistIgnoreCase(artist.getUrl(), artist.getArtist());
    }

    public void removeMusicArtwork(String artist) {
        repository.deleteByArtistIgnoreCase(artist);
    }

}

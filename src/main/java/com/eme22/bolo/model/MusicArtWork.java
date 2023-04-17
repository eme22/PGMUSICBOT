package com.eme22.bolo.model;

import lombok.*;

import javax.persistence.*;

@Entity(name = "EMBOT_MUSIC_ARTWORKS")
@Table(indexes = {
        @Index(name = "idx_musicartwork_unq", columnList = "MUSIC_ARTWORKS_ARTIST", unique = true)
})
@NamedQueries({
        @NamedQuery(name = "MusicArtWork.updateUrlByArtistIgnoreCase", query = "update EMBOT_MUSIC_ARTWORKS e set e.url = :url where upper(e.artist) = upper(:artist)")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class MusicArtWork {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "MUSIC_ARTWORKS_ID", nullable = false)
    private Long id;

    @Column(name = "MUSIC_ARTWORKS_ARTIST")
    private String artist;

    @Column(name = "MUSIC_ARTWORKS_URL")
    private String url;

    @Column(name = "MUSIC_ARTWORKS_USER")
    private Long submitedBy;

}

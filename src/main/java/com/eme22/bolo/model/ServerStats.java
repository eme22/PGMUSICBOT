package com.eme22.bolo.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "EMBOT_SERVER_STATS")
public class ServerStats {

    @Id
    @Column(name = "SERVER_ID", nullable = false)
    private Long id;

    @Column(name = "SERVER_COMMANDS", nullable = false)
    private Long commandsUsed;

    @Column(name = "SERVER_IMAGES", nullable = false)
    private Long imagesSend;

    @Column(name = "SERVER_MEMES", nullable = false)
    private Long memesSend;

    @Column(name = "SERVER_SONGS", nullable = false)
    private Long songsPlayed;

    @Column(name = "SERVER_ANAL", nullable = false)
    private Long anal;

    @Column(name = "SERVER_KISSES", nullable = false)
    private Long kisses;

    @Column(name = "SERVER_SLAPS", nullable = false)
    private Long slaps;


}

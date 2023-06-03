package com.eme22.bolo.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigInteger;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "EMBOT_STATS")
public class Stats {
    @Id
    @Column(name = "STAT_NAME", nullable = false)
    private String name;

    @Column(name = "STAT_VALUE", nullable = false)
    private Long value;

}

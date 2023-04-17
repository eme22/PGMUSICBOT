
package com.eme22.bolo.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "EMBOT_SERVER_MEMEIMAGE_DETAIL")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class MemeImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "MEMEIMAGE_ID", nullable = false)
    private Long id;

    @Column(name = "MEMEIMAGE_MESSAGE")
    private String message;
    @Column(name = "MEMEIMAGE_MEME")
    private String meme;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        MemeImage memeImage = (MemeImage) o;
        return getId() != null && Objects.equals(getId(), memeImage.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

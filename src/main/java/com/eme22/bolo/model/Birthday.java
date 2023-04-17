package com.eme22.bolo.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity(name = "EMBOT_SERVER_BIRTHDAY_DETAIL")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Birthday {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "BIRTHDAY_ID", nullable = false)
    private Long id;

    @Column(name = "BIRTHDAY_MESSAGE")
    private String message;

    @Column(name = "BIRTHDAY_DATE")
    private Date date;

    @Column(name = "BIRTHDAY_USERID")
    private Long user;

    @Column(name = "BIRTHDAY_ENABLED")
    private boolean active;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Birthday birthday = (Birthday) o;
        return getId() != null && Objects.equals(getId(), birthday.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

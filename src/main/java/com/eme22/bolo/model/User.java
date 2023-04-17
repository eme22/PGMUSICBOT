package com.eme22.bolo.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity(name = "EMBOT_USER")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "USER_ID", nullable = false)
    private Long id;

    @Column(name = "USER_NAME", nullable = false)
    String name;

    @Column(name = "USER_ROLE", nullable = false)
    @Enumerated(EnumType.STRING)
    UserRole role;

    @Column(name = "USER_LASTLOGINDATE", nullable = false)
    Date lastLogin;

    @Column(name = "USER_REGISTERDATE", nullable = false)
    Date registerDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

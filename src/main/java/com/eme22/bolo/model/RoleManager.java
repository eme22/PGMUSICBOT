
package com.eme22.bolo.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity(name = "EMBOT_SERVER_ROLEMANAGER_DETAIL")
public class RoleManager {

    @Id
    @Column(name = "ROLEMANAGER_ID")
    private Long id;

    @ElementCollection
    @CollectionTable(name = "EMBOT_SERVER_ROLEMANAGER_EMOJI")
    private Map<String, String> emoji;

    @Column(name = "ROLEMANAGER_TOGGLEDMODE")
    private boolean toggled;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        RoleManager that = (RoleManager) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

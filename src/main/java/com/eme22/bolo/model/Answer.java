
package com.eme22.bolo.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "EMBOT_SERVER_POLL_ANSWER_DETAIL")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ANSWER_ID", nullable = false)
    private Long id;

    @Column(name = "ANSWER_DATA")
    private String answer;
    @CollectionTable(name = "EMBOT_SERVER_POLL_ANSWER_VOTE")
    @ElementCollection
    private final Set<Long> votes = new HashSet<>();

    public Answer(String asString) {
        this.answer = asString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Answer answer = (Answer) o;
        return getId() != null && Objects.equals(getId(), answer.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void addVote(long userId) {
        votes.add(userId);
    }

    public void removeVote(long userId) {
        votes.remove(userId);
    }
}


package com.eme22.bolo.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "EMBOT_SERVER_POLL_DETAIL")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Log4j2
public class Poll {

    @Id
    @Column(name = "POLL_ID")
    private Long id;
    @Column(name = "POLL_QUESTION")
    private String question;
    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "EMBOT_SERVER_POLL_ANSWER",
            joinColumns = @JoinColumn(name = "POLL_ID"),
            inverseJoinColumns = @JoinColumn(name = "ANSWER_ID"))
    @ToString.Exclude
    private List<Answer> answers = new ArrayList<>();

    public void addAnswer(Answer answer){
        answers.add(answer);
    }


    public Integer getAllVoteCount(){
        return answers.stream().mapToInt(answer -> answer.getVotes().size()).sum();
    }

    public void addVoteToAnswer(int answer,Long userId){
        Answer answer1 = answers.get(answer);
        if (answer1 == null) return;
        answer1.addVote(userId);
    }

    public void removeVoteFromAnswer(int answer,Long userId){
        Answer answer1 = answers.get(answer);
        if (answer1 == null) return;
        answer1.getVotes().remove(userId);
    }


    public Integer getUserAnswer(Long userId){
        Answer answer = answers.stream().filter(answer1 -> answer1.getVotes().contains(userId)).findFirst().orElse(null);
        return answers.indexOf(answer);
    }

    public boolean isUserParticipating(Long userId){

        log.info("User "+ userId );
        log.info("Poll "+ this );
        log.info("Answers "+ answers);

        boolean contains = answers.stream().anyMatch( answer -> answer.getVotes().contains(userId));

        log.info("Result "+ contains);

        return contains;
    }

    public boolean isUserParticipatingInAnswer(int answer, Long userId){
        return answers.get(answer).getVotes().contains(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Poll poll = (Poll) o;
        return getId() != null && Objects.equals(getId(), poll.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

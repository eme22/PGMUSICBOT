
package com.eme22.bolo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "question",
    "answers"
})

@AllArgsConstructor
@With
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Poll {

    @JsonProperty("id")
    private long id;
    @JsonProperty("question")
    private String question;
    @JsonProperty("answers")
    private List<Answer> answers = new ArrayList<>();

    public void addAnswer(Answer answer){
        answers.add(answer);
    }

    @JsonIgnore
    public Integer getAllVoteCount(){
        return answers.stream().mapToInt(answer -> answer.votes.size()).sum();
    }

    public void addVoteToAnswer(int answer,Long userId){
        Answer answer1 = answers.get(answer);
        if (answer1 == null) return;
        answer1.getVotes().add(userId);
    }

    public void removeVoteFromAnswer(int answer,Long userId){
        Answer answer1 = answers.get(answer);
        if (answer1 == null) return;
        answer1.getVotes().remove(userId);
    }

    @JsonIgnore
    public Integer getUserAnswer(Long userId){
        Answer answer = answers.stream().filter(answer1 -> answer1.votes.contains(userId)).findFirst().orElse(null);
        return answers.indexOf(answer);
    }

    public boolean isUserParticipating(Long userId){
        return answers.stream().anyMatch( answer -> answer.votes.contains(userId));
    }

    public boolean isUserParticipatingInAnswer(int answer, Long userId){
        return answers.get(answer).votes.contains(userId);
    }

}

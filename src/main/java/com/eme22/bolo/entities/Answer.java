
package com.eme22.bolo.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "answer",
    "votes"
})
@AllArgsConstructor
@With
@Getter
@Setter
@NoArgsConstructor
public class Answer {

    @JsonProperty("answer")
    public String answer;
    @JsonProperty("votes")
    public HashSet<Long> votes = new HashSet<>();

}

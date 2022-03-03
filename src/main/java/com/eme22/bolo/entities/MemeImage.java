
package com.eme22.bolo.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "message",
    "meme"
})

@AllArgsConstructor
@With
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class MemeImage {

    @JsonProperty("message")
    public String message;
    @JsonProperty("meme")
    public String meme;

}

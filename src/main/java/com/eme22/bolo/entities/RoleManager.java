
package com.eme22.bolo.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.HashMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "message",
        "emojilist"
})

@AllArgsConstructor
@With
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class RoleManager {

    @JsonProperty("id")
    private long id;
    @JsonProperty("message")
    private String message;
    @JsonProperty("emojilist")
    private HashMap<String, String> emoji;

}

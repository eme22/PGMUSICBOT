package com.eme22.bolo.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@With
@Getter
@Setter
@NoArgsConstructor
public class Birthday {

    @JsonProperty("message")
    private String message;

    @JsonProperty("date")
    private Date date;

    @JsonProperty("user")
    private long user;

    @JsonProperty("active")
    private boolean active;

}

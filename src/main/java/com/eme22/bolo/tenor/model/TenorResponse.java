
package com.eme22.bolo.tenor.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "results",
    "next"
})
@Generated("jsonschema2pojo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenorResponse implements Serializable
{

    @JsonProperty("results")
    public List<Result> results = new ArrayList<Result>();
    @JsonProperty("next")
    public String next;
    private final static long serialVersionUID = -7429370393738408768L;

}

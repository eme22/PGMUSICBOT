
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
    "url",
    "duration",
    "preview",
    "dims",
    "size"
})
@Generated("jsonschema2pojo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nanogif implements Serializable
{

    @JsonProperty("url")
    public String url;
    @JsonProperty("duration")
    public Integer duration;
    @JsonProperty("preview")
    public String preview;
    @JsonProperty("dims")
    public List<Integer> dims = new ArrayList<Integer>();
    @JsonProperty("size")
    public Integer size;
    private final static long serialVersionUID = -6746084457464955124L;

}

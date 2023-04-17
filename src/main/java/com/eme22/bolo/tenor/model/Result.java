
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
    "id",
    "title",
    "media_formats",
    "created",
    "content_description",
    "itemurl",
    "url",
    "tags",
    "flags",
    "hasaudio"
})
@Generated("jsonschema2pojo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result implements Serializable
{

    @JsonProperty("id")
    public String id;
    @JsonProperty("title")
    public String title;
    @JsonProperty("media_formats")
    public MediaFormats mediaFormats;
    @JsonProperty("created")
    public Float created;
    @JsonProperty("content_description")
    public String contentDescription;
    @JsonProperty("itemurl")
    public String itemurl;
    @JsonProperty("url")
    public String url;
    @JsonProperty("tags")
    public List<String> tags = new ArrayList<String>();
    @JsonProperty("flags")
    public List<String> flags = new ArrayList<String>();
    @JsonProperty("hasaudio")
    public Boolean hasaudio;
    private final static long serialVersionUID = 6449752928225364357L;

}

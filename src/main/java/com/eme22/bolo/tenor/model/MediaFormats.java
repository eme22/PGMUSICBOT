
package com.eme22.bolo.tenor.model;

import java.io.Serializable;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "gifpreview",
    "nanowebm",
    "gif",
    "tinymp4",
    "mp4",
    "tinywebm",
    "nanogifpreview",
    "nanogif",
    "tinygif",
    "webm",
    "nanomp4",
    "tinygifpreview",
    "mediumgif",
    "loopedmp4"
})
@Generated("jsonschema2pojo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaFormats implements Serializable
{

    @JsonProperty("gifpreview")
    private Gifpreview gifpreview;
    @JsonProperty("nanowebm")
    private Nanowebm nanowebm;
    @JsonProperty("gif")
    private Gif gif;
    @JsonProperty("tinymp4")
    private Tinymp4 tinymp4;
    @JsonProperty("mp4")
    private Mp4 mp4;
    @JsonProperty("tinywebm")
    private Tinywebm tinywebm;
    @JsonProperty("nanogifpreview")
    private Nanogifpreview nanogifpreview;
    @JsonProperty("nanogif")
    private Nanogif nanogif;
    @JsonProperty("tinygif")
    private Tinygif tinygif;
    @JsonProperty("webm")
    private Webm webm;
    @JsonProperty("nanomp4")
    private Nanomp4 nanomp4;
    @JsonProperty("tinygifpreview")
    private Tinygifpreview tinygifpreview;
    @JsonProperty("mediumgif")
    private Mediumgif mediumgif;
    @JsonProperty("loopedmp4")
    private Loopedmp4 loopedmp4;
    private final static long serialVersionUID = -2954992517247138351L;

}

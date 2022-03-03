
package com.eme22.bolo.settings;

import com.eme22.bolo.entities.MemeImage;
import com.eme22.bolo.entities.Poll;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.jagrosh.jdautilities.command.GuildSettingsProvider;
import lombok.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "text_channel_id",
    "voice_channel_id",
    "dj_role_id",
    "admin_role_id",
    "volume",
    "default_playlist",
    "repeat_mode",
    "prefix",
    "skip_ratio",
    "bienvenidas_channel_id",
    "bienvenidas_channel_image",
    "despedidas_channel_id",
    "despedidas_channel_image",
    "image_only_channels_ids",
    "meme_images",
    "polls"
})

@AllArgsConstructor
@NoArgsConstructor
@With
@Getter
@Setter
public class Settings implements GuildSettingsProvider {

    @JsonIgnore
    private SettingsManager manager;
    @JsonProperty("text_channel_id")
    private long textChannelId;
    @JsonProperty("voice_channel_id")
    private long voiceChannelId;
    @JsonProperty("dj_role_id")
    private long djRoleId;
    @JsonProperty("admin_role_id")
    private long adminRoleId;
    @JsonProperty("volume")
    private int volume;
    @JsonProperty("default_playlist")
    private String defaultPlaylist;
    @JsonProperty("repeat_mode")
    private RepeatMode repeatMode;
    @JsonProperty("prefix")
    private String prefix;
    @JsonProperty("skip_ratio")
    private double skipRatio;
    @JsonProperty("bienvenidas_channel_id")
    private long bienvenidasChannelId;
    @JsonProperty("bienvenidas_channel_image")
    private String bienvenidasChannelImage;
    @JsonProperty("despedidas_channel_id")
    private long despedidasChannelId;
    @JsonProperty("despedidas_channel_image")
    private String despedidasChannelImage;
    @JsonProperty("image_only_channels_ids")
    private List<Long> imageOnlyChannelsIds;
    @JsonProperty("meme_images")
    private List<MemeImage> memeImages;
    @JsonProperty("polls")
    private List<Poll> polls;

    public void addPollForGuild(Long messageId, Poll poll){
        if (this.polls.stream().anyMatch( poll1 -> poll1.getId() == messageId))
            return;
        this.polls.add(poll.withId(messageId));
    }

    public void removePollFromGuild(Long messageId){
        this.polls.removeIf( poll -> poll.getId() == messageId);
    }

    public TextChannel getTextChannel(Guild guild) {
        return guild == null ? null : guild.getTextChannelById(textChannelId);
    }

    public VoiceChannel getVoiceChannel(Guild guild) {
        return guild == null ? null : guild.getVoiceChannelById(voiceChannelId);
    }

    public TextChannel getHelloChannel(Guild guild) {
        return guild == null ? null : guild.getTextChannelById(bienvenidasChannelId);
    }

    public TextChannel getGoodbyeChannel(Guild guild) {
        return guild == null ? null : guild.getTextChannelById(despedidasChannelId);
    }

    public ArrayList<TextChannel> getOnlyImageChannels(Guild guild) {

        ArrayList<TextChannel> channels = new ArrayList<>();
        this.imageOnlyChannelsIds.forEach(channelid -> channels.add(guild.getTextChannelById(String.valueOf(channelid))));
        return channels;
    }

    public void addOnlyImageChannels(TextChannel onlyImageChannel) {

        Long channel = onlyImageChannel.getIdLong();

        if (imageOnlyChannelsIds.contains(channel))
            return;

        this.imageOnlyChannelsIds.add(channel);
    }

    public void removeFromOnlyImageChannels(TextChannel onlyImageChannel) {

        Long channel = onlyImageChannel.getIdLong();

        imageOnlyChannelsIds.removeIf(element -> element.equals(channel));
    }

    @JsonIgnore
    public MemeImage getRandomMemeImages() {
        int rand = new Random().nextInt(this.memeImages.size());
        return this.memeImages.get(rand);
    }

    public void addToMemeImages(String message, String imageLink) {

        MemeImage meme = new MemeImage(message, imageLink);
        if (this.memeImages.contains(meme))
            return;

        this.memeImages.add(meme);
    }

    public void deleteFromMemeImages(int position) {
        this.memeImages.remove(position);
    }

    public void clearServerData(Guild guild) {
        this.textChannelId = 0;
        this.voiceChannelId = 0;
        this.djRoleId = 0;
        this.volume = 0;
        this.defaultPlaylist = null;
        this.repeatMode = null;
        this.prefix = null;
        this.skipRatio = 0;
        this.adminRoleId =0;
        this.bienvenidasChannelId = 0;
        this.bienvenidasChannelImage = null;
        this.imageOnlyChannelsIds = null;
        this.memeImages = null;
        this.despedidasChannelId = 0;
        this.despedidasChannelImage = null;
        this.polls = null;
        this.manager.deleteSettings(guild.getId());
    }

    @JsonIgnore
    public Role getAdminRoleId(Guild guild) {
        return guild == null ? null : guild.getRoleById(adminRoleId);
    }

    @JsonIgnore
    public Role getDJRoleId(Guild guild) {
        return guild == null ? null : guild.getRoleById(djRoleId);
    }

    @JsonIgnore
    @Nullable
    @Override
    public Collection<String> getPrefixes() {
        return prefix == null ? Collections.emptySet() : Collections.singleton(prefix);
    }
}

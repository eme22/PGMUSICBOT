/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eme22.bolo.audio;

import com.dunctebot.sourcemanagers.DuncteBotSources;
import com.eme22.bolo.Bot;
import com.eme22.bolo.utils.FormatUtil;
import com.eme22.bolo.utils.GifSearcher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.topisenpai.lavasrc.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
@Component
@Log4j2
public class PlayerManager extends DefaultAudioPlayerManager
{
    @Setter
    private Bot bot;


    private final String user;


    private final String password;


    private final long maxSeconds;


    private final boolean stayInChannel;


    private final String successEmoji;

    private final long owner;

    private final boolean npImages;

    private final String spotifyUserId;

    private final String spotifySecret;

    @Autowired
    public PlayerManager(@Value("${youtube.user}") String user, @Value("${youtube.password}") String password, @Value("${config.maxseconds}") long maxSeconds, @Value("${config.stayinchannel}") boolean stayInChannel, @Value("${config.success}") String successEmoji, @Value("${config.nowplayingimages}") boolean npImages, @Value("${spotify.userid}")  String spotifyUserId, @Value("${config.owner}") long owner, @Value("${spotify.secret}") String spotifySecret) {
        this.user = user;
        this.password = password;
        this.maxSeconds = maxSeconds;
        this.stayInChannel = stayInChannel;
        this.successEmoji = successEmoji;
        this.spotifyUserId = spotifyUserId;
        this.spotifySecret = spotifySecret;
        this.npImages = npImages;
        this.owner = owner;
    }
    
    public void init()
    {
        //TransformativeAudioSourceManager.createTransforms(bot.getConfig().getTransforms()).forEach(this::registerSourceManager);
        this.registerRemoteSources(this);
        AudioSourceManagers.registerLocalSource(this);
        source(YoutubeAudioSourceManager.class).setPlaylistPageCount(10);
    }
    
    public Bot getBot()
    {
        return bot;
    }
    
    public boolean hasHandler(Guild guild)
    {
        return guild.getAudioManager().getSendingHandler()!=null;
    }
    
    public AudioHandler setUpHandler(Guild guild)
    {
        AudioHandler handler;
        if(guild.getAudioManager().getSendingHandler()==null)
        {
            AudioPlayer player = createPlayer();
            int volume = bot.getSettingsManager().getSettings(guild).getVolume();
            log.info("Starting Volume:" + volume);
            player.setVolume(volume);
            handler = new AudioHandler(this, player, guild.getIdLong(), stayInChannel, successEmoji, npImages, owner);
            //handler = new AudioHandler(this, guild, player);
            player.addListener(handler);
            guild.getAudioManager().setSendingHandler(handler);
        }
        else
            handler = (AudioHandler) guild.getAudioManager().getSendingHandler();
        return handler;
    }

    private void registerRemoteSources(AudioPlayerManager playerManager) {
        registerRemoteSources(playerManager, MediaContainerRegistry.DEFAULT_REGISTRY);
    }

    private void registerRemoteSources(AudioPlayerManager playerManager, MediaContainerRegistry containerRegistry) {
        playerManager.registerSourceManager(new SpotifySourceManager(null, spotifyUserId, spotifySecret, "US", playerManager));
        DuncteBotSources.registerAll(playerManager, "es-MX");
        playerManager.registerSourceManager(new YoutubeAudioSourceManager(true, user, password));
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new BeamAudioSourceManager());
        playerManager.registerSourceManager(new GetyarnAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager(containerRegistry));

    }

    public boolean isTooLong(AudioTrack track)
    {
        if(maxSeconds<=0)
            return false;
        return Math.round(track.getDuration()/1000.0) > maxSeconds;
    }

    public String getMaxTime()
    {
        return FormatUtil.formatTime(maxSeconds * 1000);
    }

}

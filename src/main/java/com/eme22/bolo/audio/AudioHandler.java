/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
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

import com.eme22.bolo.image.ArtworkImageService;
import com.eme22.bolo.model.MusicArtWork;
import com.eme22.bolo.model.RepeatMode;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.playlist.PlaylistLoader.Playlist;
import com.eme22.bolo.queue.FairQueue;
import com.eme22.bolo.utils.Constants;
import com.eme22.bolo.utils.FormatUtil;
import com.eme22.bolo.utils.GifSearcher;
import com.github.topisenpai.lavasrc.spotify.SpotifyAudioTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
@Log4j2
public class AudioHandler extends AudioEventAdapter implements AudioSendHandler {
    private final FairQueue<QueuedTrack> queue = new FairQueue<>();
    private final List<AudioTrack> defaultQueue = new LinkedList<>();
    private final Set<String> votes = new HashSet<>();

    private final PlayerManager manager;
    private final AudioPlayer audioPlayer;

    private final long guildId;

    private LastAuthor lastAuthor;

    private boolean stayInChannel;

    private String successEmoji;

    private boolean npImages;

    private long owner;

    private AudioFrame lastFrame;

    private static Color transparent =new Color(1f,0f,0f,.5f );

    private static int width = 480; // Ancho de la imagen
    private static int height = 30; // Altura de la imagen

    private static InputStream playImage = AudioHandler.class.getResourceAsStream("/images/play.png");

    private static InputStream pauseImage = AudioHandler.class.getResourceAsStream("/images/pause.png");

    public AudioHandler(PlayerManager manager, AudioPlayer audioPlayer, long guildId, boolean stayInChannel, String successEmoji, boolean npImages, long owner) {
        this.manager = manager;
        this.audioPlayer = audioPlayer;
        this.guildId = guildId;
        this.stayInChannel = stayInChannel;
        this.successEmoji = successEmoji;
        this.npImages = npImages;
        this.owner = owner;
    }

    public int addTrackToFront(QueuedTrack qtrack) {
        if (audioPlayer.getPlayingTrack() == null) {
            audioPlayer.playTrack(qtrack.getTrack());
            return -1;
        } else {
            queue.addAt(0, qtrack);
            return 0;
        }
    }

    public int addTrack(QueuedTrack qtrack) {
        if (audioPlayer.getPlayingTrack() == null) {
            audioPlayer.playTrack(qtrack.getTrack());
            return -1;
        } else
            return queue.add(qtrack);
    }

    public FairQueue<QueuedTrack> getQueue() {
        return queue;
    }

    public void stopAndClear() {
        queue.clear();
        defaultQueue.clear();
        audioPlayer.stopTrack();
        manager.getBot().getNowPlayingHandler().clearLastNPMessage(manager.getBot().getJDA().getGuildById(guildId));
        // current = null;
    }

    public boolean isMusicPlaying(JDA jda) {
        return guild(jda).getSelfMember().getVoiceState().inAudioChannel() && audioPlayer.getPlayingTrack() != null;
    }

    public Set<String> getVotes() {
        return votes;
    }

    public AudioPlayer getPlayer() {
        return audioPlayer;
    }

    public RequestMetadata getRequestMetadata() {
        if (audioPlayer.getPlayingTrack() == null)
            return RequestMetadata.EMPTY;
        RequestMetadata rm = audioPlayer.getPlayingTrack().getUserData(RequestMetadata.class);
        return rm == null ? RequestMetadata.EMPTY : rm;
    }

    public boolean playFromDefault() {
        if (!defaultQueue.isEmpty()) {
            audioPlayer.playTrack(defaultQueue.remove(0));
            return true;
        }
        Server settingsTEST = manager.getBot().getSettingsManager().getSettings(guildId);
        if (settingsTEST == null || settingsTEST.getDefaultPlaylist() == null)
            return false;

        Playlist pl = manager.getBot().getPlaylistLoader().getPlaylist(settingsTEST.getDefaultPlaylist());
        if (pl == null || pl.getItems().isEmpty())
            return false;
        pl.loadTracks(manager, (at) -> {
            if (audioPlayer.getPlayingTrack() == null)
                audioPlayer.playTrack(at);
            else
                defaultQueue.add(at);
        }, () -> {
            if (pl.getTracks().isEmpty() && !stayInChannel)
                manager.getBot().closeAudioConnection(guildId);
        });
        return true;
    }

    // Audio Events
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        RepeatMode repeatMode = manager.getBot().getSettingsManager().getSettings(guildId).getRepeatMode();
        // if the track ended normally, and we're in repeat mode, re-add it to the queue
        if (endReason == AudioTrackEndReason.FINISHED && repeatMode != RepeatMode.OFF) {
            QueuedTrack clone = new QueuedTrack(track.makeClone(), track.getUserData(RequestMetadata.class));
            if (repeatMode == RepeatMode.ALL)
                queue.add(clone);
            else
                queue.addAt(0, clone);
        }

        if (queue.isEmpty()) {
            if (!playFromDefault()) {
                manager.getBot().getNowPlayingHandler().onTrackUpdate(guildId, null, this);
                if (!stayInChannel)
                    manager.getBot().closeAudioConnection(guildId);
                // unpause, in the case when the player was paused and the track has been
                // skipped.
                // this is to prevent the player being paused next time it's being used.
                player.setPaused(false);
                manager.getBot().getNowPlayingHandler().clearLastNPMessage(guildId);
            }
        } else {
            QueuedTrack qt = queue.pull();
            player.playTrack(qt.getTrack());
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        try {

            votes.clear();
            manager.getBot().getNowPlayingHandler().onTrackUpdate(guildId, track, this);

            MessageCreateData m = getNowPlaying(manager.getBot().getJDA());
            Guild guild = manager.getBot().getJDA().getGuildById(guildId);
            if (m == null) {
                TextChannel chn = guild.getTextChannelById(manager.getBot().getSettingsManager().getSettings(guild).getTextChannelId());

                if (chn == null) {
                    chn = guild.getDefaultChannel().asTextChannel();
                }

                chn.sendMessage(getNoMusicPlaying(manager.getBot().getJDA())).queue();
                manager.getBot().getNowPlayingHandler().clearLastNPMessage(guild);
            } else {
                manager.getBot().getNowPlayingHandler().clearLastNPMessage(guild);
                TextChannel chn = guild.getTextChannelById(manager.getBot().getSettingsManager().getSettings(guild).getTextChannelId());

                if (chn == null)
                    chn = manager.getBot().getJDA().getGuildById(guildId).getDefaultChannel().asTextChannel();

                chn.sendMessage(m).queue(msg -> {
                    msg.addReaction(Emoji.fromFormatted("U+23EF")).queue(s -> {
                    }, t -> {
                    });
                    msg.addReaction(Emoji.fromFormatted("U+23ED")).queue(s -> {
                    }, t -> {
                    });
                    msg.addReaction(Emoji.fromFormatted("U+1F507")).queue(s -> {
                    }, t -> {
                    });
                    msg.addReaction(Emoji.fromFormatted("U+1F4C3")).queue(s -> {
                    }, t -> {
                    });
                    msg.addReaction(Emoji.fromFormatted("U+1F3B5")).queue(s -> {
                    }, t -> {
                    });

                    manager.getBot().getNowPlayingHandler().setLastNPMessage(msg);
                });
            }
        } catch (Exception exception) {
            log.error("Error: " + exception.getMessage(), exception);
        }
    }

    // Formatting
    public MessageCreateData getNowPlaying(JDA jda) {
        if (isMusicPlaying(jda)) {
            Guild guild = guild(jda);
            AudioTrack track = audioPlayer.getPlayingTrack();
            MessageCreateBuilder builder = new MessageCreateBuilder();
            String mb = FormatUtil.filter(successEmoji + " **Reproduciendo en: "
                    + guild.getSelfMember().getVoiceState().getChannel().getAsMention() + "...**");
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(guild.getSelfMember().getColor());
            RequestMetadata rm = getRequestMetadata();
            if (rm.getOwner() != 0L) {
                User u = guild.getJDA().getUserById(rm.user.id);
                if (u == null)
                    eb.setAuthor(rm.user.username + "#" + rm.user.discrim, null, rm.user.avatar);
                else
                    eb.setAuthor(u.getName() + "#" + u.getDiscriminator(), null, u.getEffectiveAvatarUrl());
            }

            try {
                eb.setTitle(track.getInfo().title, track.getInfo().uri);
            } catch (Exception e) {
                eb.setTitle(track.getInfo().title);
            }

            if (track instanceof YoutubeAudioTrack /* && npImages **/) {
                eb.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/mqdefault.jpg");
            }

            if (track.getInfo().author != null && !track.getInfo().author.isEmpty()){
                eb.setFooter("Source: " + track.getInfo().author, null);
            }

            //log.info("https://bf38-181-66-137-208.ngrok-free.app/"+guildId+"_progress.png");


            double progress = (double) audioPlayer.getPlayingTrack().getPosition() / track.getDuration();

            //eb.setDescription(musicPlayer(audioPlayer.getPlayingTrack().getPosition(), track.getDuration(), !audioPlayer.isPaused()));


            eb.setDescription((audioPlayer.isPaused() ? Constants.PAUSE_EMOJI : Constants.PLAY_EMOJI)
                    + " " + FormatUtil.progressBar(progress)
                    + " `[" + FormatUtil.formatTime(track.getPosition()) + "/"
                    + FormatUtil.formatTime(track.getDuration()) + "]` "
                    + FormatUtil.volumeIcon(audioPlayer.getVolume()));




            String author = track.getInfo().author.toLowerCase();

            Optional<MusicArtWork> artWork = manager.getBot().getArtworkImageService().getArtwork(author);

            if (artWork.isPresent()) {
                eb.setImage(artWork.get().getUrl());
            } else if (track instanceof SpotifyAudioTrack) {
                eb.setImage(((SpotifyAudioTrack) track).getArtworkURL());
            } else {
                eb.setImage("https://i.gifer.com/XNYN.gif");
            }

            return builder.setContent(mb).setEmbeds(eb.build()).build();


        } else
            return null;
    }

    public MessageCreateData getNoMusicPlaying(JDA jda) {
        Guild guild = guild(jda);
        return new MessageCreateBuilder()
                .setContent(FormatUtil.filter(successEmoji + " **Now Playing...**"))
                .setEmbeds(new EmbedBuilder()
                        .setTitle("No hay musica")
                        .setDescription(Constants.STOP_EMOJI + " " + FormatUtil.progressBar(-1) + " "
                                + FormatUtil.volumeIcon(audioPlayer.getVolume()))
                        .setColor(guild.getSelfMember().getColor())
                        .build())
                .build();
    }

    public String getTopicFormat(JDA jda) {
        if (isMusicPlaying(jda)) {
            long userid = getRequestMetadata().getOwner();
            AudioTrack track = audioPlayer.getPlayingTrack();
            String title = track.getInfo().title;
            if (title == null || title.equals("Titulo desconocido"))
                title = track.getInfo().uri;
            return "**" + title + "** [" + (userid == 0 ? "autoplay" : "<@" + userid + ">") + "]"
                    + "\n" + (audioPlayer.isPaused() ? Constants.PAUSE_EMOJI : Constants.PLAY_EMOJI) + " "
                    + "[" + FormatUtil.formatTime(track.getDuration()) + "] "
                    + FormatUtil.volumeIcon(audioPlayer.getVolume());
        } else
            return "No hay musica " + Constants.STOP_EMOJI + " " + FormatUtil.volumeIcon(audioPlayer.getVolume());
    }

    // Audio Send Handler methods
    /*
     * @Override
     * public boolean canProvide()
     * {
     * if (lastFrame == null)
     * lastFrame = audioPlayer.provide();
     * 
     * return lastFrame != null;
     * }
     * 
     * @Override
     * public byte[] provide20MsAudio()
     * {
     * if (lastFrame == null)
     * lastFrame = audioPlayer.provide();
     * 
     * byte[] data = lastFrame != null ? lastFrame.getData() : null;
     * lastFrame = null;
     * 
     * return data;
     * }
     */

    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastFrame.getData());
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    // Private methods
    private Guild guild(JDA jda) {
        return jda.getGuildById(guildId);
    }

    @Getter
    public static class LastAuthor implements Serializable {
        String author;
        String link;

        public LastAuthor(String author, String link) {
            this.author = author;
            this.link = link;
        }


    }
}

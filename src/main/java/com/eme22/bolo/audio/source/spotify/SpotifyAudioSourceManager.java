package com.eme22.bolo.audio.source.spotify;

import com.eme22.bolo.utils.SpotifyDecoderUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchMusicProvider;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;

public class SpotifyAudioSourceManager extends YoutubeAudioSourceManager {

    private final SpotifyDecoderUtil decoderUtil;

    private final SpotifyPlayListLoader playlistLoader;
    private static final String PLAYLIST_URL_REGEX = "^https?:\\/\\/open\\.spotify\\.com/playlist/([0-9A-Za-z]{22})";

    private static final String TRACK_URL_REGEX = "^https?:\\/\\/open\\.spotify\\.com/track/([0-9A-Za-z]{22})";

    private static final Pattern playlistUrlPattern = Pattern.compile(PLAYLIST_URL_REGEX);

    private static final Pattern trackUrlPattern = Pattern.compile(TRACK_URL_REGEX);
    private final YoutubeSearchMusicProvider searchMusicResultLoader;



    public SpotifyAudioSourceManager(String spotifyUserId, String spotifySecret) {
        super(true);
        decoderUtil  = new SpotifyDecoderUtil(spotifyUserId, spotifySecret);
        playlistLoader = new SpotifyPlayListLoader();
        searchMusicResultLoader = new YoutubeSearchMusicProvider();
    }


    @Override
    public String getSourceName() {
        return "spotify-youtube";
    }

    private AudioReference processAsSingleTrack(AudioReference reference) {
        String url = reference.identifier;

        Matcher trackUrlMatcher = trackUrlPattern.matcher(url);

        if (trackUrlMatcher.find()) {
            return loadTrack(trackUrlMatcher.group(1));
        }

        return null;
    }

    private AudioReference processAsPlaylist(AudioReference reference) {
        String url = reference.identifier;
        Matcher trackUrlMatcher = playlistUrlPattern.matcher(url);
        if (trackUrlMatcher.find()) {
            return new AudioReference(trackUrlMatcher.group(1), null);
        }

        return null;
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        try {

            return trackRouter(manager, reference);


        } catch (FriendlyException exception) {
            if (HttpClientTools.isRetriableNetworkException(exception.getCause())) {
                return trackRouter(manager, reference);
            } else {
                exception.printStackTrace();
                throw exception;
            }
        }
    }

    private AudioItem trackRouter(AudioPlayerManager manager, AudioReference reference) {
        AudioReference trackReference = processAsSingleTrack(reference);

        if (trackReference == null) {

            AudioReference playList = processAsPlaylist(reference);
            if (playList != null) {
                return playlist(playList.identifier, null);
            } else throw new FriendlyException("Unknown URL", COMMON, null);

        }

        return searchMusic(trackReference.identifier);
    }


    public AudioReference loadTrack(String spotifyID) {

        try {
            String song = decoderUtil.getTrack(spotifyID);

            if (song == null) return null;

            return new AudioReference(song, null);

        } catch (IOException | ParseException | SpotifyWebApiException e) {
            e.printStackTrace();
            throw new FriendlyException("This track is not available", COMMON, null);
        }

    }

    public AudioItem playlist(String playlistId, String selectedVideoId) {

        //log.debug("Starting to load playlist with ID { "+playlistId+"} ");

        try (HttpInterface ignored = getHttpInterface()) {
            return playlistLoader.load(decoderUtil, this, playlistId, selectedVideoId,
                    SpotifyAudioSourceManager.this::buildTrackFromInfo);
        } catch (Exception e) {
            throw ExceptionTools.wrapUnfriendlyExceptions(e);
        }
    }
    private YoutubeAudioTrack buildTrackFromInfo(AudioTrackInfo info) {
        return new YoutubeAudioTrack(info, this);
    }

    public AudioItem searchMusic(String query) {

        return searchMusicResultLoader.loadSearchMusicResult(
                query,
                SpotifyAudioSourceManager.this::buildTrackFromInfo
        );
    }

}

package com.eme22.bolo.audio.source.spotify;

import com.eme22.bolo.utils.SpotifyDecoderUtil;
import com.sedmelluq.discord.lavaplayer.source.youtube.DefaultYoutubePlaylistLoader;
import com.sedmelluq.discord.lavaplayer.track.*;
import org.apache.commons.collections4.map.SingletonMap;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpotifyPlayListLoader extends DefaultYoutubePlaylistLoader {

    public AudioPlaylist load(SpotifyDecoderUtil api, SpotifyAudioSourceManager audioSourceManager, String playlistId, String selectedVideoId, Function<AudioTrackInfo, AudioTrack> trackFactory) throws IOException, ParseException, SpotifyWebApiException {

        SingletonMap<String, ArrayList<String>> tracks = api.getPlayList(playlistId);

        String name = tracks.getKey();

        ArrayList<String> tracksLeft = tracks.getValue();

        ArrayList<AudioTrack> audioTracks = tracksLeft.stream().map(audioSourceManager::searchMusic).map(BasicAudioPlaylist.class::cast).map( basicAudioPlaylist -> basicAudioPlaylist.getTracks().get(0) ).collect(Collectors.toCollection(ArrayList::new));

        return new BasicAudioPlaylist(name, audioTracks, null, false);

    }
}

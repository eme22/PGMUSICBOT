package com.eme22.bolo.utils;

import org.apache.commons.collections4.map.SingletonMap;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.NotFoundException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SpotifyDecoderUtil {

    private static SpotifyApi spotifyApi;

    private static long nextUpdate= 0L;

    public SpotifyDecoderUtil(String user, String secret){

        spotifyApi = new SpotifyApi.Builder()
                .setClientId(user)
                .setClientSecret(secret)
                .build();

        try {
            checkCredentials();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }

    }

    public SingletonMap<String , ArrayList<String>> getPlayList(String id) throws IOException, ParseException, SpotifyWebApiException {

        checkCredentials();


        try {
            String name = spotifyApi.getPlaylist(id).fields("name").build().execute().getName();

            Paging<PlaylistTrack> playlistIRequest = spotifyApi.getPlaylistsItems(id)
            //        .fields("items(track(name, artists(name)))")
                    .limit(100).build().execute();


            ArrayList<Track> tracks = Arrays.stream(playlistIRequest.getItems()).map(PlaylistTrack::getTrack).map(Track.class::cast).collect(Collectors.toCollection(ArrayList::new));


            return new SingletonMap<>(name ,tracks.stream().map(n -> n.getName()+ " - " + n.getArtists()[0].getName() ).collect(Collectors.toCollection(ArrayList::new)));

        } catch ( NotFoundException ignored) {
            return null;
        }

    }

    public String getTrack(String id) throws IOException, ParseException, SpotifyWebApiException {

        checkCredentials();

        try {
            Track track = spotifyApi.getTrack(id).build().execute();

            return track.getName() + " - "+ track.getArtists()[0].getName();
        } catch ( NotFoundException | NullPointerException ignored) {
            return null;
        }



    }

    private void checkCredentials() throws IOException, SpotifyWebApiException, ParseException {
        if ( System.currentTimeMillis() >= nextUpdate) {

            ClientCredentials clientCredentials = spotifyApi.clientCredentials()
                    .build().execute();

            nextUpdate = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(clientCredentials.getExpiresIn());

            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        }
    }

}

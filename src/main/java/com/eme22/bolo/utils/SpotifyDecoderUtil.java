package com.eme22.bolo.utils;

import org.apache.commons.collections4.map.SingletonMap;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.NotFoundException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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


            return getStringArrayListSingletonMap(name, tracks);

        } catch ( NotFoundException ignored) {
            return null;
        }

    }

    @NotNull
    private SingletonMap<String, ArrayList<String>> getStringArrayListSingletonMap(String name, ArrayList<Track> tracks) {
        return new SingletonMap<>(name ,tracks.stream().map(n ->
                n.getArtists().length == 1 ?
                        n.getName()+ " - " + n.getArtists()[0].getName() :
                        n.getName()+ " - " + n.getArtists()[0].getName() + ", " + n.getArtists()[1].getName()
        ).collect(Collectors.toCollection(ArrayList::new)));
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

    public SingletonMap<String, ArrayList<String>> getAlbum(String albumId) throws IOException, ParseException, SpotifyWebApiException {

        checkCredentials();

        try {

            Album album = spotifyApi.getAlbum(albumId).build().execute();

            String name = album.getName();

            ArrayList<Track> tracks = Arrays.stream(album.getTracks().getItems()).map(Track.class::cast).collect(Collectors.toCollection(ArrayList::new));

            return getStringArrayListSingletonMap(name, tracks);

        } catch (IOException | SpotifyWebApiException | ParseException ignored) {
            return null;
        }

    }
}

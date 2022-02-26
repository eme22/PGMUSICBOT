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
package com.eme22.bolo.settings;

import com.eme22.bolo.entities.Poll;
import com.eme22.bolo.utils.OtherUtil;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jagrosh.jdautilities.command.GuildSettingsProvider;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.io.IOException;
import java.util.*;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
@JsonAdapter(Settings.SettingsAdapter.class)
public class Settings implements GuildSettingsProvider {
    private final SettingsManager manager;
    protected long textId;
    protected long voiceId;
    protected long roleId;
    protected long adminroleId;
    protected int volume;
    protected String defaultPlaylist;
    protected RepeatMode repeatMode;
    protected String prefix;
    protected double skipRatio;
    protected long helloID;
    protected long goodByeID;
    protected HashSet<Long> onlyImageChannels;
    protected List<Map<String, String>> memeImages;
    protected List<Poll> polls;

    public Settings(SettingsManager manager, String textId, String voiceId, String roleId, String adminroleId, int volume, String defaultPlaylist, RepeatMode repeatMode, String prefix, double skipRatio, String helloID, String goodByeID, HashSet<Long> imageOnly, List<Map<String, String>> memeImages) {
        this.manager = manager;
        try {
            this.textId = Long.parseLong(textId);
        } catch (NumberFormatException e) {
            this.textId = 0;
        }
        try {
            this.voiceId = Long.parseLong(voiceId);
        } catch (NumberFormatException e) {
            this.voiceId = 0;
        }
        try {
            this.roleId = Long.parseLong(roleId);
        } catch (NumberFormatException e) {
            this.roleId = 0;
        }
        try {
            this.adminroleId = Long.parseLong(adminroleId);
        } catch (NumberFormatException e) {
            this.adminroleId = 0;
        }
        this.volume = volume;
        this.defaultPlaylist = defaultPlaylist;
        this.repeatMode = repeatMode;
        this.prefix = prefix;
        this.skipRatio = skipRatio;
        try {
            this.helloID = Long.parseLong(helloID);
        } catch (NumberFormatException e) {
            this.helloID = 0;
        }
        try {
            this.goodByeID = Long.parseLong(goodByeID);
        } catch (NumberFormatException e) {
            this.helloID = 0;
        }

        if (imageOnly == null)
            this.onlyImageChannels = new HashSet<>();
        else
            this.onlyImageChannels = imageOnly;

        if (memeImages == null)
            this.memeImages = new ArrayList<>();
        else
            this.memeImages = memeImages;
    }

    public Settings(SettingsManager manager, long textId, long voiceId, long roleId, long adminroleId,int volume, String defaultPlaylist, RepeatMode repeatMode, String prefix, double skipRatio, long helloID, long goodByeID, HashSet<Long> imageOnly, List<Map<String, String>> memeImages) {
        this.manager = manager;
        this.textId = textId;
        this.voiceId = voiceId;
        this.roleId = roleId;
        this.adminroleId = adminroleId;
        this.volume = volume;
        this.defaultPlaylist = defaultPlaylist;
        this.repeatMode = repeatMode;
        this.prefix = prefix;
        this.skipRatio = skipRatio;
        this.helloID = helloID;
        this.goodByeID = goodByeID;
        this.onlyImageChannels = imageOnly;
        this.memeImages = memeImages;
    }

    // Getters
    public TextChannel getTextChannel(Guild guild) {
        return guild == null ? null : guild.getTextChannelById(textId);
    }

    public VoiceChannel getVoiceChannel(Guild guild) {
        return guild == null ? null : guild.getVoiceChannelById(voiceId);
    }

    public Role getDJRole(Guild guild) {
        return guild == null ? null : guild.getRoleById(roleId);
    }

    public Role getAdminRole(Guild guild) {
        return guild == null ? null : guild.getRoleById(adminroleId);
    }

    public int getVolume() {
        return volume;
    }

    public String getDefaultPlaylist() {
        return defaultPlaylist;
    }

    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    public String getPrefix() {
        return prefix;
    }

    public double getSkipRatio() {
        return skipRatio;
    }

    public TextChannel getHelloChannel(Guild guild) {
        return guild == null ? null : guild.getTextChannelById(helloID);
    }

    public TextChannel getGoodbyeChannel(Guild guild) {
        return guild == null ? null : guild.getTextChannelById(goodByeID);
    }

    @Override
    public Collection<String> getPrefixes() {
        return prefix == null ? Collections.emptySet() : Collections.singleton(prefix);
    }

    // Setters
    public void setTextChannel(TextChannel tc) {
        this.textId = tc == null ? 0 : tc.getIdLong();
        this.manager.writeSettings();
    }

    public void setVoiceChannel(VoiceChannel vc) {
        this.voiceId = vc == null ? 0 : vc.getIdLong();
        this.manager.writeSettings();
    }

    public void setHelloID(TextChannel helloID) {
        this.helloID = helloID == null ? 0 : helloID.getIdLong();
        this.manager.writeSettings();
    }

    public void setGoodByeID(TextChannel goodByeID) {
        this.goodByeID = goodByeID == null ? 0 : goodByeID.getIdLong();
        this.manager.writeSettings();
    }

    public void setDJRole(Role role) {
        this.roleId = role == null ? 0 : role.getIdLong();
        this.manager.writeSettings();
    }

    public void setAdminRole(Role role) {
        this.adminroleId = role == null ? 0 : role.getIdLong();
        this.manager.writeSettings();
    }

    public void setVolume(int volume) {
        this.volume = volume;
        this.manager.writeSettings();
    }

    public void setDefaultPlaylist(String defaultPlaylist) {
        this.defaultPlaylist = defaultPlaylist;
        this.manager.writeSettings();
    }

    public void setRepeatMode(RepeatMode mode) {
        this.repeatMode = mode;
        this.manager.writeSettings();
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.manager.writeSettings();
    }

    public void setSkipRatio(double skipRatio) {
        this.skipRatio = skipRatio;
        this.manager.writeSettings();
    }

    public ArrayList<TextChannel> getOnlyImageChannels(Guild guild) {

        ArrayList<TextChannel> channels = new ArrayList<>();

        this.onlyImageChannels.forEach(channelid -> channels.add(guild.getTextChannelById(String.valueOf(channelid))));

        return channels;
    }

    public void addOnlyImageChannels(TextChannel onlyImageChannel) {

        Long channel = onlyImageChannel.getIdLong();

        if (OtherUtil.hasValue(onlyImageChannels, channel))
            return;

        this.onlyImageChannels.add(channel);
        this.manager.writeSettings();
    }

    public void removeFromOnlyImageChannels(TextChannel onlyImageChannel) {

        Long channel = onlyImageChannel.getIdLong();

        onlyImageChannels.removeIf(element -> element.equals(channel));
        this.manager.writeSettings();
    }

    public List<Map<String, String>> getMemeImages() {
        return memeImages;
    }

    public Map<String, String> getMemeImage(int position) throws IndexOutOfBoundsException {
        return memeImages.get(position);
    }

    public Map<String, String> getRandomMemeImages() throws IllegalArgumentException {
        int rand = new Random().nextInt(this.memeImages.size());
        return this.memeImages.get(rand);
    }

    public void addToMemeImages(String message, String imageLink) {

        Map<String, String> meme = Collections.singletonMap(message, imageLink);
        if (OtherUtil.hasValue(this.memeImages, meme))
            return;

        this.memeImages.add(meme);
        this.manager.writeSettings();
    }

    public void deleteFromMemeImages(int position) {

        this.memeImages.remove(position);
        this.manager.writeSettings();
    }

    public void clearServerData(Guild guild) {
        this.textId = 0;
        this.voiceId = 0;
        this.roleId = 0;
        this.volume = 0;
        this.defaultPlaylist = null;
        this.repeatMode = null;
        this.prefix = null;
        this.skipRatio = 0;
        this.helloID = 0;
        this.onlyImageChannels = null;
        this.memeImages = null;
        this.goodByeID = 0;
        this.manager.deleteSettings(guild.getId());
    }

    public static class SettingsAdapter extends TypeAdapter<Settings> {

        @Override
        public void write(JsonWriter writer, Settings value) throws IOException {

            writer.beginObject();
            if (value.textId != 0)
                writer.name("text_channel_id").value(value.textId);
            if (value.voiceId != 0)
                writer.name("voice_channel_id").value(value.voiceId);
            if (value.roleId != 0)
                writer.name("dj_role_id").value(value.roleId);
            if (value.adminroleId != 0)
                writer.name("admin_role_id").value(value.adminroleId);
            if (value.volume != 0)
                writer.name("volume").value(value.volume);
            if (value.defaultPlaylist != null)
                writer.name("default_playlist").value(value.defaultPlaylist);
            if (value.repeatMode != null)
                writer.name("repeat_mode").value(value.repeatMode.ordinal());
            if (value.prefix != null)
                writer.name("prefix").value(value.prefix);
            if (value.skipRatio != 0)
                writer.name("skip_ratio").value(value.skipRatio);
            if (value.helloID != 0)
                writer.name("bienvenidas_channel_id").value(value.helloID);
            if (value.goodByeID != 0)
                writer.name("despedidas_channel_id").value(value.goodByeID);
            if (value.onlyImageChannels != null){
                writer.name("image_only_channels_ids");
                writer.beginArray();
                value.onlyImageChannels.forEach(channel -> {
                    try {
                        writer.value(channel);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                writer.endArray();
            }
            if (value.memeImages != null) {
                writer.name("meme_images");
                writer.beginArray();
                value.memeImages.forEach(stringStringMap -> {
                    stringStringMap.forEach((message, meme) -> {
                        try {
                            writer.beginObject();
                            writer.name("message").value(message);
                            writer.name("meme").value(meme);
                            writer.endObject();
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }

                    });

                });
                writer.endArray();
            }
            writer.endObject();

        }

        @Override
        public Settings read(JsonReader reader) throws IOException {

            String textId = null;
            String voiceId = null;
            String roleId = null;
            String adminroleId = null;
            int volume = 100;
            String defaultPlaylist = null;
            RepeatMode repeatMode = null;
            String prefix = null;
            double skipRatio = 0;
            String helloID = null;
            String goodByeID = null;
            HashSet<Long> onlyImageChannels = new HashSet<>();
            List<Map<String, String>> memeImages = new ArrayList<>();

            reader.beginObject();
            while (reader.hasNext()){
                String name = reader.nextName();
                if (name.equals("text_channel_id")) {
                    textId = String.valueOf(reader.nextLong());
                }
                if (name.equals("voice_channel_id")) {
                    voiceId = String.valueOf(reader.nextLong());
                }
                if (name.equals("dj_role_id")) {
                    roleId = String.valueOf(reader.nextLong());
                }
                if (name.equals("admin_role_id")) {
                    adminroleId = String.valueOf(reader.nextLong());
                }
                if (name.equals("volume")) {
                    volume = reader.nextInt();
                }
                if (name.equals("default_playlist")) {
                    defaultPlaylist = reader.nextString();
                }
                if (name.equals("repeat_mode")) {
                    switch (reader.nextInt()){
                        case 0: repeatMode = RepeatMode.OFF; break;
                        case 1: repeatMode = RepeatMode.ALL; break;
                        case 2: repeatMode = RepeatMode.SINGLE; break;
                    }
                }
                if (name.equals("prefix")) {
                    prefix = reader.nextString();
                }
                if (name.equals("skip_ratio")) {
                    skipRatio = reader.nextDouble();
                }
                if (name.equals("bienvenidas_channel_id")) {
                    helloID = String.valueOf(reader.nextLong());
                }
                if (name.equals("despedidas_channel_id")) {
                    goodByeID = String.valueOf(reader.nextLong());
                }
                if (name.equals("image_only_channels_ids")) {
                    reader.beginArray();
                    while (reader.hasNext()){
                        onlyImageChannels.add(reader.nextLong());
                    }
                    reader.endArray();
                }
                if (name.equals("meme_images")) {
                    reader.beginArray();
                    while (reader.hasNext()){
                        reader.beginObject();
                        String mesage = null;
                        String meme = null;
                        while (reader.hasNext()){
                            String name1 = reader.nextName();
                            if (name1.equals("message")){
                                mesage = reader.nextString();
                            }
                            else if (name1.equals("meme")){
                                meme = reader.nextString();
                            }
                        }
                        reader.endObject();
                        memeImages.add(Collections.singletonMap(mesage, meme));
                    }
                    reader.endArray();
                }
            }

            reader.endObject();

            return new Settings(null, textId, voiceId, roleId, adminroleId, volume, defaultPlaylist, repeatMode, prefix, skipRatio, helloID, goodByeID, onlyImageChannels, memeImages);
        }

    }

}
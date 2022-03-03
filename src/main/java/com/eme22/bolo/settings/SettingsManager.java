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
package com.eme22.bolo.settings;

import com.eme22.bolo.Bot;
import com.eme22.bolo.entities.Poll;
import com.eme22.bolo.utils.OtherUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.GuildSettingsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class SettingsManager implements GuildSettingsManager<Settings>
{
    private final static double SKIP_RATIO = .55;
    private final HashMap<Long, Settings> settings;

    public SettingsManager()
    {
        this.settings = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("serversettings.json");
            if (!file.exists())
                throw new IOException();
            HashMap<Long, Settings> temp_Settings = mapper.readValue(file, new TypeReference<>() {});
            temp_Settings.forEach( (aLong, settingsTEST) -> this.settings.put(aLong, settingsTEST.withManager(this)));

        } catch( IOException e) {
            LoggerFactory.getLogger("Settings").warn("Failed to load server settings (this is normal if no settings have been set yet): "+e);
        }


        /*
        this.settings = new HashMap<>();
        try {
            Reader reader = Files.newBufferedReader(OtherUtil.getPath("serversettings.json"));
            Type type = new TypeToken<HashMap<Long,Settings>>(){}.getType();
            HashMap<Long,Settings> settings = new Gson().fromJson(reader, type);
            if (settings == null)
                throw new IOException();
            settings.forEach( (aLong, settings1) -> this.settings.put(aLong, new Settings(this, settings1.textId, settings1.voiceId, settings1.roleId, settings1.adminroleId, settings1.volume, settings1.defaultPlaylist, settings1.repeatMode, settings1.prefix, settings1.skipRatio, settings1.helloID, settings1.helloImage, settings1.goodByeID, settings1.goodByeImage,settings1.onlyImageChannels, settings1.memeImages)));
            reader.close();

        } catch( IOException e) {
            LoggerFactory.getLogger("Settings").warn("Failed to load server settings (this is normal if no settings have been set yet): "+e);
        }

         */
    }
    
    /**
     * Gets non-null settings for a Guild
     * 
     * @param guild the guild to get settings for
     * @return the existing settings, or new settings for that guild
     */
    @Override
    public Settings getSettings(Guild guild)
    {
        return getSettings(guild.getIdLong());
    }
    
    public Settings getSettings(long guildId)
    {
        Settings data = null;
        try {
            data = settings.computeIfAbsent(guildId, id -> createDefaultSettings());
        } catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }

    private Settings createDefaultSettings()
    {
        return new Settings(this, 0, 0, 0, 0, 100, null, RepeatMode.OFF, null, SKIP_RATIO, 0, null, 0, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }
    
    public void writeSettings()
    {


        // convert book object to JSON file
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get("serversettings.json").toFile(), settings);

        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        try {
            FileWriter writer = new FileWriter(OtherUtil.getPath("serversettings.json").toFile());
            new GsonBuilder().setPrettyPrinting().create().toJson(settings, writer);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            LoggerFactory.getLogger("Settings").warn("Failed to write to file: "+ex);
        }
         */
    }

    protected void deleteSettings(String guild)
    {
        settings.remove(Long.parseLong(guild));
    }

    public void onGuildMessageReactionAdd(@NotNull MessageReactionAddEvent event, Bot bot) {

        if (this.settings.get(event.getGuild().getIdLong()).getPolls().stream().anyMatch( poll -> poll.getId() == event.getMessageIdLong())) {
            Poll polls = this.settings.get(event.getGuild().getIdLong()).getPolls().stream().filter(poll -> poll.getId() == event.getMessageIdLong()).findFirst().orElse(null);
            int num = OtherUtil.EmojiToNumber(event.getReaction().getReactionEmote().getEmoji());
            if (polls != null && num != -1 && polls.isUserParticipating(event.getUserIdLong())){
                event.getUser().openPrivateChannel().queue( success -> success.sendMessage(bot.getConfig().getError()+" Solo puedes votar una vez").queue(m ->
                        m.delete().queueAfter(30, TimeUnit.SECONDS)));
                event.getReaction().removeReaction(event.getUser()).queue();
            }else {

                if (polls != null) {
                    polls.addVoteToAnswer(num, event.getUserIdLong());
                    event.getTextChannel().
                            editMessageEmbedsById(
                                    event.getMessageId(),
                                    new EmbedBuilder()
                                            .setDescription(OtherUtil.makePollString(polls))
                                            .build()
                            ).queue();
                    //settings.get(event.getGuild().getIdLong()).getManager().writeSettings();
                }

            }
        }
    }

    public void onGuildMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {

        if (this.settings.get(event.getGuild().getIdLong()).getPolls().stream().anyMatch( poll -> poll.getId() == event.getMessageIdLong())) {
            Poll polls = this.settings.get(event.getGuild().getIdLong()).getPolls().stream().filter(poll -> poll.getId() == event.getMessageIdLong()).findFirst().orElse(null);
            int num = OtherUtil.EmojiToNumber(event.getReaction().getReactionEmote().getEmoji());
            if (polls != null && num != -1 && polls.isUserParticipating(event.getUserIdLong())) {
                if (polls.isUserParticipatingInAnswer(num, event.getUserIdLong())) {
                    polls.removeVoteFromAnswer(num, event.getUserIdLong());
                    event.getTextChannel().
                            editMessageEmbedsById(
                                    event.getMessageId(),
                                    new EmbedBuilder()
                                            .setDescription(OtherUtil.makePollString(polls))
                                            .build()
                            ).queue();

                    //settings.get(event.getGuild().getIdLong()).getManager().writeSettings();
                }
            }
        }

    }


    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        this.settings.get(event.getGuild().getIdLong()).removePollFromGuild(event.getMessageIdLong());
    }
}

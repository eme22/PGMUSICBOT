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

import com.eme22.bolo.model.Server;
import com.eme22.bolo.repository.ServerRepository;
import com.eme22.bolo.utils.Constants;
import com.jagrosh.jdautilities.command.GuildSettingsManager;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
@Component
public class SettingsManager implements GuildSettingsManager<Server> {
    private final ServerRepository serverRepository;

    @Autowired
    public SettingsManager(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    /**
     * Gets non-null settings for a Guild
     * 
     * @param guild the guild to get settings for
     * @return the existing settings, or new settings for that guild
     */
    @Override
    public Server getSettings(Guild guild) {
        return getSettings(guild.getIdLong());
    }

    public Server getSettings(long guildId) {

        return serverRepository.findById(guildId).map(server -> {
            if (server.getManager() == null)
                server.setManager(this);
            return server;
        }).orElse(createDefaultSettings(guildId));

    }

    public void saveSettings(Server server) {
        serverRepository.save(server);
    }

    protected Server createDefaultSettings(long guildId) {

        return Server.builder()
                .manager(this)
                .id(guildId)
                .textChannelId(0)
                .voiceChannelId(0)
                .djRoleId(0)
                .adminRoleId(0)
                .volume(Constants.DEFAULT_VOLUME)
                .defaultPlaylist(null)
                .repeatMode(com.eme22.bolo.model.RepeatMode.OFF)
                .prefix(null)
                .skipRatio(Constants.SKIP_RATIO)
                .bienvenidasChannelEnabled(false)
                .bienvenidasChannelId(0)
                .bienvenidasChannelMessage(null)
                .bienvenidasChannelImage(null)
                .despedidasChannelImage(null)
                .despedidasChannelMessage(null)
                .despedidasChannelEnabled(false)
                .despedidasChannelId(0)
                .birthdayChannelId(0)
                .birthdays(new ArrayList<>())
                .memeImages(new ArrayList<>())
                .imageOnlyChannelsIds(new ArrayList<>())
                .roleManagerList(new ArrayList<>())
                .eightBallAnswers(new ArrayList<>())
                .antiRaidMode(false)
                .build();
    }


    protected void deleteSettings(Guild guild) {
        serverRepository.deleteById(guild.getIdLong());
    }

    public void updateOldSettings(File oldSettings) {

    }
}

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

import com.eme22.bolo.BotConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.GuildSettingsManager;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class SettingsManager implements GuildSettingsManager<Settings> {
    private final static double SKIP_RATIO = .55;
    private final HashMap<Long, Settings> settings;

    public SettingsManager() {
        this.settings = new HashMap<>();
        loadSettings();

    }

    public void loadSettings() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("serversettings.json");
            if (!file.exists())
                throw new IOException();
            HashMap<Long, Settings> temp_Settings = mapper.readValue(file, new TypeReference<>() {
            });
            temp_Settings.forEach((aLong, settingsTEST) -> this.settings.put(aLong, settingsTEST.withManager(this)));

        } catch (IOException e) {
            LoggerFactory.getLogger("Settings")
                    .warn("Failed to load server settings (this is normal if no settings have been set yet): " + e);
        }
    }

    /**
     * Gets non-null settings for a Guild
     * 
     * @param guild the guild to get settings for
     * @return the existing settings, or new settings for that guild
     */
    @Override
    public Settings getSettings(Guild guild) {
        return getSettings(guild.getIdLong());
    }

    public Settings getSettings(long guildId) {
        Settings data = null;
        try {
            data = settings.computeIfAbsent(guildId, id -> createDefaultSettings());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    protected Settings createDefaultSettings() {
        return new Settings(this, 0, 0, 0, 0, BotConfig.DEFAULT_VOLUME, null, RepeatMode.OFF, null, SKIP_RATIO, 0, null,
                null, 0, null, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>());
    }

    public void writeSettings() {

        // convert book object to JSON file
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get("serversettings.json").toFile(), settings);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void deleteSettings(String guild) {
        settings.remove(Long.parseLong(guild));
    }
}

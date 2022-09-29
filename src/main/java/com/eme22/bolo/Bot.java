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
package com.eme22.bolo;

import com.eme22.bolo.audio.AloneInVoiceHandler;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.NowplayingHandler;
import com.eme22.bolo.audio.PlayerManager;
import com.eme22.bolo.birthday.BirthdayManager;
import com.eme22.bolo.gui.GUI;
import com.eme22.bolo.playlist.PlaylistLoader;
import com.eme22.bolo.settings.SettingsManager;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */

@Getter
@Setter
public class Bot
{
    private final EventWaiter waiter;
    private final ScheduledExecutorService threadpool;
    private final BotConfig config;
    private final SettingsManager settingsManager;
    private final PlayerManager playerManager;
    private final PlaylistLoader playlistLoader;
    private final NowplayingHandler nowPlayingHandler;
    private final AloneInVoiceHandler aloneInVoiceHandler;
    private final BirthdayManager birthdayManager;
    
    private boolean shuttingDown = false;
    private boolean devMode = false;
    private GUI GUI;

    // JDA Entity
    private JDA JDA;
    
    public Bot(EventWaiter waiter, BotConfig config, SettingsManager settings)
    {
        this.waiter = waiter;
        this.config = config;
        this.settingsManager = settings;
        this.playlistLoader = new PlaylistLoader(config);
        this.threadpool = Executors.newSingleThreadScheduledExecutor();
        this.playerManager = new PlayerManager(this);
        this.playerManager.init();
        this.nowPlayingHandler = new NowplayingHandler(this);
        this.nowPlayingHandler.init();
        this.aloneInVoiceHandler = new AloneInVoiceHandler(this);
        this.aloneInVoiceHandler.init();
        this.birthdayManager = new BirthdayManager(this);
    }
    
    public void closeAudioConnection(long guildId)
    {
        Guild guild = JDA.getGuildById(guildId);
        if(guild!=null)
            threadpool.submit(() -> guild.getAudioManager().closeAudioConnection());
    }
    
    public void resetGame()
    {
        Activity game = config.getGame()==null || config.getGame().getName().equalsIgnoreCase("none") ? null : config.getGame();
        if(!Objects.equals(JDA.getPresence().getActivity(), game))
            JDA.getPresence().setActivity(game);
    }

    public void shutdown()
    {
        if(shuttingDown)
            return;
        shuttingDown = true;
        threadpool.shutdownNow();
        if(JDA.getStatus()!=net.dv8tion.jda.api.JDA.Status.SHUTTING_DOWN)
        {
            JDA.getGuilds().forEach(g ->
            {
                g.getAudioManager().closeAudioConnection();
                AudioHandler ah = (AudioHandler)g.getAudioManager().getSendingHandler();
                if(ah!=null)
                {
                    ah.stopAndClear();
                    ah.getPlayer().destroy();
                    nowPlayingHandler.updateTopic(g.getIdLong(), ah, true);
                }
            });
            JDA.shutdown();
        }
        if(GUI!=null)
            GUI.dispose();
        System.exit(0);
    }
}

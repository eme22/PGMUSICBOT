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
package com.eme22.bolo.commands.dj;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.commands.DJCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class PauseCmd extends DJCommand 
{
    public PauseCmd(Bot bot)
    {
        super(bot);
        this.name = "pause";
        this.help = "pauses the current song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) 
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if(handler.getPlayer().isPaused())
        {
            event.replyWarning("The player is already paused! Use `"+event.getClient().getPrefix()+"play` to unpause!");
            return;
        }
        handler.getPlayer().setPaused(true);
        event.replySuccess("Paused **"+handler.getPlayer().getPlayingTrack().getInfo().title+"**. Type `"+event.getClient().getPrefix()+"play` to unpause!");
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if(handler.getPlayer().isPaused())
        {
            event.reply( getClient().getWarning()+ "The player is already paused! Use `"+getClient().getPrefix()+"play` to unpause!");
            return;
        }
        handler.getPlayer().setPaused(true);
        event.reply( getClient().getSuccess()+ "Paused **"+handler.getPlayer().getPlayingTrack().getInfo().title+"**. Type `"+getClient().getPrefix()+"play` to unpause!");

    }
}

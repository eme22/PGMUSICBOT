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
package com.eme22.bolo.commands.music;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.commands.MusicCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
import org.springframework.stereotype.Component;

@Component
public class ShuffleCmd extends MusicCommand 
{

    @Value("${config.aliases.shuffle:}")
    String[] aliases = new String[0];

    public ShuffleCmd(Bot bot)
    {
        super(bot);
        this.name = "shuffle";
        this.help = "shuffles songs you have added";
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) 
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        int s = handler.getQueue().shuffle(event.getAuthor().getIdLong());
        switch (s) 
        {
            case 0:
                event.replyError("You don't have any music in the queue to shuffle!");
                break;
            case 1:
                event.replyWarning("You only have one song in the queue!");
                break;
            default:
                event.replySuccess("You successfully shuffled your "+s+" entries.");
                break;
        }
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        int s = handler.getQueue().shuffle(event.getUser().getIdLong());
        switch (s)
        {
            case 0:
                event.reply(event.getClient().getError()+"You don't have any music in the queue to shuffle!").queue();
                break;
            case 1:
                event.reply(event.getClient().getWarning()+ "You only have one song in the queue!").queue();
                break;
            default:
                event.reply(event.getClient().getSuccess()+"You successfully shuffled your "+s+" entries.").queue();
                break;
        }
    }

}

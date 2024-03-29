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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
import org.springframework.stereotype.Component;

@Component
public class NowplayingCmd extends MusicCommand {

    @Value("${config.aliases.nowplaying:}")
    String[] aliases = new String[0];

    public NowplayingCmd(Bot bot) {
        super(bot);
        this.name = "nowplaying";
        this.help = "shows the song that is currently playing";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        MessageCreateData m = handler.getNowPlaying(event.getJDA());
        if(m==null)
        {
            event.reply(handler.getNoMusicPlaying(event.getJDA()));
            bot.getNowPlayingHandler().clearLastNPMessage(event.getGuild());
        }
        else
        {
            event.reply(m, msg -> {
                msg.addReaction(Emoji.fromFormatted("U+23EF")).queue();
                msg.addReaction(Emoji.fromFormatted("U+23ED")).queue();
                msg.addReaction(Emoji.fromFormatted("U+1F507")).queue();
                msg.addReaction(Emoji.fromFormatted("U+1F4C3")).queue();
                msg.addReaction(Emoji.fromFormatted("U+1F3B5")).queue();
                bot.getNowPlayingHandler().setLastNPMessage(msg);
            });
        }
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        MessageCreateData m = handler.getNowPlaying(event.getJDA());
        if(m==null)
        {
            event.reply(handler.getNoMusicPlaying(event.getJDA())).queue();
            bot.getNowPlayingHandler().clearLastNPMessage(event.getGuild());
        }
        else
        {
            event.reply(m).queue( s -> s.retrieveOriginal().queue(msg -> {
                msg.addReaction(Emoji.fromFormatted("U+23EF")).queue();
                msg.addReaction(Emoji.fromFormatted("U+23ED")).queue();
                msg.addReaction(Emoji.fromFormatted("U+1F507")).queue();
                msg.addReaction(Emoji.fromFormatted("U+1F4C3")).queue();
                msg.addReaction(Emoji.fromFormatted("U+1F3B5")).queue();
                bot.getNowPlayingHandler().setLastNPMessage(msg);
            }) );
        }
    }
}

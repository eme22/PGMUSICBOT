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
package com.eme22.bolo.commands.music;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.audio.QueuedTrack;
import com.eme22.bolo.commands.MusicCommand;
import com.eme22.bolo.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
import org.springframework.stereotype.Component;

@Component
public class SearchCmd extends MusicCommand {

    @Value("${config.aliases.search:}")
    String[] aliases = new String[0];

    @Value("${config.searching}")
    private String searchingEmoji;
    protected String searchPrefix = "ytsearch:";
    private final OrderedMenu.Builder builder;

    public SearchCmd(Bot bot)
    {
        super(bot);
        this.name = "search";
        this.arguments = "<query>";
        this.help = "searches Youtube for a provided query";
        this.beListening = true;
        this.bePlaying = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "busqueda", "Busca la cancion, playlist o link que desea reproducir.").setRequired(true));
        builder = new OrderedMenu.Builder()
                .allowTextInput(true)
                .useNumbers()
                .useCancelButton(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1, TimeUnit.MINUTES);
    }
    @Override
    public void doCommand(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError("Por favor incluya una busqueda.");
            return;
        }
        event.reply(searchingEmoji+" Searching... `["+event.getArgs()+"]`", 
                m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), searchPrefix + event.getArgs(), new ResultHandler(m,event, null)));
    }

    @Override
    public void doCommand(SlashCommandEvent event) {

        OptionMapping args = event.getOption("busqueda");

        if(args == null || args.getAsString().isEmpty())
        {
            event.reply(event.getClient().getWarning()+"Please include a query.").queue();
            return;
        }
        event.reply(searchingEmoji+" Searching... `["+args.getAsString().isEmpty()+"]`").queue( s -> s.retrieveOriginal().queue(
                m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), searchPrefix + args.getAsString().isEmpty(), new ResultHandler(m, null, event))));

    }

    private class ResultHandler implements AudioLoadResultHandler 
    {
        private final Message m;
        private final CommandEvent event;
        private final SlashCommandEvent slashEvent;
        private final String args;
        
        private ResultHandler(Message m, CommandEvent event, SlashCommandEvent slashEvent)
        {
            this.m = m;
            this.event = event;
            this.slashEvent = slashEvent;
            this.args = slashEvent != null ? slashEvent.getOption("busqueda", OptionMapping::getAsString) : event.getArgs();
        }
        
        @Override
        public void trackLoaded(AudioTrack track)
        {
            if(bot.getPlayerManager().isTooLong(track))
            {
                m.editMessage(FormatUtil.filter((slashEvent == null ? event.getClient() : slashEvent.getClient()).getWarning()+" This track (**"+track.getInfo().title+"**) is longer than the allowed maximum: `"
                        +FormatUtil.formatTime(track.getDuration())+"` > `"+bot.getPlayerManager().getMaxTime()+"`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler)(slashEvent == null ? event.getGuild() : slashEvent.getGuild()).getAudioManager().getSendingHandler();
            int pos = handler.addTrack(new QueuedTrack(track, (slashEvent == null ? event.getAuthor() : slashEvent.getUser())))+1;
            m.editMessage(FormatUtil.filter((slashEvent == null ? event.getClient() : slashEvent.getClient()).getSuccess()+" Added **"+track.getInfo().title
                    +"** (`"+FormatUtil.formatTime(track.getDuration())+"`) "+(pos==0 ? "to begin playing" 
                        : " to the queue at position "+pos))).queue();
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist)
        {
            builder.setColor((slashEvent == null ? event.getGuild().getSelfMember() : slashEvent.getGuild().getSelfMember()).getColor())
                    .setText(FormatUtil.filter((slashEvent == null ? event.getClient() : slashEvent.getClient()).getSuccess()+" Search results for `"+ (slashEvent == null ? event.getArgs() : args)+"`:"))
                    .setChoices()
                    .setSelection((msg,i) -> 
                    {
                        AudioTrack track = playlist.getTracks().get(i-1);
                        if(bot.getPlayerManager().isTooLong(track))
                        {
                            if (slashEvent == null)
                                event.replyWarning("This track (**"+track.getInfo().title+"**) is longer than the allowed maximum: `"
                                    +FormatUtil.formatTime(track.getDuration())+"` > `"+bot.getPlayerManager().getMaxTime()+"`");
                            else
                                slashEvent.reply(event.getClient().getWarning()+ "This track (**"+track.getInfo().title+"**) is longer than the allowed maximum: `"
                                        +FormatUtil.formatTime(track.getDuration())+"` > `"+bot.getPlayerManager().getMaxTime()+"`").queue();
                            return;
                        }
                        AudioHandler handler = (AudioHandler)(slashEvent == null ? event.getGuild() : slashEvent.getGuild()).getAudioManager().getSendingHandler();
                        int pos = handler.addTrack(new QueuedTrack(track, (slashEvent == null ? event.getAuthor() : slashEvent.getUser())))+1;
                        event.replySuccess("Added **" + FormatUtil.filter(track.getInfo().title)
                                + "** (`" + FormatUtil.formatTime(track.getDuration()) + "`) " + (pos==0 ? "to begin playing" 
                                    : " to the queue at position "+pos));
                    })
                    .setCancel((msg) -> {})
                    .setUsers((slashEvent == null ? event.getAuthor() : slashEvent.getUser()))
            ;
            for(int i=0; i<4 && i<playlist.getTracks().size(); i++)
            {
                AudioTrack track = playlist.getTracks().get(i);
                builder.addChoices("`["+FormatUtil.formatTime(track.getDuration())+"]` [**"+track.getInfo().title+"**]("+track.getInfo().uri+")");
            }
            builder.build().display(m);
        }

        @Override
        public void noMatches() 
        {
            m.editMessage(FormatUtil.filter((slashEvent == null ? event.getClient() : slashEvent.getClient()).getWarning()+" No results found for `"+(slashEvent == null ? event.getArgs() : args)+"`.")).queue();
        }

        @Override
        public void loadFailed(FriendlyException throwable) 
        {
            if(throwable.severity==Severity.COMMON)
                m.editMessage((slashEvent == null ? event.getClient() : slashEvent.getClient()).getError()+" Error loading: "+throwable.getMessage()).queue();
            else
                m.editMessage((slashEvent == null ? event.getClient() : slashEvent.getClient()).getError()+" Error loading track.").queue();
        }
    }
}

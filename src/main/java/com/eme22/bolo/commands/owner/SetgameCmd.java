/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
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
package com.eme22.bolo.commands.owner;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
import org.springframework.stereotype.Component;

@Component
public class SetgameCmd extends OwnerCommand {

    @Value("${config.aliases.setgame:}")
    String[] aliases = new String[0];

    public SetgameCmd(Bot bot)
    {
        this.name = "setgame";
        this.help = "sets the game the bot is playing";
        this.arguments = "[action] [game]";
        this.guildOnly = false;
        this.children = new OwnerCommand[]{
                new SetlistenCmd(),
                new SetstreamCmd(),
                new SetwatchCmd()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        String title = " a";
        //String title = event.getArgs().toLowerCase().startsWith("playing") ? event.getArgs().substring(7).trim() : event.getArgs();
        try
        {
            event.getJDA().getPresence().setActivity(title.isEmpty() ? null : Activity.playing(title));
            event.reply(event.getClient().getSuccess()+" **"+event.getJDA().getSelfUser().getName()
                    +"** is "+(title.isEmpty() ? "no longer playing anything." : "now playing `"+title+"`")).queue();
        }
        catch(Exception e)
        {
            event.reply(event.getClient().getError()+" The game could not be set!").queue();
        }

    }

    @Override
    protected void execute(CommandEvent event) 
    {
        String title = event.getArgs().toLowerCase().startsWith("playing") ? event.getArgs().substring(7).trim() : event.getArgs();
        try
        {
            event.getJDA().getPresence().setActivity(title.isEmpty() ? null : Activity.playing(title));
            event.reply(event.getClient().getSuccess()+" **"+event.getSelfUser().getName()
                    +"** is "+(title.isEmpty() ? "no longer playing anything." : "now playing `"+title+"`"));
        }
        catch(Exception e)
        {
            event.reply(event.getClient().getError()+" The game could not be set!");
        }
    }
    
    private static class SetstreamCmd extends OwnerCommand
    {
        private SetstreamCmd()
        {
            this.name = "stream";
            this.aliases = new String[]{"twitch","streaming"};
            this.help = "sets the game the bot is playing to a stream";
            this.arguments = "<username> <game>";
            this.guildOnly = false;
            this.options = Arrays.asList(
                    new OptionData(OptionType.STRING, "username", "Usuario de Twitch").setRequired(true),
                    new OptionData(OptionType.STRING, "game", "Juego a mostrar").setRequired(true)
            );
        }

        @Override
        protected void execute(CommandEvent event)
        {
            String[] parts = event.getArgs().split("\\s+", 2);
            if(parts.length<2)
            {
                event.replyError("Please include a twitch username and the name of the game to 'stream'");
                return;
            }
            try
            {
                event.getJDA().getPresence().setActivity(Activity.streaming(parts[1], "https://twitch.tv/"+parts[0]));
                event.replySuccess("**"+event.getSelfUser().getName()
                        +"** is now streaming `"+parts[1]+"`");
            }
            catch(Exception e)
            {
                event.reply(event.getClient().getError()+" The game could not be set!");
            }
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String user = event.optString("username", null);
            String game = event.optString("game", null);

            if (game == null) {
                event.reply(event.getClient().getError()+" No se puede setear ese juego!").queue();
                return;
            }

            try
            {
                event.getJDA().getPresence().setActivity(Activity.streaming(game, "https://twitch.tv/"+user));
                event.reply(event.getClient().getSuccess()+  "**"+event.getJDA().getSelfUser().getName()
                        +"** is now streaming `"+game+"`").queue();
            }
            catch(Exception e)
            {
                event.reply(event.getClient().getError()+" No se puede setear ese juego!").queue();
            }

        }


    }
    
    private static class SetlistenCmd extends OwnerCommand
    {
        private SetlistenCmd()
        {
            this.name = "listen";
            this.aliases = new String[]{"listening"};
            this.help = "sets the game the bot is listening to";
            this.arguments = "<title>";
            this.guildOnly = false;
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "title", "Setea la cancion que el bot esta escuchando")
                    .setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String title = event.optString("title", null);

            if(title == null)
            {
                event.reply(event.getClient().getError()+ " Por favor incluya un titulo!").queue();
                return;
            }

            try
            {
                event.getJDA().getPresence().setActivity(Activity.listening(title));
                event.reply(event.getClient().getSuccess()+  " **"+event.getJDA().getSelfUser().getName()+"** esta escuchando a `"+title+"`").queue();
            } catch(Exception e) {
                event.reply(event.getClient().getError()+" El juego no se puede setear!").queue();
            }
        }

        @Override
        protected void execute(CommandEvent event)
        {
            if(event.getArgs().isEmpty())
            {
                event.replyError("Please include a title to listen to!");
                return;
            }
            String title = event.getArgs().toLowerCase().startsWith("to") ? event.getArgs().substring(2).trim() : event.getArgs();
            try
            {
                event.getJDA().getPresence().setActivity(Activity.listening(title));
                event.replySuccess("**"+event.getSelfUser().getName()+"** is now listening to `"+title+"`");
            } catch(Exception e) {
                event.reply(event.getClient().getError()+" The game could not be set!");
            }
        }
    }
    
    private static class SetwatchCmd extends OwnerCommand
    {
        private SetwatchCmd()
        {
            this.name = "watch";
            this.aliases = new String[]{"watching"};
            this.help = "sets the game the bot is watching";
            this.arguments = "<title>";
            this.guildOnly = false;
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "title", "Setea el video que el bot esta viendo")
                    .setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {

            String title = event.optString("title", null);

            if(title == null || title.isEmpty())
            {
                event.reply("Incluya un titulo!").queue();
                return;
            }

            try
            {
                event.getJDA().getPresence().setActivity(Activity.watching(title));
                event.reply( event.getClient().getSuccess() + " **"+event.getJDA().getSelfUser().getName()+"** is now watching `"+title+"`").queue();
            } catch(Exception e) {
                event.reply(event.getClient().getError()+" No se puede setear el titulo!").queue();
            }

        }

        @Override
        protected void execute(CommandEvent event)
        {
            if(event.getArgs().isEmpty())
            {
                event.replyError("Please include a title to watch!");
                return;
            }
            String title = event.getArgs();
            try
            {
                event.getJDA().getPresence().setActivity(Activity.watching(title));
                event.replySuccess("**"+event.getSelfUser().getName()+"** is now watching `"+title+"`");
            } catch(Exception e) {
                event.replyError(" No se puede setear el titulo!");
            }
        }
    }
}

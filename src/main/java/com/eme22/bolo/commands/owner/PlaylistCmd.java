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
package com.eme22.bolo.commands.owner;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.OwnerCommand;
import com.eme22.bolo.playlist.PlaylistLoader.Playlist;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
import org.springframework.stereotype.Component;

@Component
public class PlaylistCmd extends OwnerCommand 
{
    private final Bot bot;
    @Value("${config.aliases.playlist:}")
    String[] aliases = new String[0];


    public PlaylistCmd(Bot bot)
    {
        this.bot = bot;
        this.guildOnly = false;
        this.name = "playlist";
        this.arguments = "<append|delete|make|setdefault>";
        this.help = "playlist management";
        this.children = new OwnerCommand[]{
            new ListCmd(),
            new AppendlistCmd(),
            new DeletelistCmd(),
            new MakelistCmd(),
            new DefaultlistCmd(bot)
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        StringBuilder builder = new StringBuilder(event.getClient().getWarning()+" Playlist Management Commands:\n");
        for(Command cmd: this.children)
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments()==null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        event.reply(builder.toString()).queue();
    }

    @Override
    public void execute(CommandEvent event) 
    {
        StringBuilder builder = new StringBuilder(event.getClient().getWarning()+" Playlist Management Commands:\n");
        for(Command cmd: this.children)
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments()==null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        event.reply(builder.toString());
    }


public class MakelistCmd extends OwnerCommand 
    {
        public MakelistCmd()
        {
            this.name = "make";
            this.aliases = new String[]{"create"};
            this.help = "makes a new playlist";
            this.arguments = "<name>";
            this.guildOnly = false;
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "Playlist name").setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {

            String pname = event.getOption("name", OptionMapping::getAsString);

            if(bot.getPlaylistLoader().getPlaylist(pname)==null)
            {
                try
                {
                    bot.getPlaylistLoader().createPlaylist(pname);
                    event.reply(event.getClient().getSuccess()+" Successfully created playlist `"+pname+"`!").queue();
                }
                catch(IOException e)
                {
                    event.reply(event.getClient().getError()+" I was unable to create the playlist: "+e.getLocalizedMessage()).queue();
                }
            }
            else
                event.reply(event.getClient().getError()+" Playlist `"+pname+"` already exists!").queue();

        }

        @Override
        protected void execute(CommandEvent event) 
        {
            String pname = event.getArgs().replaceAll("\\s+", "_");
            if(bot.getPlaylistLoader().getPlaylist(pname)==null)
            {
                try
                {
                    bot.getPlaylistLoader().createPlaylist(pname);
                    event.replySuccess(" Successfully created playlist `"+pname+"`!");
                }
                catch(IOException e)
                {
                    event.replyError(" I was unable to create the playlist: "+e.getLocalizedMessage());
                }
            }
            else
                event.replyError(" Playlist `"+pname+"` already exists!");
        }
    }

public class DeletelistCmd extends OwnerCommand 
    {
        public DeletelistCmd()
        {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "deletes an existing playlist";
            this.arguments = "<name>";
            this.guildOnly = false;
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "Playlist name").setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String pname = event.getOption("name", OptionMapping::getAsString);
            if(bot.getPlaylistLoader().getPlaylist(pname)==null)
                event.reply(event.getClient().getError()+" Playlist `"+pname+"` doesn't exist!").queue();
            else
            {
                try
                {
                    bot.getPlaylistLoader().deletePlaylist(pname);
                    event.reply(event.getClient().getSuccess()+" Successfully deleted playlist `"+pname+"`!").queue();
                }
                catch(IOException e)
                {
                    event.reply(event.getClient().getError()+" I was unable to delete the playlist: "+e.getLocalizedMessage()).queue();
                }
            }
        }

        @Override
        protected void execute(CommandEvent event) 
        {
            String pname = event.getArgs().replaceAll("\\s+", "_");
            if(bot.getPlaylistLoader().getPlaylist(pname)==null)
                event.reply(event.getClient().getError()+" Playlist `"+pname+"` doesn't exist!");
            else
            {
                try
                {
                    bot.getPlaylistLoader().deletePlaylist(pname);
                    event.reply(event.getClient().getSuccess()+" Successfully deleted playlist `"+pname+"`!");
                }
                catch(IOException e)
                {
                    event.reply(event.getClient().getError()+" I was unable to delete the playlist: "+e.getLocalizedMessage());
                }
            }
        }
    }

public class AppendlistCmd extends OwnerCommand 
    {
        public AppendlistCmd()
        {
            this.name = "append";
            this.aliases = new String[]{"add"};
            this.help = "appends songs to an existing playlist";
            this.arguments = "<name> <URL> | <URL> | ...";
            this.guildOnly = false;
            this.options = Arrays.asList(
                    new OptionData(OptionType.STRING, "name", "nombre de la playlist").setRequired(true),
                    new OptionData(OptionType.STRING, "url1", "link del video 1").setRequired(true),
                    new OptionData(OptionType.STRING, "url2", "link del video 2").setRequired(true),
                    new OptionData(OptionType.STRING, "url3", "link del video 3").setRequired(true),
                    new OptionData(OptionType.STRING, "url4", "link del video 4").setRequired(true),
                    new OptionData(OptionType.STRING, "url5", "link del video 5").setRequired(true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {

            String pname = event.getOption("name", OptionMapping::getAsString);

            String[] parts = event.getOptionsByName("url").stream().map(OptionMapping::getAsString).toArray(String[]::new);

            StringBuilder builder = new StringBuilder();

            for(String url: parts) {
                builder.append("\r\n").append(url);
            }

            try
            {
                bot.getPlaylistLoader().writePlaylist(pname, builder.toString());
                event.reply(event.getClient().getSuccess()+" Successfully added "+parts.length+" items to playlist `"+pname+"`!").queue();
            }
            catch(IOException e)
            {
                event.reply(event.getClient().getError()+" I was unable to append to the playlist: "+e.getLocalizedMessage()).queue();
            }

        }

        @Override
        protected void execute(CommandEvent event) 
        {
            String[] parts = event.getArgs().split("\\s+", 2);
            if(parts.length<2)
            {
                event.reply(event.getClient().getError()+" Please include a playlist name and URLs to add!");
                return;
            }
            String pname = parts[0];
            Playlist playlist = bot.getPlaylistLoader().getPlaylist(pname);
            if(playlist==null)
                event.reply(event.getClient().getError()+" Playlist `"+pname+"` doesn't exist!");
            else
            {
                StringBuilder builder = new StringBuilder();
                playlist.getItems().forEach(item -> builder.append("\r\n").append(item));
                String[] urls = parts[1].split("\\|");
                for(String url: urls)
                {
                    String u = url.trim();
                    if(u.startsWith("<") && u.endsWith(">"))
                        u = u.substring(1, u.length()-1);
                    builder.append("\r\n").append(u);
                }
                try
                {
                    bot.getPlaylistLoader().writePlaylist(pname, builder.toString());
                    event.reply(event.getClient().getSuccess()+" Successfully added "+urls.length+" items to playlist `"+pname+"`!");
                }
                catch(IOException e)
                {
                    event.reply(event.getClient().getError()+" I was unable to append to the playlist: "+e.getLocalizedMessage());
                }
            }
        }
    }
    

public class DefaultlistCmd extends AutoplaylistCmd 
    {
        public DefaultlistCmd(Bot bot)
        {
            super(bot);
            this.name = "setdefault";
            this.aliases = new String[]{"default"};
            this.arguments = "<playlistname|NONE>";
            this.guildOnly = true;
        }
    }

public class ListCmd extends OwnerCommand 
    {
        public ListCmd()
        {
            this.name = "all";
            this.aliases = new String[]{"available","list"};
            this.help = "lists all available playlists";
            this.guildOnly = true;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            if(!bot.getPlaylistLoader().folderExists())
                bot.getPlaylistLoader().createFolder();
            if(!bot.getPlaylistLoader().folderExists())
            {
                event.reply(event.getClient().getWarning()+" Playlists folder does not exist and could not be created!").queue();
                return;
            }
            List<String> list = bot.getPlaylistLoader().getPlaylistNames();
            if(list==null)
                event.reply(event.getClient().getError()+" Failed to load available playlists!").queue();
            else if(list.isEmpty())
                event.reply(event.getClient().getWarning()+" There are no playlists in the Playlists folder!").queue();
            else
            {
                StringBuilder builder = new StringBuilder(event.getClient().getSuccess()+" Available playlists:\n");
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString()).queue();
            }
        }

        @Override
        protected void execute(CommandEvent event) 
        {
            if(!bot.getPlaylistLoader().folderExists())
                bot.getPlaylistLoader().createFolder();
            if(!bot.getPlaylistLoader().folderExists())
            {
                event.replyWarning(" Playlists folder does not exist and could not be created!");
                return;
            }
            List<String> list = bot.getPlaylistLoader().getPlaylistNames();
            if(list==null)
                event.replyError(" Failed to load available playlists!");
            else if(list.isEmpty())
                event.replyWarning(" There are no playlists in the Playlists folder!");
            else
            {
                StringBuilder builder = new StringBuilder(event.getClient().getSuccess()+" Available playlists:\n");
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString());
            }
        }
    }
}

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
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
import org.springframework.stereotype.Component;

@Component
public class DebugCmd extends OwnerCommand 
{
    private final static String[] PROPERTIES = {"java.version", "java.vm.name", "java.vm.specification.version", 
        "java.runtime.name", "java.runtime.version", "java.specification.version",  "os.arch", "os.name"};

    @Value("${config.aliases.debug:}")
    String[] aliases = new String[0];

    @Value("${config.owner}")
    private long owner;
    @Value("${config.prefix}")
    private String prefix;
    @Value("${config.altprefix}")
    private String altprefix;
    @Value("${config.maxseconds}")
    private long maxSeconds;
    @Value("${config.nowplayingimages}")
    private boolean npImages;
    @Value("${config.stayinchannel}")
    private boolean stayInChannel;
    @Value("${config.songinstatus}")
    private boolean songInStatus;
    @Value("${config.eval}")
    private boolean useEval;
    @Value("${config.update}")
    private boolean updatealerts;

    private final Bot bot;
    private final BuildProperties buildProperties;
    
    public DebugCmd(Bot bot, BuildProperties buildProperties)
    {
        this.bot = bot;
        this.buildProperties = buildProperties;
        this.name = "debug";
        this.help = "shows debug info";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String sb = getDebugMessage(event.getJDA());

        if(event.isFromType(ChannelType.PRIVATE)
                || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ATTACH_FILES))
            event.getChannel().sendFiles(FileUpload.fromData(sb.getBytes(),"debug_information.txt")).queue();
        else
            event.reply("Debug Information: " + sb);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply("Debug Information: " + getDebugMessage(event.getJDA())).queue();
    }

    private String getDebugMessage(JDA jda) {
        StringBuilder sb = new StringBuilder();
        sb.append("```\nSystem Properties:");
        for(String key: PROPERTIES)
            sb.append("\n  ").append(key).append(" = ").append(System.getProperty(key));
        sb.append("\n\nJMusicBot Information:")
                .append("\n  Version = ").append(buildProperties.getVersion())
                .append("\n  Owner = ").append(owner)
                .append("\n  Prefix = ").append(prefix)
                .append("\n  AltPrefix = ").append(altprefix)
                .append("\n  MaxSeconds = ").append(maxSeconds)
                .append("\n  NPImages = ").append(npImages)
                .append("\n  SongInStatus = ").append(songInStatus)
                .append("\n  StayInChannel = ").append(stayInChannel)
                .append("\n  UseEval = ").append(useEval)
                .append("\n  UpdateAlerts = ").append(updatealerts);
        sb.append("\n\nDependency Information:")
                .append("\n  JDA Version = ").append(JDAInfo.VERSION)
                .append("\n  JDA-Utilities Version = ").append(JDAUtilitiesInfo.VERSION)
                .append("\n  Lavaplayer Version = ").append(PlayerLibrary.VERSION);
        long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long used = total - (Runtime.getRuntime().freeMemory() / 1024 / 1024);
        sb.append("\n\nRuntime Information:")
                .append("\n  Total Memory = ").append(total)
                .append("\n  Used Memory = ").append(used);
        sb.append("\n\nDiscord Information:")
                .append("\n  ID = ").append(jda.getSelfUser().getId())
                .append("\n  Guilds = ").append(jda.getGuildCache().size())
                .append("\n  Users = ").append(jda.getUserCache().size());
        sb.append("\n```");
        return sb.toString();
    }

}

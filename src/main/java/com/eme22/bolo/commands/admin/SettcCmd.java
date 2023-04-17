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
package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
import org.springframework.stereotype.Component;

@Component
public class SettcCmd extends AdminCommand 
{

    @Value("${config.aliases.settc:}")
    String[] aliases = new String[0];

    public SettcCmd(@Qualifier("adminCategory") Category category)
    {
        super(category);
        this.name = "settc";
        this.help = "especifica un canal para los comandos de musica";
        this.arguments = "<channel|NONE>";
        this.options = Collections.singletonList(new OptionData(OptionType.CHANNEL, "canal", "canal a poner para solo comandos de musica.").setRequired(false));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping option = event.getOption("canal");

        TextChannel channel = null;
        Server s = event.getClient().getSettingsFor(event.getGuild());

        if (option != null){
            if (option.getAsChannel().getType().equals(ChannelType.TEXT))
                channel = option.getAsChannel().asTextChannel();

        }

        else {
            s.setTextChannelId(0);
            s.save();
            event.reply(event.getClient().getSuccess()+" Los comandos de música se pueden utilizar ahora en cualquier canal").queue();
            return;
        }

        if (channel == null) {
            event.reply("Asegurese de que es un canal de texto").setEphemeral(true).queue();
            return;
        }

        s.setTextChannelId(channel.getIdLong());
        s.save();
        event.reply(event.getClient().getSuccess()+" Music commands can now only be used in <#"+channel.getId()+">").queue();


    }

    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError(" Por favor, incluya un canal de texto o NONE");
            return;
        }
        Server s = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            s.setTextChannelId(0);
            s.save();
            event.replySuccess(" Los comandos de música se pueden utilizar ahora en cualquier canal");
        }
        else
        {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if(list.isEmpty())
                event.replyWarning(" No Text Channels found matching \""+event.getArgs()+"\"");
            else if (list.size()>1)
                event.replyWarning(FormatUtil.listOfTChannels(list, event.getArgs()));
            else
            {
                s.setTextChannelId(list.get(0).getIdLong());
                s.save();
                event.replySuccess(" Los comandos de música ahora sólo se pueden utilizar en <#"+list.get(0).getId()+">");
            }
        }
    }
    
}

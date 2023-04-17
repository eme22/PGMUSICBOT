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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
public class SetGoodByeCmd extends AdminCommand
{

    @Value("${config.aliases.setgoodbye:}")
    String[] aliases = new String[0];

    public SetGoodByeCmd(@Qualifier("adminCategory") Category category)
    {
        super(category);
        this.name = "setgoodbye";
        this.help = "especifica un canal para las despedidas";
        this.arguments = "<channel|NONE>";
        this.options = Collections.singletonList(new OptionData(OptionType.CHANNEL, "canal", "canal a poner para mensaje de despedidas. Se utilizara el canal por defecto si esta activado.").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        TextChannel channel = event.getOption("canal").getAsChannel().asTextChannel();
        Server s = event.getClient().getSettingsFor(event.getGuild());
        s.setDespedidasChannelId(channel.getIdLong());
        s.save();
        event.reply(event.getClient().getSuccess()+" El canal de las despedidas es ahora <#"+channel.getId()+">").queue();

    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.reply(event.getClient().getError()+" Ponga un canal de texto o NONE");
            return;
        }
        Server s = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            s.setDespedidasChannelId(0);
            s.save();
            event.reply(event.getClient().getSuccess()+" El canal de las despedidas ha sido quitado.");
        }
        else
        {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if(list.isEmpty())
                event.reply(event.getClient().getWarning()+" No Text Channels found matching \""+event.getArgs()+"\"");
            else if (list.size()>1)
                event.reply(event.getClient().getWarning()+FormatUtil.listOfTChannels(list, event.getArgs()));
            else
            {
                s.setDespedidasChannelId(list.get(0).getIdLong());
                s.save();
                event.reply(event.getClient().getSuccess()+" El canal de las despedidas es ahora <#"+list.get(0).getId()+">");
            }
        }
    }
    
}

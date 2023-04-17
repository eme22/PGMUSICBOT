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
public class DeleteImageChannel extends AdminCommand
{

    @Value("${config.aliases.delimagechannel:}")
    String[] aliases = new String[0];

    public DeleteImageChannel(@Qualifier("adminCategory") Category category)
    {
        super(category);
        this.name = "delimagechannel";
        this.help = "elimina un canal de la lista de no texto";
        this.arguments = "<channel>";
        this.options = Collections.singletonList(new OptionData(OptionType.CHANNEL, "canal", "selecciona el canal a quitar.").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        TextChannel textChannel = event.getOption("canal").getAsChannel().asTextChannel();
        Server s = event.getClient().getSettingsFor(event.getGuild());
        if (s.isOnlyImageChannel(textChannel)){
            s.removeFromOnlyImageChannels(textChannel);
            s.save();
            event.reply(event.getClient().getSuccess()+" Canal <#"+textChannel.getId()+"> quitado de la lista de canales sin texto").queue();
        }
        else {
            event.reply(event.getClient().getError() + " Canal <#"+textChannel.getId()+"> no esta en la lista de canales sin texto").setEphemeral(true).queue();
        }

    }

    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError(" Incluya un canal de Texto");
            return;
        }
        Server s = event.getClient().getSettingsFor(event.getGuild());
        List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
        if(list.isEmpty())
            event.replyWarning(" No se han encontrado canales de texto que coincidan con \""+event.getArgs()+"\"");
        else
        {
            list.forEach( textChannel -> {
                if (s.isOnlyImageChannel(textChannel)) {
                    s.removeFromOnlyImageChannels(textChannel);
                    s.save();
                    event.replySuccess(" Canal <#" + textChannel.getId() + "> quitado de la lista de canales sin texto");
                }
                else {
                    event.replyError(" Canal <#"+textChannel.getId()+"> no esta en la lista de canales sin texto");
                }
            });

        }

    }
    
}

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
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class DeleteImageChannel extends AdminCommand
{
    public DeleteImageChannel(Bot bot)
    {
        this.name = "delimagechannel";
        this.help = "elimina un canal de la lista de no texto";
        this.arguments = "<channel>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.reply(event.getClient().getError()+" Incluya un canal de Texto");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
        if(list.isEmpty())
            event.reply(event.getClient().getWarning()+" No Text Channels found matching \""+event.getArgs()+"\"");
        else
        {
            list.forEach( textChannel -> {
                s.removeFromOnlyImageChannels(textChannel);
                event.reply(event.getClient().getSuccess()+" Canal <#"+textChannel.getId()+"> quitado de la lista de canales sin texto");
            });

        }

    }
    
}

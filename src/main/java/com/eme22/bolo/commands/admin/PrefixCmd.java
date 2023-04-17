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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
import org.springframework.stereotype.Component;

@Component
public class PrefixCmd extends AdminCommand
{

    @Value("${config.aliases.prefix:}")
    String[] aliases = new String[0];

    public PrefixCmd(Bot bot, @Qualifier("adminCategory") Category category)
    {
        super(category);
        this.name = "prefix";
        this.help = "pone un prefijo por servidor";
        this.arguments = "<prefix|NONE>";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "prefix", "Selecciona el prefijo de los comandos (none = limpiar prefijo).").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String prefix = event.optString("prefix");

        Server s = event.getClient().getSettingsFor(event.getGuild());
        if(prefix == null || prefix.equalsIgnoreCase("none"))
        {
            s.setPrefix(null);
            event.reply(event.getClient().getSuccess()+ " Prefijo del servidor limpiado.").queue();
        }
        else
        {
            s.setPrefix(prefix);
            event.reply(event.getClient().getSuccess()+" Prefijo personalizado fijado en `" + prefix + "` en *" + event.getGuild().getName() + "*").queue();
        }

        s.save();

    }

    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError("Please include a prefix or NONE");
            return;
        }
        
        Server s = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            s.setPrefix(null);
            event.replySuccess("Prefijo del servidor limpiado.");
        }
        else
        {
            s.setPrefix(event.getArgs());
            event.replySuccess("Prefijo personalizado fijado en `" + event.getArgs() + "` en *" + event.getGuild().getName() + "*");
        }

        s.save();
    }
}

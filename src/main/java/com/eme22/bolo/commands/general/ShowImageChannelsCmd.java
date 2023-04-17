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
package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShowImageChannelsCmd extends BaseCommand
{

    @Value("${config.aliases.showimgch:}")
    String[] aliases = new String[0];

    public ShowImageChannelsCmd(Bot bot)
    {
        this.name = "showimgch";
        this.help = "muestra los canales de solo imagen listados en el servidor";
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Server s = event.getClient().getSettingsFor(event.getGuild());
        List<Long> onlyimages = s.getImageOnlyChannelsIds();

        StringBuilder builder1 = new StringBuilder();

        onlyimages.forEach( image -> builder1.append(event.getGuild().getTextChannelById(image).getName()).append(" \n"));

        EmbedBuilder ebuilder = new EmbedBuilder()
                .setColor(event.getGuild().getSelfMember().getColor())
                .setDescription(builder1.toString());
        MessageCreateBuilder mbuilder = new MessageCreateBuilder();
        mbuilder.setContent(" ** Canales de solo Imagenes **");
        mbuilder.setEmbeds(ebuilder.build());

        event.reply(mbuilder.build()).queue();
    }

    @Override
    protected void execute(CommandEvent event) 
    {
        Server s = event.getClient().getSettingsFor(event.getGuild());
        List<Long> onlyimages = s.getImageOnlyChannelsIds();

        StringBuilder builder1 = new StringBuilder();

        onlyimages.forEach( image -> builder1.append(event.getGuild().getTextChannelById(image).getName()).append(" \n"));

        EmbedBuilder ebuilder = new EmbedBuilder()
                .setColor(event.getSelfMember().getColor())
                .setDescription(builder1.toString());
        MessageCreateBuilder mbuilder = new MessageCreateBuilder();
        mbuilder.setContent(" ** Canales de solo Imagenes **");
        mbuilder.setEmbeds(ebuilder.build());
        event.reply(mbuilder.build());
    }
    
}

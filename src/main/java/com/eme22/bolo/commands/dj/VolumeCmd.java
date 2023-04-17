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
package com.eme22.bolo.commands.dj;

import com.eme22.bolo.Bot;
import com.eme22.bolo.audio.AudioHandler;
import com.eme22.bolo.commands.DJCommand;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
import org.springframework.stereotype.Component;

@Component
public class VolumeCmd extends DJCommand
{
    @Value("${config.aliases.volume:}")
    String[] aliases = new String[0];

    public VolumeCmd(Bot bot, @Qualifier("djCategory") Category category)
    {
        super(bot, category);
        this.name = "volume";
        this.help = "sets or shows volume";
        this.arguments = "[0-999]";
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "volumen", "Setea el volumen seleccionado.")
                .setMinValue(0)
                .setMaxValue(999)
                .setRequired(false));
    }

    @Override
    public void doCommand(CommandEvent event)
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        int volume = handler.getPlayer().getVolume();
        if(event.getArgs().isEmpty())
        {
            event.reply(FormatUtil.volumeIcon(volume)+" El volumen es `"+volume+"`");
        }
        else
        {
            int nvolume;
            try{
                nvolume = Integer.parseInt(event.getArgs());
            }catch(NumberFormatException e){
                event.replyError(" El volumen debe ser un numero!");
                return;
            }
            if(nvolume<0 || nvolume>999)
                event.replyError(" El volumen debe estar entre  0 y 999!");
            else
            {
                handler.getPlayer().setVolume(nvolume);
                Server settings = event.getClient().getSettingsFor(event.getGuild());
                settings.setVolume(nvolume);
                settings.save();

                event.reply(FormatUtil.volumeIcon(nvolume)+" Volumen cambiado de `"+volume+"` a `"+nvolume+"`");
            }
        }
    }

    @Override
    public void doCommand(SlashCommandEvent event) {

        OptionMapping option = event.getOption("volumen");
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();

        int volume = handler.getPlayer().getVolume();
        if(option == null) {
            event.reply(FormatUtil.volumeIcon(volume)+" El volumen es `"+volume+"`").queue();
        }
        else {
            int nvolume = Integer.parseInt(option.getAsString());
            if(nvolume<0 || nvolume>999)
                event.reply(event.getClient().getError()+" El volumen debe estar entre  0 y 999!").setEphemeral(true).queue();
            else
            {
                handler.getPlayer().setVolume(nvolume);
                Server settings = event.getClient().getSettingsFor(event.getGuild());
                settings.setVolume(nvolume);
                settings.save();
                event.reply(FormatUtil.volumeIcon(nvolume)+" Volume cambiado de `"+volume+"` a `"+nvolume+"`").queue();
            }
        }




    }

}

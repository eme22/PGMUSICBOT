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
package com.eme22.bolo.commands.dj;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.DJCommand;
import com.eme22.bolo.model.RepeatMode;
import com.eme22.bolo.model.Server;
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
public class RepeatCmd extends DJCommand
{

    @Value("${config.aliases.repeat:}")
    String[] aliases = new String[0];

    public RepeatCmd(Bot bot, @Qualifier("djCategory") Category category)
    {
        super(bot, category);
        this.name = "repeat";
        this.help = "re-adds music to the queue when finished";
        this.arguments = "[off|all|single]";
        this.guildOnly = true;
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "modo", "[off-all-single]").setRequired(false));

    }
    
    // override musiccommand's execute because we don't actually care where this is used
    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();
        RepeatMode value;
        Server settings = event.getClient().getSettingsFor(event.getGuild());
        if(args.isEmpty())
        {
            if(settings.getRepeatMode() == RepeatMode.OFF)
                value = RepeatMode.ALL;
            else
                value = RepeatMode.OFF;
        }
        else if(args.equalsIgnoreCase("false") || args.equalsIgnoreCase("off"))
        {
            value = RepeatMode.OFF;
        }
        else if(args.equalsIgnoreCase("true") || args.equalsIgnoreCase("on") || args.equalsIgnoreCase("all"))
        {
            value = RepeatMode.ALL;
        }
        else if(args.equalsIgnoreCase("one") || args.equalsIgnoreCase("single"))
        {
            value = RepeatMode.SINGLE;
        }
        else
        {
            event.replyError("Valid options are `off`, `all` or `single` (or leave empty to toggle between `off` and `all`)");
            return;
        }
        settings.setRepeatMode(value);
        event.replySuccess("Repeat mode is now `"+value.getUserFriendlyName()+"`");
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping option = event.getOption("modo");
        RepeatMode value;
        Server settings = event.getClient().getSettingsFor(event.getGuild());
        String args = null;
        if (option != null) {
            args = option.getAsString();
        }
        if (option == null) {
            if(settings.getRepeatMode() == RepeatMode.OFF)
                value = RepeatMode.ALL;
            else
                value = RepeatMode.OFF;
        }
        else if(args.equalsIgnoreCase("false") || args.equalsIgnoreCase("off"))
        {
            value = RepeatMode.OFF;
        }
        else if(args.equalsIgnoreCase("true") || args.equalsIgnoreCase("on") || args.equalsIgnoreCase("all"))
        {
            value = RepeatMode.ALL;
        }
        else if(args.equalsIgnoreCase("one") || args.equalsIgnoreCase("single"))
        {
            value = RepeatMode.SINGLE;
        }
        else
        {
            event.reply(event.getClient().getError() + "Valid options are `off`, `all` or `single` (or leave empty to toggle between `off` and `all`)").queue();
            return;
        }
        settings.setRepeatMode(value);
        event.reply(event.getClient().getSuccess()+ "Repeat mode is now `"+value.getUserFriendlyName()+"`").queue();
    }

    @Override
    public void doCommand(CommandEvent event) { /* Intentionally Empty */ }

    @Override
    public void doCommand(SlashCommandEvent event) { /* Intentionally Empty */ }
}

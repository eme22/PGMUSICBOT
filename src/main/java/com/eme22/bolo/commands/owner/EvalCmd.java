/*
 * Copyright 2016 John Grosh (jagrosh).
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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Collections;

/**
 *
 * @author John Grosh (jagrosh)
 */
import org.springframework.stereotype.Component;

@Component
public class EvalCmd extends OwnerCommand 
{
    private final Bot bot;

    @Value("${config.aliases.eval:}")
    String[] aliases = new String[0];

    public EvalCmd(@NotNull Bot bot)
    {
        this.bot = bot;
        this.name = "eval";
        this.help = "evaluates nashorn code";
        this.guildOnly = false;
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "code", "Eval Code").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {

        String command = event.getOption("code", OptionMapping::getAsString);

        ScriptEngine se = new ScriptEngineManager().getEngineByName("Nashorn");
        se.put("bot", bot);
        se.put("event", event);
        se.put("jda", event.getJDA());
        se.put("guild", event.getGuild());
        se.put("channel", event.getChannel());
        try
        {
            event.reply(event.getClient().getSuccess()+" Evaluated Successfully:\n```\n"+se.eval(command)+" ```").queue();
        }
        catch(Exception e)
        {
            event.reply(event.getClient().getError()+" An exception was thrown:\n```\n"+e+" ```").queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) 
    {
        String command = event.getArgs();

        ScriptEngine se = new ScriptEngineManager().getEngineByName("Nashorn");
        se.put("bot", bot);
        se.put("event", event);
        se.put("jda", event.getJDA());
        se.put("guild", event.getGuild());
        se.put("channel", event.getChannel());
        try
        {
            event.reply(event.getClient().getSuccess()+" Evaluated Successfully:\n```\n"+se.eval(command)+" ```");
        }
        catch(Exception e)
        {
            event.reply(event.getClient().getError()+" An exception was thrown:\n```\n"+e+" ```");
        }
    }

}

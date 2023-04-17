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
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
import org.springframework.stereotype.Component;

@Component
public class ShutdownCmd extends OwnerCommand {

    @Value("${config.aliases.shutdown:}")
    String[] aliases = new String[0];
    private final Bot bot;
    
    public ShutdownCmd(Bot bot) {
        this.bot = bot;
        this.name = "shutdown";
        this.help = "safely shuts down";
        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(event.getClient().getWarning()+ " Apagando...").queue();
        bot.shutdown();
    }

    @Override
    protected void execute(CommandEvent event)
    {
        event.replyWarning(" Apagando...");
        bot.shutdown();
    }
}

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
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
import org.springframework.stereotype.Component;

@Component
public class SetstatusCmd extends OwnerCommand
{
    @Value("${config.aliases.shutdown:}")
    String[] aliases = new String[0];

    public SetstatusCmd()
    {
        this.name = "setstatus";
        this.help = "sets the status the bot displays";
        this.arguments = "<status>";
        this.guildOnly = false;
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "status", "Setea el status del bot")
                        .addChoices()
                .addChoices(
                        Arrays.asList(new Command.Choice("Online", "ONLINE"),
                                new Command.Choice("Idle", "IDLE"),
                                new Command.Choice("No Molestar","DND"),
                                new Command.Choice("Invisible", "INVISIBLE")
                        )
                )
                .setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        try {

            String url = event.optString("status", null);

            OnlineStatus status = OnlineStatus.fromKey(url);
            if(status==OnlineStatus.UNKNOWN)
            {
                event.reply(event.getClient().getError()+ " Please include one of the following statuses: `ONLINE`, `IDLE`, `DND`, `INVISIBLE`").queue();
            }
            else
            {
                event.getJDA().getPresence().setStatus(status);
                event.reply(event.getClient().getSuccess()+  " Set the status to `"+status.getKey().toUpperCase()+"`").queue();
            }
        } catch(Exception e) {
            event.reply(event.getClient().getError()+" The status could not be set!").queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            OnlineStatus status = OnlineStatus.fromKey(event.getArgs());
            if(status==OnlineStatus.UNKNOWN)
            {
                event.replyError("Please include one of the following statuses: `ONLINE`, `IDLE`, `DND`, `INVISIBLE`");
            }
            else
            {
                event.getJDA().getPresence().setStatus(status);
                event.replySuccess("Set the status to `"+status.getKey().toUpperCase()+"`");
            }
        } catch(Exception e) {
            event.reply(event.getClient().getError()+" The status could not be set!");
        }
    }


}

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
import com.eme22.bolo.utils.OtherUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
import org.springframework.stereotype.Component;

@Component
public class SetavatarCmd extends OwnerCommand {

    @Value("${config.aliases.setavatar:}")
    String[] aliases = new String[0];

    public SetavatarCmd(Bot bot) {
        this.name = "setavatar";
        this.help = "sets the avatar of the bot";
        this.arguments = "<url>";
        this.guildOnly = false;
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "url", "Setea el avatar del bot")
                .setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {

        String url = event.optString("url", null);

        try {

            InputStream s = OtherUtil.imageFromUrl(url);
            if(s==null)
            {
                event.reply(event.getClient().getError()+" Url Invalida!!!").queue();
                return;
            }

            event.getJDA().getSelfUser().getManager().setAvatar(Icon.from(s)).queue(
                    v -> event.reply(event.getClient().getSuccess()+" Successfully changed avatar.").queue(),
                    t -> event.reply(event.getClient().getError()+" Failed to set avatar.").queue());
        } catch(IOException e) {
            event.reply(event.getClient().getError()+" Could not load from provided URL.").queue();
        }



    }

    @Override
    protected void execute(CommandEvent event) {
        String url;
        if(event.getArgs().isEmpty())
            if(!event.getMessage().getAttachments().isEmpty() && event.getMessage().getAttachments().get(0).isImage())
                url = event.getMessage().getAttachments().get(0).getUrl();
            else
                url = null;
        else
            url = event.getArgs();
        InputStream s = OtherUtil.imageFromUrl(url);
        if(s==null)
        {
            event.replyError(" Invalid or missing URL");
        }
        else
        {
            try {
            event.getSelfUser().getManager().setAvatar(Icon.from(s)).queue(
                    v -> event.replySuccess(" Successfully changed avatar."),
                    t -> event.replyError(" Failed to set avatar."));
            } catch(IOException e) {
                event.replyError(" Could not load from provided URL.");
            }
        }
    }
}

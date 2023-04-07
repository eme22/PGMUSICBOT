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
import com.eme22.bolo.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetAdminCmd extends AdminCommand
{
    public SetAdminCmd(Bot bot)
    {
        this.name = "setadmin";
        this.help = "actualiza el rol de Admin";
        this.arguments = "<rolename|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.ROLE, "rol", "rol a poner de admib. Ponga @Everyone para limpiar").setRequired(true));

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Role role = event.getOption("rol").getAsRole();
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(role.getIdLong() == event.getGuild().getIdLong()) {
            s.setAdminRoleId(0);
            event.reply(event.getClient().getSuccess()+"Rol de admin limpiado. Solo el creador del servidor puede usar los comandos de admin.").queue();
        }
        else {
            s.setAdminRoleId(role.getIdLong());
            event.reply(event.getClient().getSuccess()+" Los comandos de admin ahora pueden ser usados por usuarios con el rol **"+role.getAsMention()+"**.").queue();
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        try
        {
        if(event.getArgs().isEmpty())
        {
            event.replyError(" Ponga un rol o NONE para ninguno");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            s.setAdminRoleId(0);
            event.replySuccess(" Rol de admin limpiado. Solo el creador del servidor puede usar los comandos de admin.");
        }
        else
        {
            List<Role> list = FinderUtil.findRoles(event.getArgs(), event.getGuild());
            if(list.isEmpty())
                event.replyWarning(" No Roles found matching \""+event.getArgs()+"\"");
            else if (list.size()>1)
                event.replyWarning(FormatUtil.listOfRoles(list, event.getArgs()));
            else
            {
                s.setAdminRoleId(list.get(0).getIdLong());
                event.replySuccess(" Los comandos de admin ahora pueden ser usados por usuarios con el rol **"+list.get(0).getName()+"** role.");
            }
        }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

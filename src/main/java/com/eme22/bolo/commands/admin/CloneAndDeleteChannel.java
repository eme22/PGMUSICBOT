package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.entities.RoleManager;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.requests.Route;

import java.util.List;

public class CloneAndDeleteChannel extends AdminCommand {

    public CloneAndDeleteChannel(Bot bot) {
        this.name = "clonechannel2";
        this.help = "clona el canal especificado y lo borra";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {
        TextChannel channel = event.getTextChannel();

        channel.createCopy().queue(
                success -> {
                    Settings s = event.getClient().getSettingsFor(event.getGuild());

                    if (s.getHelloChannel(event.getGuild()).getIdLong() == channel.getIdLong())
                        s.setBienvenidasChannelId(success.getIdLong());

                    if (s.getGoodbyeChannel(event.getGuild()).getIdLong() == channel.getIdLong())
                        s.setDespedidasChannelId(success.getIdLong());

                    if (s.getTextChannel(event.getGuild()).getIdLong() == channel.getIdLong())
                        s.setTextChannelId(success.getIdLong());

                    checkBienvenidas(event, success, s);


                    channel.delete().queue();
                    success.sendMessage("El canal se ha clonado con exito").queue();
                }
        );
    }

    private void checkBienvenidas(CommandEvent event, TextChannel success, Settings s) {
        String bienvenidasMessage = s.getBienvenidasChannelMessage();
        List<TextChannel> channels = FinderUtil.findTextChannels(bienvenidasMessage , event.getGuild());
        List<Role> roles = FinderUtil.findRoles(bienvenidasMessage , event.getGuild());
        List<User> users = FinderUtil.findUsers(bienvenidasMessage , event.getJDA());
        if (!channels.isEmpty()) {
            bienvenidasMessage =  bienvenidasMessage.replace(channels.get(0).getAsMention(), success.getAsMention());
            s.setBienvenidasChannelMessage(bienvenidasMessage);
        }
        if (!roles.isEmpty()) {
            bienvenidasMessage =bienvenidasMessage.replace(roles.get(0).getAsMention(), success.getAsMention());
            s.setBienvenidasChannelMessage(bienvenidasMessage);
        }
        if (!users.isEmpty()) {
            bienvenidasMessage = bienvenidasMessage.replace(users.get(0).getAsMention(), success.getAsMention());
            s.setBienvenidasChannelMessage(bienvenidasMessage);
        }
    }
}

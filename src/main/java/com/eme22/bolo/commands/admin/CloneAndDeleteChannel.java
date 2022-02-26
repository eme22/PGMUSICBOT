package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.TextChannel;

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
                        s.setHelloID(success);

                    if (s.getGoodbyeChannel(event.getGuild()).getIdLong() == channel.getIdLong())
                        s.setGoodByeID(success);

                    if (s.getTextChannel(event.getGuild()).getIdLong() == channel.getIdLong())
                        s.setTextChannel(success);

                    channel.delete().queue();
                    success.sendMessage("El canal se ha clonado con exito").queue();
                }
        );
    }
}

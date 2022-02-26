package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public class AvatarCmd extends Command {

    public AvatarCmd(Bot bot) {
        this.name = "avatar";
        this.help = "muestra el avatar del usuario nombrado";
        this.arguments = "<user>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getArgs().isEmpty())
        {
            event.reply(event.getClient().getError()+"Por favor incluya un nombre");
            return;
        }

        List<Member> member = FinderUtil.findMembers(event.getArgs(), event.getGuild());

        if (member.isEmpty())
        {
            event.reply(event.getClient().getError()+"Asegurese de que el usuario exista y no sea un bot");
            return;
        }

        Member member1 = member.get(0);
        String avatar = member1.getUser().getEffectiveAvatarUrl();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription("Avatar para "+member1.getAsMention());
        eb.setImage(avatar);
        event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
    }
}

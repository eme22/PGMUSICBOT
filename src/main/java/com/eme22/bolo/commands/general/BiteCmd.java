package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.nsfw.NSFWStrings;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import pics.waifu.Endpoints;
import pics.waifu.WaifuClient;

import java.util.List;

public class BiteCmd extends Command {

    public BiteCmd(Bot bot) {
        this.name = "bite";
        this.help = "muerde al usuario seleccionado";
        this.arguments = "<user>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getArgs().isEmpty())
        {
            event.replyError("Por favor incluya un nombre");
            return;
        }

        List<Member> member = FinderUtil.findMembers(event.getArgs(), event.getGuild());

        if (member.isEmpty())
        {
            event.replyError("Asegurese de que el usuario exista y no sea un bot");
            return;
        }

        Member memberKisser = event.getMember();
        Member memberKissed = member.get(0);

        if (memberKisser.equals(memberKissed))
        {
            event.replyError("Asegurese de que el usuario no sea usted");
            return;
        }


        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(memberKisser.getAsMention()+ NSFWStrings.getRandomBite() + memberKissed.getAsMention());
        builder.setImage(new WaifuClient().getSFWImage(Endpoints.SFW.BITE));
        event.reply(builder.build());
    }
}

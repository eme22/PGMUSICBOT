package com.eme22.bolo.commands.general;

import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.stats.StatsService;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AvatarCmd extends BaseCommand {

    private final StatsService statsService;
    @Autowired
    public AvatarCmd(StatsService statsService,@Value("${config.aliases.avatar:}") String[] aliases) {
        this.name = "avatar";
        this.help = "muestra el avatar del usuario nombrado";
        this.arguments = "<user>";
        this.guildOnly = true;
        this.statsService = statsService;
        this.aliases = aliases;
        this.options = Collections.singletonList(new OptionData(OptionType.USER, "usuario", "Seleccione al usuario al que ver su avatar.").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping option = event.getOption("usuario");
        if(option == null)
        {
            event.reply(event.getClient().getError()+ "Asegurese de que el usuario exista y no sea un bot").setEphemeral(true).queue();
            return;
        }

        Member member = option.getAsMember();

        String avatar = member.getUser().getEffectiveAvatarUrl()+"?size=512";
        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription("Avatar para "+member.getAsMention());
        eb.setImage(avatar);
        event.replyEmbeds(eb.build()).queue(success -> statsService.updateImagesSend(event.getGuild().getIdLong()));
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

        Member member1 = member.get(0);
        String avatar = member1.getUser().getEffectiveAvatarUrl()+"?size=512";
        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription("Avatar para "+member1.getAsMention());
        eb.setImage(avatar);
        event.reply(eb.build(), success -> statsService.updateImagesSend(event.getGuild().getIdLong()));
    }
}

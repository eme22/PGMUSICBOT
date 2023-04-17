package com.eme22.bolo.commands.general;

import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.configuration.BotConfiguration;
import com.eme22.bolo.utils.Constants;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
@Log4j2
public class AboutCmd extends BaseCommand {

    private boolean IS_AUTHOR = true;
    private String REPLACEMENT_ICON = "+";
    private final Color color;
    private final String description;
    private final Permission[] perms;
    private String oauthLink;
    private final String[] features;

    @Autowired
    public AboutCmd(Color color, BuildProperties properties) {
        this.color = color;
        this.description = String.format(Constants.message, properties.getVersion());
        this.features = Constants.features;
        this.name = "about";
        this.help = "muestra info sobre el bot";
        this.guildOnly = false;
        this.perms = BotConfiguration.RECOMMENDED_PERMS;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        if (this.oauthLink == null) {
            getOauthLink(event.getJDA());
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(event.isFromType(ChannelType.TEXT) ? event.getGuild().getSelfMember().getColor() : this.color);
        builder.setAuthor("Informacion de " + event.getJDA().getSelfUser().getName() + "!", null, event.getJDA().getSelfUser().getAvatarUrl());
        boolean join = event.getClient().getServerInvite() != null && !event.getClient().getServerInvite().isEmpty();
        boolean inv = !this.oauthLink.isEmpty();
        String invline = "\n" + (join ? "Unete a mi servidor [`link`](" + event.getClient().getServerInvite() + ")" : (inv ? " " : "")) + (inv ? (join ? ", o " : "") + "[`invitame`](" + this.oauthLink + ") a tu servidor" : "") + "!";
        String author = event.getJDA().getUserById(event.getClient().getOwnerId()) == null ? "<@" + event.getClient().getOwnerId() + ">" : event.getJDA().getUserById(event.getClient().getOwnerId()).getName();
        StringBuilder descr = (new StringBuilder()).append("Hola soy **").append(event.getJDA().getSelfUser().getName()).append("**, ").append(this.description).append("\nI ").append(this.IS_AUTHOR ? "fui escrito en java" : "mi creador/dueño").append(" es **").append(author).append("** uso [ChewUtils](https://github.com/Chew/JDA-Chewtils) (").append(JDAUtilitiesInfo.VERSION).append(") en conjunto de la libreria [JDA](https://github.com/DV8FromTheWorld/JDA) (").append(JDAInfo.VERSION).append(")\nEscribe `").append(event.getClient().getTextualPrefix()).append(event.getClient().getHelpWord()).append("` para ver mis comandos!").append(!join && !inv ? "" : invline).append("\n\nAlgunas de mis caracteristicas son : ```css");

        for (String feature : this.features) {
            descr.append("\n").append(event.getClient().getSuccess().startsWith("<") ? this.REPLACEMENT_ICON : event.getClient().getSuccess()).append(" ").append(feature);
        }

        descr.append(" ```");
        builder.setDescription(descr);
        if (event.getJDA().getShardInfo() == JDA.ShardInfo.SINGLE) {
            builder.addField("Estadisticas", event.getJDA().getGuilds().size() + " Servidores\n1 Nodo", true);
            builder.addField("Usuarios", event.getJDA().getUsers().size() + " \n" + event.getJDA().getGuilds().stream().mapToInt((g) -> g.getMembers().size()).sum() + " total", true);
            builder.addField("Canales", event.getJDA().getTextChannels().size() + " Texto\n" + event.getJDA().getVoiceChannels().size() + " Voz", true);
        } else {
            builder.addField("Estadisticas", event.getClient().getTotalGuilds() + " Servidores\nNodo " + (event.getJDA().getShardInfo().getShardId() + 1) + "/" + event.getJDA().getShardInfo().getShardTotal(), true);
            builder.addField("Este nodo: ", event.getJDA().getUsers().size() + " Usuarios\n" + event.getJDA().getGuilds().size() + " Servidores", true);
            builder.addField("", event.getJDA().getTextChannels().size() + " Canales de Texto\n" + event.getJDA().getVoiceChannels().size() + " Canales de Voz", true);
        }

        builder.setFooter("Ultimo Reinicio", (String)null);
        builder.setTimestamp(event.getClient().getStartTime());
        event.replyEmbeds(builder.build()).queue();

    }

    private void getOauthLink(JDA event) {
        try {
            ApplicationInfo info = event.retrieveApplicationInfo().complete();
            this.oauthLink = info.isBotPublic() ? info.getInviteUrl(0L, this.perms) : "";
        } catch (Exception var12) {
            log.error("Could not generate invite link ", var12);
            this.oauthLink = "";
        }
    }

    @Override
    protected void execute(CommandEvent event) {
        if (this.oauthLink == null) {
            getOauthLink(event.getJDA());
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(event.isFromType(ChannelType.TEXT) ? event.getGuild().getSelfMember().getColor() : this.color);
        builder.setAuthor("Informacion de " + event.getSelfUser().getName() + "!", null, event.getSelfUser().getAvatarUrl());
        boolean join = event.getClient().getServerInvite() != null && !event.getClient().getServerInvite().isEmpty();
        boolean inv = !this.oauthLink.isEmpty();
        String invline = "\n" + (join ? "Unete a mi servidor [`link`](" + event.getClient().getServerInvite() + ")" : (inv ? " " : "")) + (inv ? (join ? ", o " : "") + "[`invitame`](" + this.oauthLink + ") a tu servidor" : "") + "!";
        String author = event.getJDA().getUserById(event.getClient().getOwnerId()) == null ? "<@" + event.getClient().getOwnerId() + ">" : event.getJDA().getUserById(event.getClient().getOwnerId()).getName();
        StringBuilder descr = (new StringBuilder()).append("Hola soy **").append(event.getSelfUser().getName()).append("**, ").append(this.description).append("\nI ").append(this.IS_AUTHOR ? "fui escrito en java" : "mi creador/dueño").append(" es **").append(author).append("** uso [ChewUtils](https://github.com/Chew/JDA-Chewtils) (").append(JDAUtilitiesInfo.VERSION).append(") en conjunto de la libreria [JDA](https://github.com/DV8FromTheWorld/JDA) (").append(JDAInfo.VERSION).append(")\nEscribe `").append(event.getClient().getTextualPrefix()).append(event.getClient().getHelpWord()).append("` para ver mis comandos!").append(!join && !inv ? "" : invline).append("\n\nAlgunas de mis caracteristicas son : ```css");

        for (String feature : this.features) {
            descr.append("\n").append(event.getClient().getSuccess().startsWith("<") ? this.REPLACEMENT_ICON : event.getClient().getSuccess()).append(" ").append(feature);
        }

        descr.append(" ```");
        builder.setDescription(descr);
        if (event.getJDA().getShardInfo() == JDA.ShardInfo.SINGLE) {
            builder.addField("Estadisticas", event.getJDA().getGuilds().size() + " Servidores\n1 Nodo", true);
            builder.addField("Usuarios", event.getJDA().getUsers().size() + " \n" + event.getJDA().getGuilds().stream().mapToInt((g) -> g.getMembers().size()).sum() + " total", true);
            builder.addField("Canales", event.getJDA().getTextChannels().size() + " Texto\n" + event.getJDA().getVoiceChannels().size() + " Voz", true);
        } else {
            builder.addField("Estadisticas", event.getClient().getTotalGuilds() + " Servidores\nNodo " + (event.getJDA().getShardInfo().getShardId() + 1) + "/" + event.getJDA().getShardInfo().getShardTotal(), true);
            builder.addField("Este nodo: ", event.getJDA().getUsers().size() + " Usuarios\n" + event.getJDA().getGuilds().size() + " Servidores", true);
            builder.addField("", event.getJDA().getTextChannels().size() + " Canales de Texto\n" + event.getJDA().getVoiceChannels().size() + " Canales de Voz", true);
        }

        builder.setFooter("Ultimo Reinicio", (String)null);
        builder.setTimestamp(event.getClient().getStartTime());
        event.reply(builder.build());

    }
}

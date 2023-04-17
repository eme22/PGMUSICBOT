package com.eme22.bolo.commands.general;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import com.eme22.bolo.Bot;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.eme22.bolo.commands.BaseCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

@Component
public class SendMessageAsCmd extends BaseCommand {

    @Value("${config.aliases.sendmessageas:}")
    String[] aliases = new String[0];

    public SendMessageAsCmd(Bot bot) {
        this.name = "sendmessageas";
        this.help = "envia un mensaje como el usuario seleccionado";
        this.arguments = "[usuario] mensaje";
        this.options = Arrays.asList(
                new OptionData(OptionType.USER, "usuario", "busca el usuario a hacerce pasar.").setRequired(true),
                new OptionData(OptionType.STRING, "mensaje", "mensaje a decir").setRequired(true));
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        String message = event.getOption("mensaje").getAsString();
        User usuario = event.getOption("usuario").getAsUser();

        try {

            sendFakeMessage(usuario, message, event.getTextChannel());
            event.reply(event.getClient().getSuccess()+ " Mensaje Enviado").setEphemeral(true).queue();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void sendFakeMessage(User usuario, String message, TextChannel textChannel) throws IOException {

        Member member = textChannel.getGuild().getMember(usuario);

        String avatarUrl;
        String name;

        if (member == null) {
            avatarUrl = usuario.getEffectiveAvatarUrl();
            name = usuario.getName();
        }
        else {
            avatarUrl = member.getEffectiveAvatarUrl();
            name = member.getEffectiveName();
        }


        URL url = new URL(avatarUrl);

        Webhook webhook = null;

        webhook = textChannel
                .createWebhook(name)
                .setAvatar(Icon.from(new BufferedInputStream(url.openStream())))
                .complete();


        try (JDAWebhookClient client = JDAWebhookClient.from(webhook)) {
            Webhook finalWebhook = webhook;
            client.send(message).thenRun(()->{
                finalWebhook.delete().queue();
            }); // send a JDA message instance
        }
    }

    @Override
    protected void execute(CommandEvent event) {

        if(event.getArgs().isEmpty()) {
            event.replyError(" Por favor incluya al menos un usuario y mensaje");
            return;
        }

        String[] data = event.getArgs().split("] ");

        if (data.length != 2) {
            event.replyError(" Parametros incorrectos");
            return;
        }

        User usuario = FinderUtil.findUsers(data[0].substring(1).trim(), event.getJDA()).get(0);
        String message = data[1];

        try {

            sendFakeMessage( usuario, message, event.getTextChannel());

            event.getMessage().delete().queue();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


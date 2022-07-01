package com.eme22.bolo.commands.general;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import com.eme22.bolo.Bot;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class SendMessageAsCmd extends SlashCommand {

    public SendMessageAsCmd(Bot bot) {
        this.name = "sendmessageas";
        this.help = "envia un mensaje como el usuario seleccionado";
        this.arguments = "[usuario] mensaje";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Arrays.asList(
                new OptionData(OptionType.USER, "usuario", "busca el usuario a hacerce pasar.").setRequired(true),
                new OptionData(OptionType.STRING, "mensaje", "mensaje a decir").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        String message = event.getOption("mensaje").getAsString();
        User usuario = event.getOption("usuario").getAsUser();

        try {

            sendFakeMessage(usuario, message, event.getTextChannel());

            event.reply(getClient().getSuccess()+ " Mensaje Enviado").setEphemeral(true).queue();


        } catch (IOException e) {
            e.printStackTrace();
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

        User usuario = FinderUtil.findUsers(data[0].substring(1), event.getJDA()).get(0);
        String message = data[1];

        try {

            sendFakeMessage(usuario, message, event.getTextChannel());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFakeMessage(User usuario, String message, TextChannel textChannel) throws IOException {
        String avatarUrl = usuario.getAvatarUrl();

        if (avatarUrl == null)
            avatarUrl = usuario.getDefaultAvatarUrl();

        URL url = new URL(avatarUrl);
        Webhook webhook = textChannel
                .createWebhook(usuario.getName())
                .setAvatar(Icon.from(new BufferedInputStream(url.openStream())))
                .complete();

        try (JDAWebhookClient client = JDAWebhookClient.from(webhook)) {
            client.send(message); // send a JDA message instance
        } finally {
            webhook.delete().queue();
        }
    }
}


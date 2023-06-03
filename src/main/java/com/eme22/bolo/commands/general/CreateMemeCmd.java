package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.utils.MemeUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eme22.bolo.utils.OtherUtil.imageFromUrl;

import org.springframework.stereotype.Component;

//@Component
@Log4j2
public class CreateMemeCmd extends BaseCommand {

    @Value("${config.aliases.creatememe:}")
    String[] aliases = new String[0];

    private MemeUtil memeUtil;
    public CreateMemeCmd() {
        this.name = "creatememe";
        this.arguments = "<URL IMAGEN> [TEXTO SUPERIOR] [TEXTO INFERIOR]";
        this.help = "genera un meme desde una imagen";
        this.guildOnly = true;
        this.options = Arrays.asList(
                new OptionData(OptionType.STRING, "url", "url de la imagen base.").setRequired(true),
                new OptionData(OptionType.STRING, "superior", "texto superior del meme.").setRequired(true),
                new OptionData(OptionType.STRING, "inferior", "texto inferior del meme.").setRequired(true)
        );

    }

    @Override
    protected void execute(SlashCommandEvent event) {

        String url = event.getOption("url").getAsString();
        String textoS = event.getOption("superior").getAsString();
        String textoI = event.getOption("inferior").getAsString();

        File image = generateFile();

        try {
            InputStream str = imageFromUrl(url);
            memeUtil = new MemeUtil(ImageIO.read(str));
            str.close();
        } catch (IOException e) {
            log.error("Error al generar el meme", e);
            event.reply(event.getClient().getError()+ " se ha producido un error al generar el meme").queue();
        }


        try {
            ImageIO.write(memeUtil.generateMeme(textoS, textoI), "png", image);
        } catch (IOException e) {
            log.error("Error al generar el meme", e);
            event.reply(event.getClient().getError()+ " se ha producido un error al generar el meme").queue();
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setImage("attachment://tempMeme.png");

        event.replyEmbeds(eb.build()).addFiles(FileUpload.fromData(image)).queue(end -> image.delete());
    }

    @Override
    protected void execute(CommandEvent event) {

        if(event.getArgs().isEmpty()) {
            event.replyError(" Por favor incluya al menos un usuario y mensaje");
            return;
        }

        String regexGeneral = "<(.*?)> \\[(.*?)\\] \\[(.*?)\\]";

        Pattern pattern = Pattern.compile(regexGeneral, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(event.getArgs());
        boolean matchFound = matcher.find();
        if(matchFound) {
            String url = matcher.group(1);
            String textoS = matcher.group(2);
            String textoI = matcher.group(3);

            File image = generateFile();

            System.out.println(url);

            try {
                InputStream str = imageFromUrl(url);
                memeUtil = new MemeUtil(ImageIO.read(str));
                str.close();
            } catch (IOException e) {
                log.error("Error al generar el meme", e);
                event.replyError(" se ha producido un error al generar el meme");
            }


            try {
                ImageIO.write(memeUtil.generateMeme(textoS, textoI), "png", image);
            } catch (IOException e) {
                log.error("Error al generar el meme", e);
                event.replyError(" se ha producido un error al generar el meme");
            }

            System.out.println(image);
            EmbedBuilder eb = new EmbedBuilder();
            eb.setImage("attachment://tempMeme.png");

            event.getChannel().sendMessageEmbeds(eb.build()).addFiles(FileUpload.fromData(image)).queue( end -> image.delete());

        } else {
            event.replyError(" Por favor incluya al menos un usuario y mensaje");
        }

    }

    @NotNull
    private File generateFile() {

        File parent = new File("temp");
        if (!parent.exists()) {
            if (parent.mkdirs()) {
                log.error("Temp folder successfully created");
            }
        }

        File converted = new File(parent,"tempMeme.png");
        if (converted.delete()) {
            log.error("Image deleted from memory before new image");
        }
        return converted;
    }

}

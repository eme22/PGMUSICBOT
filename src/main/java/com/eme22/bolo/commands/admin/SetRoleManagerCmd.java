package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.entities.RoleManager;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Role;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import static com.jagrosh.jdautilities.commons.utils.FinderUtil.DISCORD_ID;
import static com.jagrosh.jdautilities.commons.utils.FinderUtil.EMOTE_MENTION;

public class SetRoleManagerCmd extends AdminCommand {

    protected final Bot bot;

    public SetRoleManagerCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "rolemsgbuild";
        this.help = "crea un mensaje en el cual los usuarios pueden reaccionar para obtener un rol determinado";
        this.arguments = "[Mensaje] emoji rol emoji rol... emoji rol";
        this.aliases = bot.getConfig().getAliases(this.name);
    }


    @Override
    protected void execute(CommandEvent event) {
        if(event.getArgs().isEmpty()) {
            event.replyError(" Por favor incluya al menos un mensaje, un emoji y un rol");
            return;
        }

        String message = event.getArgs();

        String[] data = message.split("] ");

        String[] emojisnroles = data[1].split(" ");

        if (emojisnroles.length % 2 != 0){
            event.replyError(" Por favor incluya correctamente los emojis y roles");
            return;
        }

        if ( emojisnroles.length < 1) {
            event.replyError(" Por favor incluya al menos un emoji y un rol");
            return;
        }

        message = data[0].substring(1);
        RoleManager manager = new RoleManager().withMessage(message);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription(message);
        event.getTextChannel().sendMessageEmbeds( eb.build()).queue( success -> {

            HashMap<String, String> map = new HashMap<>();

            for (int i = 0; i < emojisnroles.length ; i+=2) {
                List<Role> roles = FinderUtil.findRoles(emojisnroles[i+1].trim(), event.getGuild());

                if (emojisnroles[i].startsWith("<"))
                    emojisnroles[i] = emojisnroles[i].substring(2, emojisnroles[i].length()-1);
                success.addReaction( emojisnroles[i]).queue();
                map.put(emojisnroles[i], roles.get(0).getAsMention());
            }


            manager.setId(success.getIdLong());
            manager.setEmoji(map);
            bot.getSettingsManager().getSettings(event.getGuild().getIdLong()).addToRoleManagers(manager);
        });

    }
}

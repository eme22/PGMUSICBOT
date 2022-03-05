package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.entities.RoleManager;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.HashMap;
import java.util.HashSet;

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

        String[] content = data[1].split("[ ]+");

        message = data[0];

        if ( content.length < 1) {
            event.replyError(" Por favor incluya al menos un emoji y un rol");
            return;
        }

        if ( (content.length % 2) != 0) {
            event.replyError(" Por favor incluya correctamente los emojis y roles");
            return;
        }

        RoleManager manager = new RoleManager().withMessage(message);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription(message);
        event.getTextChannel().sendMessageEmbeds( eb.build()).queue( success -> {

            HashMap<String, String> map = new HashMap<>();

            for (int j = 0; j < content.length; j+=2) {
                success.addReaction(content[j]).queue();
                map.put(content[j], content[j+1]);
            }

            manager.setId(success.getIdLong());
            manager.setEmoji(map);
            bot.getSettingsManager().getSettings(event.getGuild().getIdLong()).addToRoleManagers(manager);
        });

    }
}

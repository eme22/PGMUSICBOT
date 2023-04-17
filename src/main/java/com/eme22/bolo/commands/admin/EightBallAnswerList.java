package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.AdminCommand;
import com.eme22.bolo.model.Server;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
public class EightBallAnswerList extends AdminCommand {

    private final Paginator.Builder builder;

    public EightBallAnswerList(Bot bot,@Qualifier("adminCategory") Category category, @Value("${config.aliases.8ballanswers:}") String[] aliases) {
        super(category);
        this.name = "8ballanswers";
        this.help = "muestra la lista de respuestas del comando 8ball del servidor";
        this.guildOnly = true;
        this.aliases = aliases;
        this.builder = new Paginator.Builder()
                .setColumns(1)
                .setFinalAction(m -> {
                    try {
                        m.clearReactions().queue();
                    } catch (PermissionException ignore) {
                    }
                })
                .setItemsPerPage(20)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .wrapPageEnds(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(10, TimeUnit.MINUTES);

    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Server s = event.getClient().getSettingsFor(event.getGuild());
        List<String> data = s.getEightBallAnswers();
        if (data.isEmpty()){
            event.reply( event.getClient().getError()+ " No hay respuestas para mostrar").setEphemeral(true).queue();
            return;
        }

        event.reply(event.getClient().getSuccess()+ " Lista de respuestas del comando 8ball").queue();
        builder.setText("").setItems(data.toArray(new String[0]));
        builder.build().paginate(event.getChannel(), 1);
    }

    @Override
    protected void execute(CommandEvent event) {
        Server s = event.getClient().getSettingsFor(event.getGuild());
        List<String> data = s.getEightBallAnswers();
        if (data.isEmpty()){
            event.replyError(" No hay respuestas para mostrar");
            return;
        }

        event.replySuccess( " Lista de respuestas del comando 8ball");
        builder.setText("").setItems(data.toArray(new String[0]));
        builder.build().paginate(event.getChannel(), 1);
    }
}

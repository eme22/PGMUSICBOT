package com.eme22.bolo.commands.admin;

import com.eme22.bolo.Bot;
import com.eme22.bolo.settings.Settings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.time.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BotFixedMessageCmd extends SlashCommand {


    public BotFixedMessageCmd(Bot bot) {
        this.name = "messageExt";
        this.help = "hace hablar al bot con opciones extendidas";
        this.arguments = "-t <tiempo> -u <unidad S (Segundo) | M (Minuto) | H (Hora) | D (Dia)> -f <finalizacion (DD/MM/AAAA)> -h <hh:mm> -m <mensaje (Comandos especiales %day% %month% %date% %time% %who% )>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.category = new Category("Admin", event ->
        {
            if(event.getAuthor().getId().equals(event.getClient().getOwnerId()))
                return true;
            if (event.getAuthor().getId().equals(event.getGuild().getOwnerId()))
                return true;
            if(event.getGuild()==null)
                return true;
            Settings settings = event.getClient().getSettingsFor(event.getGuild());
            Role admin = settings.getAdminRoleId(event.getGuild());
            return admin!=null && (event.getMember().getRoles().contains(admin) || admin.getIdLong()==event.getGuild().getIdLong());
        });
        this.guildOnly = true;

        this.options = createOptions();
    }

    private List<OptionData> createOptions() {
        final ArrayList<OptionData> optionsList = new ArrayList<>();
        optionsList.add(new OptionData(OptionType.INTEGER, "tiempo", "fija el numero de tiempo de intervalo a mostrar el mensaje").setRequiredRange(1, 9999).setRequired(false));
        optionsList.add(new OptionData(OptionType.STRING, "unidad", "fija la unidad de intervalo: S (Segundo) | M (Minuto) | H (Hora) | D (Dia)").setRequired(false));
        optionsList.add(new OptionData(OptionType.STRING, "fecha", "fija una fecha de finalizacion (DD/MM/AAAA) (MAÑANA por defecto)").setRequired(false));
        optionsList.add(new OptionData(OptionType.STRING, "hora", "fija una hora de finalizacion en la fecha  de finalizacion (HH:MM) (00:00 por defecto)").setRequired(false));
        optionsList.add(new OptionData(OptionType.STRING, "message", "mensaje a decir (Comandos especiales %day% %month% %date% %time% %who% )").setRequired(true));
        return optionsList;
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        OptionMapping tiempo = event.getOption("tiempo");
        OptionMapping unidad = event.getOption("unidad");
        OptionMapping fecha = event.getOption("fecha");
        OptionMapping hora = event.getOption("hora");
        OptionMapping message = event.getOption("message");

        int tiempo2;
        long time = -1;
        LocalDate fecha2 = LocalDate.now().plusDays(1);
        LocalDateTime fecha3 = LocalDateTime.from(fecha2);
        if (tiempo != null){
            tiempo2 = Integer.parseInt(tiempo.getAsString());
            if (unidad != null){
                switch (unidad.getAsString().charAt(0)){
                    default: break;
                    case 'S': time = TimeUnit.SECONDS.toMillis(tiempo2); break;
                    case 'M': time = TimeUnit.MINUTES.toMillis(tiempo2); break;
                    case 'H': time = TimeUnit.HOURS.toMillis(tiempo2); break;
                    case 'D': time = TimeUnit.DAYS.toMillis(tiempo2); break;
                }

                if (fecha != null){

                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        DateTimeFormatter formatter1 = new DateTimeFormatterBuilder().append(formatter)
                                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                                .toFormatter();
                        fecha2 = LocalDate.parse(fecha.getAsString(), formatter1);
                    } catch (DateTimeParseException e) {
                        e.printStackTrace();
                    }

                    fecha3 = LocalDateTime.from(fecha2);

                    if (hora != null){
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                        DateTimeFormatter formatter1 = new DateTimeFormatterBuilder().append(formatter)
                                .parseDefaulting(ChronoField.YEAR, fecha3.getYear())
                                .parseDefaulting(ChronoField.MONTH_OF_YEAR, fecha3.getMonthValue())
                                .parseDefaulting(ChronoField.DAY_OF_MONTH, fecha2.getDayOfMonth())
                                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                                .toFormatter();

                        fecha3 =  LocalDateTime.parse(hora.getAsString(), formatter1);
                    }
                }
            }
        }

        String message2 = "";
        if (message != null) {
            message2 = message.getAsString();
        }

        if (time != -1){
            Timer timer = new Timer();
            String finalMessage = message2;
            LocalDateTime finalFecha = fecha3;
            timer.schedule(new TimerTask() {
                public void run() {
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isAfter(finalFecha))
                        timer.cancel();
                    else
                        event.getChannel().sendMessage(buildMessage(finalMessage, finalFecha, event.getUser())).queue();
                }
            }, 0, time);
        }
        else {
            event.getChannel().sendMessage(buildMessage(message2 ,fecha3, event.getUser())).queue();
        }



    }

    private String buildMessage(String message2, LocalDateTime fecha3, User user) {
        message2 = message2.replaceAll("%day%", getDay(fecha3));
        message2 = message2.replaceAll("%date%", getDate(fecha3));
        message2 = message2.replaceAll("%time%", getTime(fecha3));
        message2 = message2.replaceAll("%who%", user.getAsMention());
        return message2;
    }


    private String getDay(LocalDateTime fecha){

        boolean today = DateUtils.isSameDay(new Date(),Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant()));
        boolean tomorrow = DateUtils.isSameDay(Date.from(LocalDateTime.from(new Date().toInstant()).plusDays(1).atZone(ZoneId.systemDefault()).toInstant()),Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant()));

        if (today)
            return  "HOY";

        if (tomorrow)
            return "MAÑANA";

        switch (fecha.getDayOfWeek()){
            default: return  "";
            case MONDAY: return "LUNES";
            case TUESDAY: return "MARTES";
            case WEDNESDAY: return "MIERCOLES";
            case THURSDAY: return  "JUEVES";
            case FRIDAY: return "VIERNES";
            case SATURDAY: return "SABADO";
            case SUNDAY: return "DOMINGO";
        }
    }

    private String getDate(LocalDateTime fecha){

        boolean today = DateUtils.isSameDay(new Date(),Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant()));
        boolean tomorrow = DateUtils.isSameDay(Date.from(LocalDateTime.from(new Date().toInstant()).plusDays(1).atZone(ZoneId.systemDefault()).toInstant()),Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant()));

        if (today || tomorrow)
            return  "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return fecha.format(formatter);
    }

    private String getTime(LocalDateTime fecha){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh.mm a");
        return fecha.format(formatter);
    }

    @Override
    protected void execute(CommandEvent event) {

    }
}

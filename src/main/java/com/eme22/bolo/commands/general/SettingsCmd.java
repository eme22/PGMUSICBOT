/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eme22.bolo.commands.general;

import com.eme22.bolo.Bot;
import com.eme22.bolo.commands.BaseCommand;
import com.eme22.bolo.model.RepeatMode;
import com.eme22.bolo.model.Server;
import com.eme22.bolo.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class SettingsCmd extends BaseCommand {
        private final static String EMOJI = "\uD83C\uDFA7"; // 🎧

        @Value("${config.aliases.settings:}")
        String[] aliases = new String[0];

        public SettingsCmd(Bot bot) {
                this.name = "settings";
                this.help = "muestra las opciones del bot";
                this.guildOnly = true;
        }

        @Override
        @Transactional
        protected void execute(SlashCommandEvent event) {
                Server s = event.getClient().getSettingsFor(event.getGuild());
                String builder = EMOJI + " **" +
                        FormatUtil.filter(event.getGuild().getSelfMember().getUser().getName()) +
                        "** settings:";
                TextChannel wchan = event.getGuild().getTextChannelById(s.getBienvenidasChannelId());
                TextChannel dchan = event.getGuild().getTextChannelById(s.getDespedidasChannelId());
                TextChannel tchan = event.getGuild().getTextChannelById(s.getTextChannelId());
                VoiceChannel vchan = event.getGuild().getVoiceChannelById(s.getVoiceChannelId());
                Role djRole = event.getGuild().getRoleById(s.getDjRoleId());
                Role adminrole = event.getGuild().getRoleById(s.getAdminRoleId());
                List<Long> onlyimages = s.getImageOnlyChannelsIds();

                EmbedBuilder ebuilder = new EmbedBuilder()
                                .setColor(event.getGuild().getSelfMember().getColor())
                                .setDescription("Canal de Musica: "
                                                + (tchan == null ? "Cualquiera" : "**#" + tchan.getName() + "**")
                                                + "\nCanal de Bienvenida: "
                                                + (wchan == null ? "Cualquiera" : "**#" + wchan.getName() + "**")
                                                + "\nCanal de Despedidas: "
                                                + (dchan == null ? "Cualquiera" : "**#" + dchan.getName() + "**")
                                                + "\nCanal de Voz: "
                                                + (vchan == null ? "Cualquiera" : vchan.getAsMention())
                                                + "\nRol de Admin: "
                                                + (adminrole == null ? "Ninguno" : "**" + adminrole.getName() + "**")
                                                + "\nRol de DJ: "
                                                + (djRole == null ? "Ninguno" : "**" + djRole.getName() + "**")
                                                + "\nCanales de solo imagenes: "
                                                + (onlyimages.isEmpty() ? "Ninguno" : "**" + onlyimages.size() + "**")

                                                + "\nPrefijo Personalizado: "
                                                + (s.getPrefix() == null ? "Ninguno" : "`" + s.getPrefix() + "`")
                                                + "\nModo de Repeticion: " + (s.getRepeatMode() == RepeatMode.OFF
                                                                ? s.getRepeatMode().getUserFriendlyName()
                                                                : "**" + s.getRepeatMode().getUserFriendlyName() + "**")
                                                + "\nPlaylist por defecto: "
                                                + (s.getDefaultPlaylist() == null ? "Ninguno"
                                                                : "**" + s.getDefaultPlaylist() + "**"))
                                .setFooter(event.getJDA().getGuilds().size() + " servers | "
                                                + event.getJDA().getGuilds().stream()
                                                                .filter(g -> g.getSelfMember().getVoiceState()
                                                                                .inAudioChannel())
                                                                .count()
                                                + " conecciones de audio", null);

                MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();

                messageCreateBuilder.addContent(builder).setEmbeds(ebuilder.build());

                event.reply(messageCreateBuilder.build()).queue();
        }

        @Override
        @Transactional
        protected void execute(CommandEvent event) {
                Server s = event.getClient().getSettingsFor(event.getGuild());

                String builder = EMOJI + " **" +
                        FormatUtil.filter(event.getSelfUser().getName()) +
                        "** settings:";
                TextChannel wchan = event.getGuild().getTextChannelById(s.getBienvenidasChannelId());
                TextChannel dchan = event.getGuild().getTextChannelById(s.getDespedidasChannelId());
                TextChannel tchan = event.getGuild().getTextChannelById(s.getTextChannelId());
                VoiceChannel vchan = event.getGuild().getVoiceChannelById(s.getVoiceChannelId());
                Role djRole = event.getGuild().getRoleById(s.getDjRoleId());
                Role adminrole = event.getGuild().getRoleById(s.getAdminRoleId());
                List<Long> onlyimages = s.getImageOnlyChannelsIds();

                EmbedBuilder ebuilder = new EmbedBuilder()
                                .setColor(event.getSelfMember().getColor())
                                .setDescription("Canal de Musica: "
                                                + (tchan == null ? "Cualquiera" : "**#" + tchan.getName() + "**")
                                                + "\nCanal de Bienvenida: "
                                                + (wchan == null ? "Cualquiera" : "**#" + wchan.getName() + "**")
                                                + "\nCanal de Despedidas: "
                                                + (dchan == null ? "Cualquiera" : "**#" + dchan.getName() + "**")
                                                + "\nCanal de Voz: "
                                                + (vchan == null ? "Cualquiera" : vchan.getAsMention())
                                                + "\nRol de Admin: "
                                                + (adminrole == null ? "Ninguno" : "**" + adminrole.getName() + "**")
                                                + "\nRol de DJ: "
                                                + (djRole == null ? "Ninguno" : "**" + djRole.getName() + "**")
                                                + "\nCanales de solo imagenes: "
                                                + (onlyimages.isEmpty() ? "Ninguno" : "**" + onlyimages.size() + "**")

                                                + "\nPrefijo Personalizado: "
                                                + (s.getPrefix() == null ? "Ninguno" : "`" + s.getPrefix() + "`")
                                                + "\nModo de Repeticion: " + (s.getRepeatMode() == RepeatMode.OFF
                                                                ? s.getRepeatMode().getUserFriendlyName()
                                                                : "**" + s.getRepeatMode().getUserFriendlyName() + "**")
                                                + "\nPlaylist por defecto: "
                                                + (s.getDefaultPlaylist() == null ? "Ninguno"
                                                                : "**" + s.getDefaultPlaylist() + "**"))
                                .setFooter(event.getJDA().getGuilds().size() + " servers | "
                                                + event.getJDA().getGuilds().stream()
                                                                .filter(g -> g.getSelfMember().getVoiceState()
                                                                        .inAudioChannel())
                                                                .count()
                                                + " conecciones de audio", null);

                MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();

                messageCreateBuilder.addContent(builder).setEmbeds(ebuilder.build());

                event.reply(messageCreateBuilder.build());
        }

}

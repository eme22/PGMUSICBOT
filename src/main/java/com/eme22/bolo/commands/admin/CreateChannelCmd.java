package com.eme22.bolo.commands.admin;

import com.eme22.bolo.commands.AdminCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import org.springframework.stereotype.Component;


public class CreateChannelCmd extends AdminCommand {
    public CreateChannelCmd(Category adminCategory) {
        super(adminCategory);
    }

    @Override
    protected void execute(SlashCommandEvent event) {

    }
}

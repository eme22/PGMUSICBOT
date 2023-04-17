/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
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
package com.eme22.bolo.commands.music;

import com.eme22.bolo.Bot;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
import org.springframework.stereotype.Component;

@Component
public class SCSearchCmd extends SearchCmd {

    @Value("${config.aliases.scsearch:}")
    String[] aliases = new String[0];

    public SCSearchCmd(Bot bot)
    {
        super(bot);
        this.searchPrefix = "scsearch:";
        this.name = "scsearch";
        this.help = "searches Soundcloud for a provided query";
    }
}

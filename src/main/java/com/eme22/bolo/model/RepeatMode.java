package com.eme22.bolo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
public enum RepeatMode {

    OFF(null, "Off"),
    ALL("\uD83D\uDD01", "All"), // 🔁
    SINGLE("\uD83D\uDD02", "Single"); // 🔂

    private final String emoji;
    private final String userFriendlyName;

    private RepeatMode(String emoji, String userFriendlyName)
    {
        this.emoji = emoji;
        this.userFriendlyName = userFriendlyName;
    }

    public static RepeatMode of(String mode) {
        return Stream.of(RepeatMode.values())
                .filter(p -> Objects.equals(p.getUserFriendlyName(), mode))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}

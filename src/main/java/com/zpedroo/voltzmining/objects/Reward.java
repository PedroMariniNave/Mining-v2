package com.zpedroo.voltzmining.objects;

import java.util.List;

public class Reward {

    private String name;
    private String display;
    private List<String> commands;

    public Reward(String name, String display, List<String> commands) {
        this.name = name;
        this.display = display;
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }

    public List<String> getCommands() {
        return commands;
    }
}
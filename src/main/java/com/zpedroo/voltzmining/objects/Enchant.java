package com.zpedroo.voltzmining.objects;

public class Enchant {

    private String name;
    private Double increasePerLevel;
    private Integer maxLevel;
    private Integer initialLevel;

    public Enchant(String name, Double increasePerLevel, Integer maxLevel, Integer initialLevel) {
        this.name = name;
        this.increasePerLevel = increasePerLevel;
        this.maxLevel = maxLevel;
        this.initialLevel = initialLevel;
    }

    public String getName() {
        return name;
    }

    public Double getIncreasePerLevel() {
        return increasePerLevel;
    }

    public Integer getMaxLevel() {
        return maxLevel;
    }

    public Integer getInitialLevel() {
        return initialLevel;
    }
}
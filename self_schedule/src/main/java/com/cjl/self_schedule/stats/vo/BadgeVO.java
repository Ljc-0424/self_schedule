package com.cjl.self_schedule.stats.vo;

import lombok.Data;

@Data
public class BadgeVO {
    private String id;
    private String name;
    private String icon;
    private String description;
    private String category;
    private Boolean unlocked;
    private String progress;

    public BadgeVO() {}

    public BadgeVO(String id, String name, String icon, String description, String category, Boolean unlocked, String progress) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.category = category;
        this.unlocked = unlocked;
        this.progress = progress;
    }
}

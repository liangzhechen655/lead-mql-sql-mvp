package com.example.leadmqlsql.model;

public enum WechatStatus {
    NOT_STARTED("未开始"),
    PENDING("待加微"),
    ADDED("已加微"),
    FAILED("加微失败");

    private final String label;

    WechatStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

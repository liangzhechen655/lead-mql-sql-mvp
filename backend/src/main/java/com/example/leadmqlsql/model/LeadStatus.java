package com.example.leadmqlsql.model;

public enum LeadStatus {
    NEW("新线索"),
    PENDING_CALL("待外呼"),
    CALLED_CONNECTED("已接通"),
    CALLED_NOT_CONNECTED("未接通"),
    VALID("有效"),
    INVALID("无效"),
    PENDING_WECHAT("待加微"),
    WECHAT_ADDED("已加微"),
    WECHAT_FAILED("加微失败"),
    FOLLOWING("跟进中"),
    MQL("MQL"),
    SQL("SQL");

    private final String label;

    LeadStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

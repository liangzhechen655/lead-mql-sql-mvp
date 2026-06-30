package com.example.leadmqlsql.model;

public enum CallStatus {
    NOT_CALLED("未外呼"),
    PENDING("待外呼"),
    CONNECTED("已接通"),
    NOT_CONNECTED("未接通"),
    FAILED("回传失败");

    private final String label;

    CallStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

package com.davorsauer.commons;

public enum NotifyType {

    SUCCESS("success"), FAILURE("failure"), ALERT("alert"), NEUTRAL("neutral");
    
    private String type;
    
    private NotifyType(String type) {
        this.type = type;
    }
    
}

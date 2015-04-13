package com.davorsauer.rest;

import com.davorsauer.commons.NotifyType;

public class ContactRes {

    private NotifyType notifyType;
    private int status;
    private String message;

    public NotifyType getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(NotifyType notifyType) {
        this.notifyType = notifyType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}

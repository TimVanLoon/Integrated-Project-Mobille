package com.example.keiichi.project_mobile.DAL.POJOs;


import java.io.Serializable;

public class Recipient implements Serializable {

    private EmailAddress emailAddress;

    public Recipient(EmailAddress emailAddress) {
        this.emailAddress = emailAddress;
    }

    public EmailAddress getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(EmailAddress emailAddress) {
        this.emailAddress = emailAddress;
    }
}

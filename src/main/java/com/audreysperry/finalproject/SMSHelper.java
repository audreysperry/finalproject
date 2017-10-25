package com.audreysperry.finalproject;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;


public class SMSHelper {

    public static final String TWILIO_SID = System.getenv("TWILIO_SID");
    public static final String TWILIO_AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    public static final String TWILIO_NUMBER = "+18643831532";

    public static void sendMessage(String phoneNumber, String note) {

        Twilio.init(TWILIO_SID, TWILIO_AUTH_TOKEN);

        Message message = Message
                .creator(new PhoneNumber(phoneNumber), new PhoneNumber(TWILIO_NUMBER),
                        note)
                .create();
        System.out.println(message.getSid());

    }
}

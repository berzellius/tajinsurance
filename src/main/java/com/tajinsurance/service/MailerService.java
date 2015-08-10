package com.tajinsurance.service;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by berz on 20.09.14.
 */
public interface MailerService {
    public enum EMAIL_TMPL{
        MP0_CONTRACT_ACCEPTED,
        MP0_WAIT_NOTIFICATION
    };

    /**
     * This method will send compose and send the message
     * */
    public void sendMail(String to, String subject, String body);

    public void sendMailFromTmpl(String to, EMAIL_TMPL email_tmpl, HashMap<String, String> parameters) throws IOException;
}

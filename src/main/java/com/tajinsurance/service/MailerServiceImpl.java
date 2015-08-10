package com.tajinsurance.service;

/**
 * Created by berz on 20.09.14.
 */

import com.tajinsurance.utils.CodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Properties;

@Service("mailerService")
public class MailerServiceImpl implements MailerService {

    private Properties emailProperties = null;

    private Properties getEmailProperties() {
        if (emailProperties == null) {
            try {
                emailProperties = PropertiesLoaderUtils.loadAllProperties("email.properties");
            } catch (IOException e) {
                emailProperties = null;
                throw new IllegalStateException("emailProperties in MailerService not set");
            }
        }
        return emailProperties;
    }

    @Autowired
    private MailSender mailSender;

    @Autowired
    private CodeUtils codeUtils;

    public MailerServiceImpl() {
    }


    /**
     * This method will send compose and send the message
     */
    public void sendMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);


        String from = MessageFormat.format("<{0}>",
                //getEmailProperties().getProperty("email.bima_from_field"),
                getEmailProperties().getProperty("email.bima_from_adress")
            );

        message.setFrom(from);

       // System.out.println(message.toString());

        try {
            mailSender.send(message);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private String getParameter(String name, EMAIL_TMPL email_tmpl, HashMap<String, String> parameters) {
        String param = parameters.get(name);
        if (param == null){
            throw new IllegalArgumentException("Argument '" + name + "' was not set for email template " + email_tmpl.toString());
        }
        return param;
    }

    @Override
    public void sendMailFromTmpl(String to, EMAIL_TMPL email_tmpl, HashMap<String, String> parameters) throws IOException {
        String subject = null;
        String body = null;

        String bodyTmpl;
        String subjTmpl;

        switch (email_tmpl) {
            case MP0_WAIT_NOTIFICATION:

                bodyTmpl = getEmailProperties().getProperty("email.bima_waiting_mp0_notification");
                subjTmpl = getEmailProperties().getProperty("email.bima_waiting_mp0_notification_subject");

                body = MessageFormat.format( bodyTmpl,
                        this.getParameter("number", email_tmpl, parameters),
                        this.getParameter("date", email_tmpl, parameters),
                        this.getParameter("partner", email_tmpl, parameters),
                        this.getParameter("user", email_tmpl, parameters)
                        );

                subject = MessageFormat.format( subjTmpl,
                        this.getParameter("number", email_tmpl, parameters)
                        );
                break;

            case MP0_CONTRACT_ACCEPTED:
                bodyTmpl = getEmailProperties().getProperty("email.bima_accepted_mp0_notification");
                subjTmpl = getEmailProperties().getProperty("email.bima_accepted_mp0_notification_subject");

                assert bodyTmpl != null && subjTmpl != null;

                body = MessageFormat.format(bodyTmpl,
                        this.getParameter("user", email_tmpl, parameters),
                        this.getParameter("number", email_tmpl, parameters)
                    );

                subject = MessageFormat.format(
                        subjTmpl,
                        this.getParameter("number", email_tmpl, parameters)
                );


                break;

            default:
                throw new IllegalArgumentException("Cant find email template: " + email_tmpl.toString());
        }

        if (body == null || subject == null)
            throw new IllegalStateException("Template " + email_tmpl.toString() + " exists, but subject or body not set");

        sendMail(to, subject, body);
    }


}

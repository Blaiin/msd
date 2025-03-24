package it.dmi.system.emails;

import it.dmi.system.emails.config.SmtpWrapper;
import it.dmi.utils.file.PropsLoader;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

import static it.dmi.utils.constants.FileConstants.*;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Singleton
@Startup
@Slf4j
public class EmailUtils {

    private static SmtpWrapper wrapper;

    private static final Properties PROPS = new Properties();

    private static Session session;

    private static final boolean enableEmailDebug = false;

    @PostConstruct
    private void loadSmtpConfig() {
        wrapper = PropsLoader.loadSmtpProperties();
        if(wrapper == null) {
            log.error("Error loading SMTP configuration file.");
            return;
        }
        log.debug("SMTP configuration loaded.");
        PROPS.put(SMTP_HOST, wrapper.smtp().host());
        PROPS.put(SMTP_PORT, wrapper.smtp().port());
        PROPS.put(SMTP_AUTH, wrapper.smtp().auth());
        PROPS.put(SMTP_STARTTLS, wrapper.smtp().startTls());
        PROPS.put(SMTP_DEBUG, String.valueOf(enableEmailDebug));

//        PROPS.put(SMTP_PROXY_HOST, wrapper.smtp().proxyHost());
//        PROPS.put(SMTP_PROXY_PORT, wrapper.smtp().proxyPort());
//        PROPS.put(SMTP_PROXY_USER, wrapper.smtp().proxyUser());
//        PROPS.put(SMTP_PROXY_PASS, wrapper.smtp().proxyPassword());

        session = Session.getInstance(PROPS, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(wrapper.smtp().user(), wrapper.smtp().password());
            }
        });
        log.debug("Email service initialized.");
    }

    public static boolean sendEmail(String emailList, String cc,
                                    String subject, String messageBody) throws MessagingException {
        if (isBlank(emailList)) {
            log.error("Empty email list, cannot send email");
            return false;
        }
        final var recipientList = emailList.trim().replace(';', ',');
        String ccRecipientsList = null;
        if (isNotBlank(cc)) ccRecipientsList = cc.trim().replace(';', ',');
        log.debug("Sending email to: {}", recipientList);
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(wrapper.smtp().user()));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientList));
        if (ccRecipientsList != null) msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccRecipientsList));
        msg.setSubject(subject);
        msg.setText(messageBody);

        Transport.send(msg);
        log.debug("Email sent successfully!");
        return true;
    }
}

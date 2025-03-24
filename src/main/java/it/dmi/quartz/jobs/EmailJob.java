package it.dmi.quartz.jobs;

import it.dmi.data.dto.AzioneDTO;
import it.dmi.system.emails.EmailUtils;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.dmi.utils.constants.NamingConstants.*;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Slf4j
public class EmailJob extends MSDQuartzJob {

    @Override
    //TODO implement email sending mechanism
    public void execute(@NotNull JobExecutionContext context) throws JobExecutionException {

        // Job identity and components
        JobDataMap dataMap = getDataMap(context);
        final var aID = dataMap.getString(ID);
        final var azione = (AzioneDTO) dataMap.get(TASK + aID);
        var container = getContainer(aID, dataMap);

        // Job resources
        final String emailTo = azione.destinatario();
        final String emailCC = azione.destinatarioCC();
        final var templateEmail = azione.templateEmail();
        String body = templateEmail != null ? templateEmail.emailBody() : null;

        // Process and execute
        final boolean canSend = isNotBlank(emailTo);
        final String actualBody = body != null ? body : "{Empty email template}";
        final String actualCC = isNotBlank(emailCC) ? emailCC : "";
        final boolean sent = sendEmail(emailTo, emailCC, actualBody);

        // Cache job output
        Map<String, List<String>> results = null;
        if (canSend) results = populateResults(emailTo, actualCC, actualBody, sent);

        if (container.setOutputResults(results))
            log.debug("Output content cached for Azione {}", aID);
        else log.debug("Could not cache contents for Azione {}", aID);
    }

    private static @NotNull Map<String, List<String>> populateResults(@NotNull String emailTo, String actualCC,
                                                                      String actualBody, boolean sent) {
        Map<String, List<String>> results = new HashMap<>();
        results.put("Email to", List.of(emailTo.split(";")));
        results.put("CC", List.of(actualCC));
        results.put("body", List.of(actualBody));
        results.put("success", List.of(String.valueOf(sent)));
        return results;
    }

    //TODO populate subject and body before sending
    private boolean sendEmail(String emailTo, String emailCC, String body) throws JobExecutionException {
        if (isBlank(emailTo)) {
            log.error("Email recipients were null, cannot sent email");
            return false;
        }
        try {
            final boolean sent = EmailUtils.sendEmail(emailTo, emailCC, "test_subject",
                    body != null ? body : "{Empty email template}");
            if (sent) log.info("Email sent → To: {}, CC: {}, Body: {}", emailTo, emailCC, body);
             else log.warn("Couldn't send email.");
            return sent;
        } catch (MessagingException e) {
            throw new JobExecutionException(e);
        }
    }
}

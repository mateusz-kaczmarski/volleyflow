package pl.volleyflow.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.volleyflow.auth.event.AccountActivationEmailRequested;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine springTemplateEngine;

    @Value("${app.activation.page-url}")
    private String activationPageUrl;

    @Value("${app.mail.from}")
    private String mailFrom;

    public void sendActivationEmail(AccountActivationEmailRequested emailRequested) {
        String activationUrl = buildActivationUrl(emailRequested.userExternalId(), emailRequested.activationToken());
        String htmlContent = renderActivationHtml(emailRequested.firstName(), activationUrl);
        String plainText = buildActivationPlainText(activationUrl);
        MimeMessage message = createActivationMessage(emailRequested.email(), plainText, htmlContent);

        javaMailSender.send(message);
    }

    private String buildActivationUrl(UUID userExternalId, String token) {
        return UriComponentsBuilder.fromUriString(activationPageUrl)
                .queryParam("userId", userExternalId)
                .queryParam("token", token)
                .encode()
                .toUriString();
    }

    private String renderActivationHtml(String firstName, String activationUrl) {
        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("activationUrl", activationUrl);

        return springTemplateEngine.process("mail/account-activation", context);
    }

    private String buildActivationPlainText(String activationUrl) {
        return """
                Dziękujemy za rejestrację w VolleyFlow!

                Aktywuj konto, otwierając link:
                %s

                Link jest ważny przez 15 minut od wygenerowania.
                Jeśli nie zakładałeś konta, zignoruj tę wiadomość.
                """.formatted(activationUrl);
    }

    private MimeMessage createActivationMessage(String recipientEmail, String plainText, String htmlContent) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(mailFrom);
            helper.setTo(recipientEmail);
            helper.setSubject("Aktywuj konto w VolleyFlow");
            helper.setText(plainText, htmlContent);
            return message;
        } catch (MessagingException e) {
            throw new IllegalStateException("Could not prepare account activation email", e);
        }
    }

}

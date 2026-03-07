package demo.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import demo.event.EmailVerifiedEvent;
import demo.event.UserRegisteredEvent;

@Component
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class MailListener {

    private final JavaMailSender mailSender;

    public MailListener(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Écoute la queue "mail.registration"
     * Quand un utilisateur s'inscrit, envoie un email avec le lien de vérification
     */
    @RabbitListener(queues = "mail.registration")
    public void onUserRegistered(UserRegisteredEvent event) {

        String verifyUrl = "http://localhost:8080/auth/verify?tokenId="
                + event.getTokenId()
                + "&token="
                + event.getTokenClear();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.getEmail());
        message.setFrom("noreply@demo.local");
        message.setSubject("Vérification de votre compte");
        message.setText(
            "Bonjour,\n\n"
            + "Cliquez sur ce lien pour vérifier votre compte :\n"
            + verifyUrl + "\n\n"
            + "Ce lien expire dans 15 minutes."
        );

        mailSender.send(message);
        System.out.println("[MAIL] Email de vérification envoyé à " + event.getEmail());
    }

    /**
     * Écoute la queue "mail.verified"
     * Quand un email est vérifié, envoie un email de confirmation
     */
    @RabbitListener(queues = "mail.verified")
    public void onEmailVerified(EmailVerifiedEvent event) {

        System.out.println("[MAIL] Email vérifié pour userId=" + event.getUserId());
        // Optionnel : envoyer un email de bienvenue ici
    }
}

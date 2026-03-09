package demo.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
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

        try {

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

        } catch (Exception e) {

            System.err.println("[MAIL] Erreur envoi email → DLQ : " + e.getMessage());

            // Envoie le message directement dans la Dead Letter Queue
            throw new AmqpRejectAndDontRequeueException("Erreur envoi email", e);
        }
    }
   /*
    @RabbitListener(queues = "mail.registration")
    public void onUserRegistered(UserRegisteredEvent event) {
        throw new RuntimeException("TEST DLQ");
    }*/

    /**
     * Écoute la queue "mail.verified"
     * Quand un email est vérifié
     */
    @RabbitListener(queues = "mail.verified")
    public void onEmailVerified(EmailVerifiedEvent event) {

        try {

            System.out.println("[MAIL] Email vérifié pour userId=" + event.getUserId());

        } catch (Exception e) {

            System.err.println("[MAIL] Erreur traitement verification → DLQ");

            throw new AmqpRejectAndDontRequeueException("Erreur traitement verification", e);
        }
    }

    /**
     * DLQ - mail.registration
     */
    @RabbitListener(queues = "mail.registration.dlq")
    public void handleRegistrationDlq(UserRegisteredEvent event) {

        System.err.println("[DLQ] Message en Dead Letter Queue (registration)");
        System.err.println("userId=" + event.getUserId());
        System.err.println("email=" + event.getEmail());
    }

    /**
     * DLQ - mail.verified
     */
    @RabbitListener(queues = "mail.verified.dlq")
    public void handleVerifiedDlq(EmailVerifiedEvent event) {

        System.err.println("[DLQ] Message en Dead Letter Queue (verified)");
        System.err.println("userId=" + event.getUserId());
    }
}
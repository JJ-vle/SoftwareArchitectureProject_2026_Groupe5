package demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitConfig {

    // Exchange où les événements sont publiés
    @Bean
    public TopicExchange authEventsExchange() {
        return new TopicExchange("auth.events");
    }

    // ===== DLX (Dead Letter Exchange) et DLQ =====
    @Bean
    public DirectExchange authEventsDlExchange() {
        return new DirectExchange("auth.events.dlx", true, false);
    }

    @Bean
    public Queue registrationMailDlq() {
        return new Queue("mail.registration.dlq", true);
    }

    @Bean
    public Queue verifiedMailDlq() {
        return new Queue("mail.verified.dlq", true);
    }

    @Bean
    public Binding bindRegistrationDlq(Queue registrationMailDlq, DirectExchange authEventsDlExchange) {
        return BindingBuilder.bind(registrationMailDlq).to(authEventsDlExchange).with("mail.registration");
    }

    @Bean
    public Binding bindVerifiedDlq(Queue verifiedMailDlq, DirectExchange authEventsDlExchange) {
        return BindingBuilder.bind(verifiedMailDlq).to(authEventsDlExchange).with("mail.verified");
    }

    // ===== Queues principales avec DLX =====
    // Queue pour les emails d'inscription
    @Bean
    public Queue registrationMailQueue() {
        return QueueBuilder.durable("mail.registration")
                .deadLetterExchange("auth.events.dlx")
                .deadLetterRoutingKey("mail.registration")
                .ttl(3600000) // TTL 1 heure (messages expirent après 1h)
                //.ttl(1000) // TTL 1s
                .build();
    }

    // Queue pour les emails de vérification confirmée
    @Bean
    public Queue verifiedMailQueue() {
        return QueueBuilder.durable("mail.verified")
                .deadLetterExchange("auth.events.dlx")
                .deadLetterRoutingKey("mail.verified")
                .ttl(3600000) // TTL 1 heure
                .build();
    }

    // Binding : événements "auth.user-registered" -> queue mail.registration
    @Bean
    public Binding bindRegistration(Queue registrationMailQueue, TopicExchange authEventsExchange) {
        return BindingBuilder.bind(registrationMailQueue).to(authEventsExchange).with("auth.user-registered");
    }

    // Binding : événements "auth.email-verified" -> queue mail.verified
    @Bean
    public Binding bindVerified(Queue verifiedMailQueue, TopicExchange authEventsExchange) {
        return BindingBuilder.bind(verifiedMailQueue).to(authEventsExchange).with("auth.email-verified");
    }

    // Sérialisation JSON des messages (avec support java.time)
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
}

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

    // Queue pour les emails d'inscription
    @Bean
    public Queue registrationMailQueue() {
        return new Queue("mail.registration", true);
    }

    // Queue pour les emails de vérification confirmée
    @Bean
    public Queue verifiedMailQueue() {
        return new Queue("mail.verified", true);
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

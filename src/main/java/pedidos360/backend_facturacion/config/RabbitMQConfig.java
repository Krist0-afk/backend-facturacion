package pedidos360.backend_facturacion.config;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final RabbitMQProperties props;

  

    @Bean
    public TopicExchange ordenesTopicExchange() {
        return ExchangeBuilder.topicExchange(props.getExchanges().getOrdenesTopic())
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange facturacionDirectExchange() {
        return ExchangeBuilder.directExchange(props.getExchanges().getFacturacionDirect())
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(props.getExchanges().getDlx())
                .durable(true)
                .build();
    }

   

    @Bean
    public Queue facturacionOrdenQueue() {
        return QueueBuilder.durable(props.getQueues().getFacturacionOrden())
                .withArgument("x-dead-letter-exchange", props.getExchanges().getDlx())
                .withArgument("x-dead-letter-routing-key", props.getRoutingKeys().getDlq())
                .build();
    }

    @Bean
    public Queue facturacionDlq() {
        return QueueBuilder.durable(props.getQueues().getFacturacionDlq()).build();
    }



    @Bean
    public Binding bindingOrdenCreadaTopic() {
        return BindingBuilder.bind(facturacionOrdenQueue())
                .to(ordenesTopicExchange())
                .with(props.getRoutingKeys().getOrdenCreada());
    }

    @Bean
    public Binding bindingOrdenCreadaDirect() {
        return BindingBuilder.bind(facturacionOrdenQueue())
                .to(facturacionDirectExchange())
                .with(props.getRoutingKeys().getOrdenCreadaExact());
    }


    // La cola de boleta.emitida la declara ordenes

    @Bean
    public Binding bindingFacturacionDlq() {
        return BindingBuilder.bind(facturacionDlq())
                .to(deadLetterExchange())
                .with(props.getRoutingKeys().getDlq());
    }


  

    // Las DLQ se leen como texto para poder registrar también mensajes con JSON inválido
    @Bean
    public SimpleRabbitListenerContainerFactory dlqListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }
}
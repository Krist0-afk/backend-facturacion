package pedidos360.backend_facturacion.config;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
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
    public Queue boletaEmitidaQueue() {
        return QueueBuilder.durable(props.getQueues().getBoletaEmitida())
                .withArgument("x-dead-letter-exchange", props.getExchanges().getDlx())
                .withArgument("x-dead-letter-routing-key", props.getRoutingKeys().getBoletaDlq())
                .build();
    }

    @Bean
    public Queue boletaEmitidaDlq() {
        return QueueBuilder.durable(props.getQueues().getBoletaEmitidaDlq()).build();
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


    @Bean
    public Binding bindingBoletaEmitida() {
        return BindingBuilder.bind(boletaEmitidaQueue())
                .to(facturacionDirectExchange())
                .with(props.getRoutingKeys().getBoletaEmitida());
    }


    @Bean
    public Binding bindingFacturacionDlq() {
        return BindingBuilder.bind(facturacionDlq())
                .to(deadLetterExchange())
                .with(props.getRoutingKeys().getDlq());
    }

    @Bean
    public Binding bindingBoletaEmitidaDlq() {
        return BindingBuilder.bind(boletaEmitidaDlq())
                .to(deadLetterExchange())
                .with(props.getRoutingKeys().getBoletaDlq());
    }

  

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
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
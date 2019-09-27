import org.apache.activemq.broker.Connection;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsMessage;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.ThrottlerRejectedExecutionException;
import org.apache.camel.util.jndi.JndiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.ConnectionFactory;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

public class Service {
    public static float getPrix(String idProduit) {
        return 5.0f;
    }

    public static void main(String[] args) throws Exception {
        Logger logger = LoggerFactory.getLogger(Service.class);

        CamelContext context = new DefaultCamelContext();

        InitialContext jndiContext = new InitialContext();

        ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("connectionFactory");

        context.addComponent("jms_component", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jms_component:price.Request").process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {

                        JmsMessage jmsMessage = (JmsMessage) exchange.getIn();

                        TextMessage textMessage = (TextMessage) jmsMessage.getJmsMessage();

                        logger.info("Message received : "+textMessage.getText());

                        float prix = getPrix(textMessage.getText());
                        textMessage.setText(Float.toString(prix));

                        jmsMessage.setJmsMessage(textMessage);

                        exchange.setOut(jmsMessage);

                    }
                }).to("jms_component:price.Response");
            }
        });
    }
}

package axon_ampq_rabbiit;

import com.rabbitmq.client.Channel;
import junit.framework.Assert;
import org.axonframework.domain.EventMessage;
import org.axonframework.domain.GenericEventMessage;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.amqp.spring.SpringAMQPTerminal;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author: pradyumna
 * @since 1.0 28/03/2015
 */
@ContextConfiguration(locations = "classpath:cqrs-infrastructure-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class RabbitPublish {

    private static final int EVENT_COUNT = 100;
    private static final int THREAD_COUNT = 10;
    @Autowired
    EventBus eventBus;
    @Autowired
    private ConnectionFactory connectionFactory;
    @Autowired
    private SpringAMQPTerminal terminal;
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testPublishing() throws Exception {
        final EventMessage<String> sentEvent = GenericEventMessage.asEventMessage("Hello world");
        for (int t = 0; t < 100; t++) {
            eventBus.publish(sentEvent);
        }
    }
}

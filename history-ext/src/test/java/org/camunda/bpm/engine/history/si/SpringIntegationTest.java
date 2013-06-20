package org.camunda.bpm.engine.history.si;

import org.camunda.bpm.engine.history.EventBuilder;
import org.camunda.bpm.engine.history.HistoryEventMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 
 * Spring-Integeration should be an good citizen for our requirements.
 * 
 * @author jbellmann
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SpringIntegationTest {

  private static final Logger LOG = LoggerFactory.getLogger(SpringIntegationTest.class);

  @Autowired
  @Qualifier("historyEventMessageChannel")
  private MessageChannel historyEventMessageChannel;

  @Before
  public void checkDeps() {
    Assert.assertNotNull(historyEventMessageChannel);
  }

  @Test
  public void test() {
    Message<HistoryEventMessage> message = MessageBuilder.withPayload(new HistoryEventMessage(EventBuilder.buildHistoricActivityInstanceEventEntity()))
        .setHeader("hostname", "localhost").build();
    Assert.assertTrue(historyEventMessageChannel.send(message));
  }

  @Configuration
  @ImportResource("classpath:/si-context.xml")
  static class TesConfig {

    @Autowired
    @Qualifier("outChannel")
    private SubscribableChannel outChannel;

    @Bean
    public AbstractEndpoint consumer() {

      EventDrivenConsumer c = new EventDrivenConsumer(outChannel, messageHandler());
      return c;

    }

    @Bean
    public MessageHandler messageHandler() {
      return new MessageHandler() {

        @Override
        public void handleMessage(Message<?> message) throws MessagingException {
          LOG.debug("----- MESSAGE_ARRIVED -----");
          LOG.debug("HEADER: " + message.getHeaders().toString());
          LOG.debug(message.getPayload().toString());
          LOG.debug("----- MESSAGE_END -----");
        }
      };
    }
  }
}

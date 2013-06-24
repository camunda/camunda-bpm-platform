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
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
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
@ContextConfiguration(classes = ConsumerProducerConfig.class)
@ActiveProfiles("JMS")
// to run JMS_OVER_AMQP, you have to verify the config and to
// make sure to run ActiveMQ with AMQP-Connector
// @ActiveProfiles("JMS_OVER_AMQP")
public class SpringIntegationTest {

  static final Logger LOG = LoggerFactory.getLogger(SpringIntegationTest.class);

  @Autowired
  @Qualifier("historyEventMessageChannel")
  private MessageChannel historyEventMessageChannel;

  @Autowired
  private HistoryEventMessageGateway historyEventMessageGateway;

  @Before
  public void checkDeps() {
    Assert.assertNotNull(historyEventMessageChannel);
    Assert.assertNotNull(historyEventMessageGateway);
  }

  @Test
  public void test() throws InterruptedException {
    Message<HistoryEventMessage> message = MessageBuilder.withPayload(new HistoryEventMessage(EventBuilder.buildHistoricActivityInstanceEventEntity()))
        .setHeader("hostname", "localhost").setHeader("inputStyle", "CHANNEL").build();
    Assert.assertTrue(historyEventMessageChannel.send(message));

    historyEventMessageGateway.send(new HistoryEventMessage(EventBuilder.buildHistoricProcessInstanceEventEntity()));

    // Give the consumer some time
    Thread.sleep(3 * 1000);
  }
}

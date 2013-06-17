package org.camunda.bpm.engine.history.jms;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.camunda.bpm.engine.history.HistoryEventHandlerComposite;
import org.camunda.bpm.engine.history.IncreaseCounterHandler;
import org.camunda.bpm.engine.history.marshaller.EventBuilder;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
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
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.remoting.JmsInvokerProxyFactoryBean;
import org.springframework.jms.remoting.JmsInvokerServiceExporter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

/**
 * @author jbellmann
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class JmsIntegrationTest {

  @Autowired
  @Qualifier("clientHistoryEventHandler")
  private HistoryEventHandler clientHistoryEventHandler;

  static AtomicInteger consumerCounter = new AtomicInteger(0);

  AtomicInteger clientCounter = new AtomicInteger(0);

  @Before
  public void setUp() {

    // clean message store
    ActiveMqTestUtils.prepare();
  }

  @Test
  public void sendingMessage() throws InterruptedException {

    for (int i = 0; i < 10; i++) {
      HistoryEvent historyEvent = EventBuilder.buildHistoryEvent();
      clientHistoryEventHandler.handleEvent(historyEvent);
      clientCounter.incrementAndGet();
    }

    Thread.sleep(5 * 1000);

    Assert.assertEquals("Events should be consumed", clientCounter.get(), consumerCounter.get());

  }

  @Configuration
  static class TestConfig {

    @Bean
    public CachingConnectionFactory connectionFactory() {
      CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();

      // in vm-activemq
      cachingConnectionFactory.setTargetConnectionFactory(new ActiveMQConnectionFactory("vm://localhost"));
      cachingConnectionFactory.setCacheConsumers(false);
      cachingConnectionFactory.setCacheProducers(false);
      cachingConnectionFactory.setSessionCacheSize(5);
      return cachingConnectionFactory;
    }

    @Bean
    public ActiveMQQueue eventQueue() {
      return new ActiveMQQueue("camunda.bpmn.history.events");
    }

    @Bean
    public HistoryEventHandler clientHistoryEventHandler() {
      JmsInvokerProxyFactoryBean ipfb = new JmsInvokerProxyFactoryBean();
      ipfb.setQueue(eventQueue());
      ipfb.setConnectionFactory(connectionFactory());
      ipfb.setServiceInterface(HistoryEventHandler.class);

      //
      ipfb.afterPropertiesSet();
      return (HistoryEventHandler) ipfb.getObject();
    }

    @Bean
    public JmsInvokerServiceExporter historyEventConsumer() {
      JmsInvokerServiceExporter ise = new JmsInvokerServiceExporter();
      ise.setServiceInterface(HistoryEventHandler.class);
      ise.setService(new HistoryEventHandlerComposite(Lists.asList(new IncreaseCounterHandler(consumerCounter), new LoggingHistoryEventHandler(),
          new HistoryEventHandler[0])));

      //
      ise.afterPropertiesSet();
      return ise;
    }

    @Bean
    public SimpleMessageListenerContainer listenerContainer() {
      SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
      container.setConnectionFactory(connectionFactory());
      container.setDestination(eventQueue());
      container.setConcurrentConsumers(1);
      container.setMessageListener(historyEventConsumer());
      return container;
    }
  }

  /**
   * Simple {@link HistoryEventHandler} that just logs the incoming
   * {@link HistoryEvent}s.
   * 
   * @author jbellmann
   */
  static class LoggingHistoryEventHandler implements HistoryEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingHistoryEventHandler.class);

    @Override
    public void handleEvent(final HistoryEvent historyEvent) {

      LOG.info("Got History-Event : {}", historyEvent.toString());
    }

  }

}

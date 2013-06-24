package org.camunda.bpm.engine.history.si;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

/**
 * 
 * @author jbellmann
 * 
 */
@Configuration
class ConsumerProducerConfig {

  // @Autowired
  // @Qualifier("inChannel")
  // private SubscribableChannel inChannel;
  //
  // /**
  // * Consumes the Messages.
  // *
  // * @return
  // */
  // @Bean
  // public AbstractEndpoint consumer() {
  //
  // EventDrivenConsumer c = new EventDrivenConsumer(inChannel,
  // messageHandler());
  // return c;
  //
  // }
  //
  // @Bean
  // public MessageHandler messageHandler() {
  // return new MessageHandler() {
  //
  // @Override
  // public void handleMessage(Message<?> message) throws MessagingException {
  // SpringIntegationTest.LOG.debug("----- MESSAGE_ARRIVED -----");
  // SpringIntegationTest.LOG.debug("HEADER: " +
  // message.getHeaders().toString());
  // SpringIntegationTest.LOG.debug(message.getPayload().toString());
  // SpringIntegationTest.LOG.debug("----- MESSAGE_END -----");
  // }
  // };
  // }

  @Configuration
  @ImportResource("classpath:/si-jms-context.xml")
  @Profile("JMS")
  static class JMS {
  }

  @Configuration
  @ImportResource("classpath:/si-jms-over-amqp-context.xml")
  @Profile("JMS_OVER_AMQP")
  static class JmsOverAmqp {
  }
}
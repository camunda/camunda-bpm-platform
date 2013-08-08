package org.camunda.bpm.engine.history.amqp;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.qpid.amqp_1_0.jms.impl.ConnectionFactoryImpl;
import org.apache.qpid.amqp_1_0.jms.impl.QueueImpl;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ConnectionTest {

  @Test
  public void testConnection() throws JMSException {
    ConnectionFactory connectionFactory = new ConnectionFactoryImpl("yourHostName", 5672, "egal", "egal", "testClient");
    Connection connection = connectionFactory.createConnection();
    QueueImpl queue = new QueueImpl("queue://cam-async-history-2");
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Destination destination = session.createQueue("cam-async-history-2");
    MessageProducer p = session.createProducer(destination);
    TextMessage tm = session.createTextMessage();
    tm.setText("Just a Test");
    p.send(tm);
  }
}

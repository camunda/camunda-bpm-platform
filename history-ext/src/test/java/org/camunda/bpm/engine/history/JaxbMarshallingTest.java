package org.camunda.bpm.engine.history;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.junit.BeforeClass;

/**
 * Make sure we can marshall and unmarshall {@link HistoryEvent}s with Jaxb.
 * 
 * @author jbellmann
 * 
 */
public class JaxbMarshallingTest extends AbstractMarshallingTest {

  private static Marshaller marshaller;
  private static Unmarshaller unmarshaller;

  @BeforeClass
  public static void setUpOnce() {

    JAXBContext context;
    try {
      context = JAXBContext.newInstance(new Class[] { HistoryEventMessage.class });
      marshaller = context.createMarshaller();
      unmarshaller = context.createUnmarshaller();
    } catch (JAXBException e) {
      throw new RuntimeException(e.getMessage(), e);
    }

  }

  @Override
  public HistoryEventMessage unmarshall(String source) throws Exception {

    return (HistoryEventMessage) unmarshaller.unmarshal(new StringReader(source));
  }

  @Override
  public String marshall(HistoryEventMessage historyEvent) throws Exception {

    StringWriter writer = new StringWriter();
    marshaller.marshal(historyEvent, writer);
    return writer.toString();
  }
}

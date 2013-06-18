package org.camunda.bpm.engine.history;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.camunda.bpm.engine.history.marshaller.EventBuilder;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.junit.Test;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * 
 * @author jbellmann
 * 
 */
public class HistoryEventMessageTest {

  @Test
  public void testJaxbMarshalling() throws JAXBException {
    HistoricActivityInstanceEventEntity entity = EventBuilder.buildHistoricActivityInstanceEventEntity();
    JAXBContext context = JAXBContext.newInstance(new Class[] { HistoryEventMessage.class });
    Marshaller marshaller = context.createMarshaller();
    marshaller.marshal(new HistoryEventMessage(entity), System.out);
  }

  @Test
  public void testJacksonMarshalling() throws IOException {
    HistoricActivityInstanceEventEntity entity = EventBuilder.buildHistoricActivityInstanceEventEntity();
    HistoryEventMessage message = new HistoryEventMessage(entity);
    ObjectMapper mapper = new ObjectMapper();
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
    mapper.setAnnotationIntrospector(introspector);
    // needed because of PersistentObject.persistentState has no setter for
    // example
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    String result = mapper.writeValueAsString(message);
    System.out.println(result);
    HistoryEventMessage messageFromString = mapper.readValue(result, HistoryEventMessage.class);
  }

}

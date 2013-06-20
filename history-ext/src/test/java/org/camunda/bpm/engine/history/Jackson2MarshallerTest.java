package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.junit.BeforeClass;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * Make sure we can marshall and unmarshall {@link HistoryEvent}s with Jackson2.
 * 
 * @author jbellmann
 * 
 */
public class Jackson2MarshallerTest extends AbstractMarshallingTest {

  private static ObjectMapper objectMapper;

  @BeforeClass
  public static void setUpOnce() {

    ObjectMapper mapper = new ObjectMapper();
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
    mapper.setAnnotationIntrospector(introspector);
    // needed because of PersistentObject.persistentState has no setter for
    // example
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper = mapper;
  }

  @Override
  public HistoryEventMessage unmarshall(String source) throws Exception {
    return objectMapper.readValue(source, HistoryEventMessage.class);
  }

  @Override
  public String marshall(HistoryEventMessage historyEventMessage) throws Exception {
    return objectMapper.writeValueAsString(historyEventMessage);
  }

}

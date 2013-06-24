package org.camunda.bpm.engine.history.si;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.history.HistoryEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * Transform a {@link HistoryEventMessage} to JSON via Jackson2.
 * 
 * @author jbellmann
 * 
 */
public class Jackson2HistoryEventMessageTransformer implements HistoryEventMessageTransformer<String> {

  private static final Logger LOG = LoggerFactory.getLogger(Jackson2HistoryEventMessageTransformer.class);

  private ObjectMapper objectMapper;

  @PostConstruct
  public void init() {
    ObjectMapper mapper = new ObjectMapper();
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
    mapper.setAnnotationIntrospector(introspector);

    // we got strange errors without that
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    // needed because of PersistentObject.persistentState has no setter for
    // example
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper = mapper;
  }

  public String transformFromHistoryEventMessage(HistoryEventMessage historyEventMessage) {
    try {
      return this.objectMapper.writeValueAsString(historyEventMessage);
    } catch (JsonProcessingException e) {
      LOG.error("Could not marshall 'HistoryEventMessage' {}", historyEventMessage);
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public HistoryEventMessage transformToHistoryEventMessage(String source) {
    LOG.debug(source);
    try {
      return this.objectMapper.readValue(source, HistoryEventMessage.class);
    } catch (JsonParseException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (JsonMappingException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}

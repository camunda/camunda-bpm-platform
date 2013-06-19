package org.camunda.bpm.engine.history.si;

import java.io.StringReader;
import java.io.StringWriter;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.camunda.bpm.engine.history.HistoryEventMessage;

/**
 * @author jbellmann
 * 
 */
public class Jaxb2HistoryEventMessageTransformer implements HistoryEventMessageTransformer<String> {

  private Marshaller marshaller;
  private Unmarshaller unmarshaller;

  @PostConstruct
  public void init() {

    try {
      JAXBContext context = JAXBContext.newInstance(new Class[] { HistoryEventMessage.class });
      marshaller = context.createMarshaller();
      unmarshaller = context.createUnmarshaller();
    } catch (JAXBException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public String transformFromHistoryEventMessage(HistoryEventMessage historyEventMessage) {

    StringWriter writer = new StringWriter();
    try {
      marshaller.marshal(historyEventMessage, writer);
      return writer.toString();
    } catch (JAXBException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public HistoryEventMessage transformToHistoryEventMessage(String source) {

    try {
      return (HistoryEventMessage) unmarshaller.unmarshal(new StringReader(source));
    } catch (JAXBException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}

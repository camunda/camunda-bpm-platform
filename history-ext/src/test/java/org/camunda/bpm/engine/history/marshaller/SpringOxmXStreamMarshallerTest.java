package org.camunda.bpm.engine.history.marshaller;

import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.xstream.XStreamMarshaller;

/**
 * @author  jbellmann
 */
public class SpringOxmXStreamMarshallerTest {

    private XStreamMarshaller marshaller = new XStreamMarshaller();

    private HistoryEventHandler eventHandlerMock = Mockito.mock(HistoryEventHandler.class);

    @Test
    public void unmarshallFromFile() throws XmlMappingException, IOException {
        Object o = marshaller.unmarshal(new StreamSource(EventResources.getHistoryEventAsInputStream()));
        Assert.assertTrue("Should be an historyEvent", o instanceof HistoryEvent);
        eventHandlerMock.handleEvent((HistoryEvent) o);
    }

}

package org.camunda.bpm.engine.history;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDetailEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricTaskInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricFormPropertyEntity;

/**
 * Simple wrapper for JAXB-Marshalling. No need to touch existing code.
 * 
 * @author jbellmann
 * 
 */
@XmlRootElement
public class HistoryEventMessage {

  @XmlElements({ @XmlElement(name = "historicActivityInstanceEventEntity", type = HistoricActivityInstanceEventEntity.class),
      @XmlElement(name = "historicDetailEventEntity", type = HistoricDetailEventEntity.class),
      @XmlElement(name = "historicProcessInstanceEventEntity", type = HistoricProcessInstanceEventEntity.class),
      @XmlElement(name = "historicFormPropertyEntity", type = HistoricFormPropertyEntity.class),
      @XmlElement(name = "historicTaskInstanceEventEntity", type = HistoricTaskInstanceEventEntity.class) })
  private HistoryEvent historyEvent;

  protected HistoryEventMessage() {
  }

  public HistoryEventMessage(HistoryEvent historyEvent) {
    if (historyEvent == null) {
      throw new IllegalArgumentException("HistoryEvent should never be null");
    }
    this.historyEvent = historyEvent;
  }

  public HistoryEvent getHistoryEvent() {
    return historyEvent;
  }
}

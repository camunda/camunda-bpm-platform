package org.camunda.bpm.engine.impl.jobexecutor.historycleanup;

import java.util.Calendar;
import java.util.Date;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * @author Svetlana Dorokhova.
 */
public class BatchWindow {

  public Date start;
  public Date end;

  public BatchWindow() {
  }

  public BatchWindow(Date start, Date end) {
    this.start = start;
    this.end = end;
  }

  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  public boolean isWithin(Date date) {
    return (date.after(start) || date.equals(start)) && date.before(end);
  }

}

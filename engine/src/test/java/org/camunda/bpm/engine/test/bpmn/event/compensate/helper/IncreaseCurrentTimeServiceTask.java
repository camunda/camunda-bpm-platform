package org.camunda.bpm.engine.test.bpmn.event.compensate.helper;

import java.util.Date;
import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.ClockUtil;

/**
 * @author Svetlana Dorokhova
 */
public class IncreaseCurrentTimeServiceTask implements JavaDelegate {
  
  public void execute(DelegateExecution execution) throws Exception {
    Date currentTime = (Date)execution.getVariable("currentTime");
    currentTime = DateUtils.addSeconds(currentTime, 1);
    ClockUtil.setCurrentTime(currentTime);
    execution.setVariable("currentTime", currentTime);
  }

}

package org.camunda.bpm.engine.spring.test.transaction.modification;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import java.util.Date;

public class CalculateTimerDate {

    public Date execute(DelegateExecution execution) {
        execution.setVariable("createDate", new Date());
        return DateUtils.addDays(new Date(), 1);
    }

}

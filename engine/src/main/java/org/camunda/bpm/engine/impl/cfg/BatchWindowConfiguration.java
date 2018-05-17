package org.camunda.bpm.engine.impl.cfg;

import java.text.ParseException;
import java.util.Date;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHelper;

/**
 * @author Svetlana Dorokhova.
 */
public class BatchWindowConfiguration {

  protected final static ConfigurationLogger LOG = ConfigurationLogger.CONFIG_LOGGER;

  protected String startTime;

  private Date startTimeAsDate;

  protected String endTime = "00:00";

  private Date endTimeAsDate;

  public BatchWindowConfiguration(String startTime, String endTime) {
    this.startTime = startTime;
    if (endTime != null) {
      this.endTime = endTime;
    }
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public Date getStartTimeAsDate() {
    try {
      if (startTimeAsDate == null) {
        startTimeAsDate = HistoryCleanupHelper.parseTimeConfiguration(startTime);
      }
      return startTimeAsDate;
    } catch (ParseException e) {
      throw LOG.invalidPropertyValue("startTime", startTime);
    }
  }

  public Date getEndTimeAsDate() {
    try {
      if (endTimeAsDate == null) {
        endTimeAsDate = HistoryCleanupHelper.parseTimeConfiguration(endTime);
      }
      return endTimeAsDate;
    } catch (ParseException e) {
      throw LOG.invalidPropertyValue("endTime", endTime);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    BatchWindowConfiguration that = (BatchWindowConfiguration) o;

    if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null)
      return false;
    return endTime != null ? endTime.equals(that.endTime) : that.endTime == null;
  }

  @Override
  public int hashCode() {
    int result = startTime != null ? startTime.hashCode() : 0;
    result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
    return result;
  }
}

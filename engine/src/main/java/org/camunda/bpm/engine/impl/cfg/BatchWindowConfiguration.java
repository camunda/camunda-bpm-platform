package org.camunda.bpm.engine.impl.cfg;

import java.text.ParseException;
import java.util.Date;
import org.camunda.bpm.engine.impl.jobexecutor.historycleanup.HistoryCleanupHelper;

/**
 * @author Svetlana Dorokhova.
 */
public class BatchWindowConfiguration {

  protected final static ConfigurationLogger LOG = ConfigurationLogger.CONFIG_LOGGER;

  private String startTime;

  private Date startTimeAsDate;

  private String endTime = "00:00";

  private Date endTimeAsDate;

  public BatchWindowConfiguration() {
  }

  public BatchWindowConfiguration(String startTime, String endTime) {
    this.startTime = startTime;
    initStartTimeAsDate();
    if (endTime != null) {
      this.endTime = endTime;
    }
    initEndTimeAsDate();
  }

  private void initStartTimeAsDate() {
    try {
      startTimeAsDate = HistoryCleanupHelper.parseTimeConfiguration(startTime);
    } catch (ParseException e) {
      throw LOG.invalidPropertyValue("startTime", startTime);
    }
  }

  private void initEndTimeAsDate() {
    try {
      endTimeAsDate = HistoryCleanupHelper.parseTimeConfiguration(endTime);
    } catch (ParseException e) {
      throw LOG.invalidPropertyValue("endTime", endTime);
    }
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
    initStartTimeAsDate();
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
    initEndTimeAsDate();
  }

  public Date getStartTimeAsDate() {
    return startTimeAsDate;
  }

  public Date getEndTimeAsDate() {
    return endTimeAsDate;
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

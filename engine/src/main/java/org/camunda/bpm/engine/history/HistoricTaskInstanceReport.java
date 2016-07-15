package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.exception.NotValidException;

import java.util.Date;
import java.util.List;

/**
 * @author Stefan Hentschel.
 */
public interface HistoricTaskInstanceReport {

  /**
   * <p>Sets the completed after date for contraining the query to search for all tasks
   * which are completed after a certain date.</p>
   *
   * @param completedAfter A {@link Date} to define the granularity of the report
   *
   * @throws NotValidException
   *          When the given date is null.
   */
  HistoricTaskInstanceReport completedAfter(Date completedAfter);

  /**
   * <p>Sets the completed before date for contraining the query to search for all tasks
   * which are completed before a certain date.</p>
   *
   * @param completedBefore A {@link Date} to define the granularity of the report
   *
   * @throws NotValidException
   *          When the given date is null.
   */
  HistoricTaskInstanceReport completedBefore(Date completedBefore);

  /**
   * <p>Defines if the query should be grouped by process definition key.</p>
   */
  HistoricTaskInstanceReport groupByProcessDefinitionKey();

  /**
   * <p>Executes the task report query and returns a list of {@link TaskReportResult}s</p>
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ_HISTORY} permission
   *          on any {@link Resources#PROCESS_DEFINITION}.
   *
   * @return a list of {@link TaskReportResult}s
   */
  List<TaskReportResult> taskReport();
}

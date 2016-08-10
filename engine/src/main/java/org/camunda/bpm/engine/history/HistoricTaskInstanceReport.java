package org.camunda.bpm.engine.history;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.query.Report;

/**
 * @author Stefan Hentschel.
 */
public interface HistoricTaskInstanceReport extends Report {

  /**
   * <p>Sets the completed after date for constraining the query to search for all tasks
   * which are completed after a certain date.</p>
   *
   * @param completedAfter A {@link Date} to define the granularity of the report
   *
   * @throws NotValidException
   *          When the given date is null.
   */
  HistoricTaskInstanceReport completedAfter(Date completedAfter);

  /**
   * <p>Sets the completed before date for constraining the query to search for all tasks
   * which are completed before a certain date.</p>
   *
   * @param completedBefore A {@link Date} to define the granularity of the report
   *
   * @throws NotValidException
   *          When the given date is null.
   */
  HistoricTaskInstanceReport completedBefore(Date completedBefore);

  /**
   * <p>Executes the task report query and returns a list of {@link HistoricTaskInstanceReportResult}s</p>
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ_HISTORY} permission
   *          on any {@link Resources#PROCESS_DEFINITION}.
   *
   * @return a list of {@link HistoricTaskInstanceReportResult}s
   */
  List<HistoricTaskInstanceReportResult> countByProcessDefinitionKey();

  /**
   * <p>Executes the task report query and returns a list of {@link HistoricTaskInstanceReportResult}s</p>
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ_HISTORY} permission
   *          on any {@link Resources#PROCESS_DEFINITION}.
   *
   * @return a list of {@link HistoricTaskInstanceReportResult}s
   */
  List<HistoricTaskInstanceReportResult> countByTaskName();
}

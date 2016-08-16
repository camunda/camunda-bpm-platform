package org.camunda.bpm.engine.history;

/**
 * @author Stefan Hentschel.
 */
public interface HistoricTaskInstanceReportResult {

  /**
   * <p>Returns the count of the grouped items.</p>
   */
  Long getCount();

  /**
   * <p>Returns the process definition key for the selected definition key.</p>
   */
  String getProcessDefinitionKey();

  /**
   * <p>Returns the process definition id for the selected definition key</p>
   */
  String getProcessDefinitionId();

  /**
   * <p></p>Returns the process definition name for the selected definition key</p>
   */
  String getProcessDefinitionName();

  /**
   * <p>Returns the name of the task</p>
   *
   * @return A task name when the query is triggered with a 'countByTaskName'. Else the return
   * value is null.
   */
  String getTaskName();

  /**
   * <p>Returns the id of the tenant task</p>
   */
  String getTenantId();
}

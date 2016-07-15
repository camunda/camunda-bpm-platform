package org.camunda.bpm.engine.history;

/**
 * @author Stefan Hentschel.
 */
public interface TaskReportResult {

  /**
   * <p>Returns the selected definition key.</p>
   *
   * @return A task definition key or a process definition key. The result depends how the query was triggered.
   *         When the query is triggered with a 'groupByProcessDefinitionKey' then the returned value will be a
   *         process definition key. Else the return value is a task defintion key
   */
  String getDefinition();

  /**
   * <p>Returns the count of the grouped items.</p>
   */
  Long getCount();
}

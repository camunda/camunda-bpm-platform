package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.query.NativeQuery;

/**
 * Allows querying of {@link HistoricTaskInstanceQuery}s via native (SQL) queries
 * @author Bernd Ruecker (camunda)
 */
public interface NativeHistoricProcessInstanceQuery extends NativeQuery<NativeHistoricProcessInstanceQuery, HistoricProcessInstance> {

}

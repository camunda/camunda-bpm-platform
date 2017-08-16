package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.camunda.bpm.engine.history.HistoricVariableInstanceQuery}s via native (SQL) queries
 * @author Ramona Koch
 */
public interface NativeHistoricVariableInstanceQuery extends NativeQuery<NativeHistoricVariableInstanceQuery, HistoricVariableInstance> {
}

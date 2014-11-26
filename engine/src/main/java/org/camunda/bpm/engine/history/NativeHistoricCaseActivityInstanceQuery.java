package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.query.NativeQuery;

/**
 * Allows querying of {@link HistoricCaseActivityInstance}s via native (SQL) queries
 *
 * @author Sebastian Menski
 */
public interface NativeHistoricCaseActivityInstanceQuery extends NativeQuery<NativeHistoricCaseActivityInstanceQuery, HistoricCaseActivityInstance> {

}

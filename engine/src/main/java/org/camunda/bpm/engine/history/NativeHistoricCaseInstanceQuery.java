package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.query.NativeQuery;

/**
 * Allows querying of {@link HistoricCaseInstance}s via native (SQL) queries
 *
 * @author Sebastian Menski
 */
public interface NativeHistoricCaseInstanceQuery extends NativeQuery<NativeHistoricCaseInstanceQuery, HistoricCaseInstance> {

}

package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.query.NativeQuery;

/**
 * Allows querying of {@link HistoricDecisionInstance}s via native (SQL) queries.
 *
 * @author Philipp Ossler
 */
public interface NativeHistoricDecisionInstanceQuery extends NativeQuery<NativeHistoricDecisionInstanceQuery, HistoricDecisionInstance> {

}

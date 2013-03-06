package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.query.NativeQuery;

/**
 * Allows querying of {@link HistoricActivityInstanceQuery}s via native (SQL) queries
 * @author Bernd Ruecker (camunda)
 */
public interface NativeHistoricActivityInstanceQuery extends NativeQuery<NativeHistoricActivityInstanceQuery, HistoricActivityInstance> {

}

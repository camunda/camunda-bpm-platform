package org.camunda.bpm.engine.runtime;

import org.camunda.bpm.engine.query.NativeQuery;

/**
 * Allows querying of {@link Execution}s via native (SQL) queries
 * @author Bernd Ruecker (camunda)
 */
public interface NativeExecutionQuery extends NativeQuery<NativeExecutionQuery, Execution> {

}

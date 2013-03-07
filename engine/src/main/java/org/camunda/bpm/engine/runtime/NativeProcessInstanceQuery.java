package org.camunda.bpm.engine.runtime;

import org.camunda.bpm.engine.query.NativeQuery;

/**
 * Allows querying of {@link ProcessInstance}s via native (SQL) queries
 * @author Bernd Ruecker (camunda)
 */
public interface NativeProcessInstanceQuery extends NativeQuery<NativeProcessInstanceQuery, ProcessInstance> {

}

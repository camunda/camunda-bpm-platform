package org.camunda.bpm.engine.task;

import org.camunda.bpm.engine.query.NativeQuery;

/**
 * Allows querying of {@link Task}s via native (SQL) queries
 * @author Bernd Ruecker (camunda)
 */
public interface NativeTaskQuery extends NativeQuery<NativeTaskQuery, Task> {

}

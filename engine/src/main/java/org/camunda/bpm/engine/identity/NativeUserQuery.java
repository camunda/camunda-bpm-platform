package org.camunda.bpm.engine.identity;

import org.camunda.bpm.engine.query.NativeQuery;

/**
 * Allows querying of {@link User}s via native (SQL) queries
 * @author Svetlana Dorokhova
 */
public interface NativeUserQuery extends NativeQuery<NativeUserQuery, User> {

}

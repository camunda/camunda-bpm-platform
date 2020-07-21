package org.camunda.bpm.engine.impl.db.entitymanager;

public enum OptimisticLockingResult {

  /**
   * Marks that that an {@link OptimisticLockingListener} resulted
   * in successfully handling, or ignoring, an {@link org.camunda.bpm.engine.OptimisticLockingException}.
   */
  IGNORE,

  /**
   * Marks that that an {@link OptimisticLockingListener} resulted
   * in a failure when handling an {@link org.camunda.bpm.engine.OptimisticLockingException}
   * and the exception should be re-thrown to the caller.
   */
  THROW,

  /**
   * Only used when operating on CockroachDB. Marks that that an {@link OptimisticLockingListener}
   * can't handle, or ignore, this exception, so the encompasing command needs to be completely retried
   * by the dedicated CockroachDB retry mechanism.
   */
  RETRY;
}

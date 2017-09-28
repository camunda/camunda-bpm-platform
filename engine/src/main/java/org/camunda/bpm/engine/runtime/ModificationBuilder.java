package org.camunda.bpm.engine.runtime;

import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;

public interface ModificationBuilder extends InstantiationBuilder<ModificationBuilder>{

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Cancel all instances of the given activity in an arbitrary order, which are:
   * <ul>
   *   <li>activity instances of that activity
   *   <li>transition instances entering or leaving that activity
   * </ul></p>
   *
   * <p>The cancellation order of the instances is arbitrary</p>
   *
   * @param activityId the activity for which all instances should be cancelled
   */
  ModificationBuilder cancelAllForActivity(String activityId);

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Cancel all instances of the given activity in an arbitrary order, which are:
   * <ul>
   *   <li>activity instances of that activity
   *   <li>transition instances entering or leaving that activity
   * </ul></p>
   *
   * <p>The cancellation order of the instances is arbitrary</p>
   *
   * @param activityId the activity for which all instances should be cancelled
   * @param cancelCurrentActiveActivityInstances
   */
  ModificationBuilder cancelAllForActivity(String activityId, boolean cancelCurrentActiveActivityInstances);

  /**
   * @param processInstanceIds the process instance ids to modify.
   */
  ModificationBuilder processInstanceIds(List<String> processInstanceIds);

  /**
   * @param processInstanceIds the process instance ids to modify.
   */
  ModificationBuilder processInstanceIds(String... processInstanceIds);

  /**
   * @param processInstanceQuery a query which selects the process instances to modify.
   *   Query results are restricted to process instances for which the user has {@link Permissions#READ} permission.
   */
  ModificationBuilder processInstanceQuery(ProcessInstanceQuery processInstanceQuery);

  /**
   * Skips custom execution listeners when creating/removing activity instances during modification
   */
  ModificationBuilder skipCustomListeners();

  /**
   * Skips io mappings when creating/removing activity instances during modification
   */
  ModificationBuilder skipIoMappings();

  /**
   * Execute the modification synchronously.
   *
   * @throws AuthorizationException
   *   if the user has not all of the following permissions
   *   <ul>
   *      <li>if the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE} or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}</li>
   *   </ul>
   */
  void execute();

  /**
   * Execute the modification asynchronously as batch. The returned batch
   * can be used to track the progress of the modification.
   *
   * @return the batch which executes the modification asynchronously.
   *
   * @throws AuthorizationException
   *   if the user has not all of the following permissions
   *   <ul>
   *     <li>{@link Permissions#CREATE} permission on {@link Resources#BATCH}</li>
   *   </ul>
   */
  Batch executeAsync();
}


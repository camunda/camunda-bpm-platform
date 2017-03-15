package org.camunda.bpm.engine.runtime;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;

public interface ModificationBuilder {

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Start before the specified activity.</p>
   *
   * <p>In particular:
   *   <ul>
   *     <li>Instantiate and execute the given activity (respects the asyncBefore
   *       attribute of the activity)</li>
   *   </ul>
   * </p>
   *
   * @param activityId the activity to instantiate
   */
  ModificationBuilder startBeforeActivity(String activityId);

  /**
   * Submits an instruction that behaves like {@link #startTransition(String)} and always instantiates
   * the single outgoing sequence flow of the given activity. Does not consider asyncAfter.
   *
   * @param activityId the activity for which the outgoing flow should be executed
   * @throws ProcessEngineException if the activity has 0 or more than 1 outgoing sequence flows
   */
  ModificationBuilder startAfterActivity(String activityId);

  /**
   * <p><i>Submits the instruction:</i></p>
   *
   * <p>Start the specified sequence flow.</p>
   *
   * <p>In particular:
   *   <ul>
   *     <li>Execute the given transition (does not consider sequence flow conditions)</li>
   *   </ul>
   * </p>
   *
   * @param transitionId the sequence flow to execute
   */
  ModificationBuilder startTransition(String transitionId);

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

  void setInstructions(List<AbstractProcessInstanceModificationCommand> instructions);

  /**
   * Skips custom execution listeners when creating/removing activity instances during migration
   */
  ModificationBuilder skipCustomListeners();

  /**
   * Skips io mappings when creating/removing activity instances during migration
   */
  ModificationBuilder skipIoMappings();

  void execute();

  Batch executeAsync();
}


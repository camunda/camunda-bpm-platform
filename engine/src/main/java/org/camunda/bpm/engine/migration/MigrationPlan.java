/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.migration;

import java.util.List;

/**
 * <p>Specifies how process instances from one process definition (the <i>source process definition</i>)
 * should be migrated to another process definition (the <i>target process definition</i>).
 *
 * <p>A migration plan consists of a number of {@link MigrationInstruction}s that tell which
 *   activity maps to which. The set of instructions is complete, i.e. the migration logic does not perform
 *   migration steps that are not given by the instructions
 *
 * @author Thorben Lindhauer
 */
public interface MigrationPlan {

  /**
   * @return the list of instructions that this plan consists of
   */
  List<MigrationInstruction> getInstructions();

  /**
   * @return the id of the process definition that is migrated from
   */
  String getSourceProcessDefinitionId();

  /**
   * @return the id of the process definition that is migrated to
   */
  String getTargetProcessDefinitionId();

}

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
package org.camunda.bpm.engine.impl.migration;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.migration.MigrationInstruction;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationInstructionImpl implements MigrationInstruction {

  protected List<String> sourceActivityIds;
  protected List<String> targetActivityIds;

  public MigrationInstructionImpl(String sourceActivityId, String targetActivityId) {
    this(Collections.singletonList(sourceActivityId), Collections.singletonList(targetActivityId));
  }

  public MigrationInstructionImpl(List<String> sourceActivityIds, List<String> targetActivityIds) {
    this.sourceActivityIds = sourceActivityIds;
    this.targetActivityIds = targetActivityIds;
  }

  public List<String> getSourceActivityIds() {
    return sourceActivityIds;
  }

  public List<String> getTargetActivityIds() {
    return targetActivityIds;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("MigrationInstruction[sourceActivities=[");
    for (int i = 0; i < sourceActivityIds.size(); i++) {
      sb.append(sourceActivityIds.get(i));
      if (i < sourceActivityIds.size() - 1) {
        sb.append(", ");
      }
    }

    sb.append("], targetActivityIds=[");
    for (int i = 0; i < targetActivityIds.size(); i++) {
      sb.append(targetActivityIds.get(i));
      if (i < targetActivityIds.size() - 1) {
        sb.append(", ");
      }
    }
    sb.append("]]");

    return sb.toString();
  }

}

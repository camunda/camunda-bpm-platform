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
package org.camunda.bpm.engine.rest.dto.dmn;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;

/**
 * @author Philipp Ossler
 */
public class DecisionResultDto {

  protected List<DecisionOutputDto> decisionOutputs;

  public List<DecisionOutputDto> getDecisionOutputs() {
    return decisionOutputs;
  }

  public void setDecisionOutputs(List<DecisionOutputDto> decisionOutputs) {
    this.decisionOutputs = decisionOutputs;
  }

  public static DecisionResultDto fromDecisionResult(DmnDecisionResult decisionResult) {
    DecisionResultDto dto = new DecisionResultDto();

    List<DecisionOutputDto> decisionOutputDtos = new ArrayList<DecisionOutputDto>();
    for (DmnDecisionOutput decisionOutput : decisionResult) {
      DecisionOutputDto decisionOutputDto = DecisionOutputDto.fromDecisionOutput(decisionOutput);
      decisionOutputDtos.add(decisionOutputDto);
    }
    dto.setDecisionOutputs(decisionOutputDtos);

    return dto;
  }

}

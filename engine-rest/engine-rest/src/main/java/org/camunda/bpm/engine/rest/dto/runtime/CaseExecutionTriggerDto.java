/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.List;
import java.util.Map;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionTriggerDto {

  private Map<String, TriggerVariableValueDto> variables;
  private List<VariableNameDto> deletions;

  public Map<String, TriggerVariableValueDto> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, TriggerVariableValueDto> variables) {
    this.variables = variables;
  }

  public List<VariableNameDto> getDeletions() {
    return deletions;
  }

  public void setDeletions(List<VariableNameDto> deletions) {
    this.deletions = deletions;
  }

}

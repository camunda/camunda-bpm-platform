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
package org.camunda.bpm.engine.rest.dto;

import java.util.List;

public class BpmnResourceReportDto implements ResourceReportDto {

  protected List<ProblemDto> errors;
  protected List<ProblemDto> warnings;

  public BpmnResourceReportDto(List<ProblemDto> errors,
      List<ProblemDto> warnings) {
    super();
    this.errors = errors;
    this.warnings = warnings;
  }

  // getter / setters ////////////////////////

  public List<ProblemDto> getErrors() {
    return errors;
  }

  public void setErrors(List<ProblemDto> errors) {
    this.errors = errors;
  }

  public List<ProblemDto> getWarnings() {
    return warnings;
  }

  public void setWarnings(List<ProblemDto> warnings) {
    this.warnings = warnings;
  }

}

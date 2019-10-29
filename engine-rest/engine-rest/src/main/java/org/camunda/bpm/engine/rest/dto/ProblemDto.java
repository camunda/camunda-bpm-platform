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

import org.camunda.bpm.engine.Problem;

/**
 * Dto for {@link Problem}
 *
 * The problem contains a list of bpmn element ids and other details for root
 * causing the issue.
 */
public class ProblemDto {

  protected String errorMessage;
  protected String resource;
  protected int line;
  protected int column;
  protected String mainBpmnElementId;
  protected List<String> bpmnElementIds;

  // transformer /////////////////////////////

  public static ProblemDto fromProblem(Problem problem) {
    ProblemDto dto = new ProblemDto();

    dto.setErrorMessage(problem.getErrorMessage());
    dto.setResource(problem.getResource());
    dto.setLine(problem.getLine());
    dto.setColumn(problem.getColumn());
    dto.setMainBpmnElementId(problem.getMainBpmnElementId());
    dto.setBpmnElementIds(problem.getBpmnElementIds());

    return dto;
  }

  // getter / setters ////////////////////////

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public int getLine() {
    return line;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public int getColumn() {
    return column;
  }

  public void setColumn(int column) {
    this.column = column;
  }

  public String getMainBpmnElementId() {
    return mainBpmnElementId;
  }

  public void setMainBpmnElementId(String mainBpmnElementId) {
    this.mainBpmnElementId = mainBpmnElementId;
  }

  public List<String> getBpmnElementIds() {
    return bpmnElementIds;
  }

  public void setBpmnElementIds(List<String> bpmnElementIds) {
    this.bpmnElementIds = bpmnElementIds;
  }

}

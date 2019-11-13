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
 * The problem contains a list of element ids and other details for root
 * causing the issue.
 */
public class ProblemDto {

  protected String message;
  protected int line;
  protected int column;
  protected String mainElementId;
  protected List<String> еlementIds;

  // transformer /////////////////////////////

  public static ProblemDto fromProblem(Problem problem) {
    ProblemDto dto = new ProblemDto();

    dto.setMessage(problem.getMessage());
    dto.setLine(problem.getLine());
    dto.setColumn(problem.getColumn());
    dto.setMainElementId(problem.getMainElementId());
    dto.setЕlementIds(problem.getElementIds());

    return dto;
  }

  // getter / setters ////////////////////////

  public String getMessage() {
    return message;
  }

  public void setMessage(String errorMessage) {
    this.message = errorMessage;
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

  public String getMainElementId() {
    return mainElementId;
  }

  public void setMainElementId(String mainElementId) {
    this.mainElementId = mainElementId;
  }

  public List<String> getЕlementIds() {
    return еlementIds;
  }

  public void setЕlementIds(List<String> elementIds) {
    this.еlementIds = elementIds;
  }

}

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
package org.camunda.bpm.engine.rest.dto.converter;

import org.camunda.bpm.engine.task.TaskCountByCandidateGroupResult;

import java.util.List;

/**
 * @author Roman Smirnov
 *
 */
public class TaskReportResultToCsvConverter {

  protected static String DELIMITER = ",";
  protected static String NEW_LINE_SEPARATOR = "\n";

  public static String CANDIDATE_GROUP_HEADER = "CANDIDATE_GROUP_NAME"
                                                + DELIMITER
                                                + "TASK_COUNT";

  public static String convertCandidateGroupReportResult(List<TaskCountByCandidateGroupResult> reports) {
    StringBuilder buffer = new StringBuilder();

    buffer.append(CANDIDATE_GROUP_HEADER);

    for (TaskCountByCandidateGroupResult report : reports) {
      buffer.append(NEW_LINE_SEPARATOR);
      buffer.append(report.getGroupName());
      buffer.append(DELIMITER);
      buffer.append(report.getTaskCount());
    }

    return buffer.toString();
  }

}

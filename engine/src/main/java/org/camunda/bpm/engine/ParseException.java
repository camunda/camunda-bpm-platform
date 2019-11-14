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
package org.camunda.bpm.engine;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.parser.ResourceReportImpl;

public class ParseException extends ProcessEngineException {

  private static final long serialVersionUID = 1L;

  protected List<ResourceReport> resorceReports;

  public ParseException(String exceptionMessage, String resource, List<Problem> errors, List<Problem> warnings) {
    super(exceptionMessage);
    ResourceReportImpl resourceReport = new ResourceReportImpl(resource, errors, warnings);
    List<ResourceReport> reports = new ArrayList<>();
    reports.add(resourceReport);
    this.resorceReports = reports;
  }

  public List<ResourceReport> getResorceReports() {
    return resorceReports;
  }

}

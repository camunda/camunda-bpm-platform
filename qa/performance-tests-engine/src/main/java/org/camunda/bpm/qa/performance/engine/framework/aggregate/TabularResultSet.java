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
package org.camunda.bpm.qa.performance.engine.framework.aggregate;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the aggregated results of a performance testsuite.
 *
 * @author Daniel Meyer
 *
 */
public class TabularResultSet {

  protected List<String> resultColumnNames = new ArrayList<String>();

  protected List<List<Object>> results = new ArrayList<List<Object>>();

  public void setResultColumnNames(List<String> resultColumnNames) {
    this.resultColumnNames = resultColumnNames;
  }

  public List<String> getResultColumnNames() {
    return resultColumnNames;
  }

  public List<List<Object>> getResults() {
    return results;
  }

  public void addResultRow(List<Object> row) {
    results.add(row);
  }

  public void setResults(List<List<Object>> results) {
    this.results = results;
  }

}

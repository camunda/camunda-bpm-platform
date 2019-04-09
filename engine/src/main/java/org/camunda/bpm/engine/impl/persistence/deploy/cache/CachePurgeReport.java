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
package org.camunda.bpm.engine.impl.persistence.deploy.cache;

import org.camunda.bpm.engine.impl.management.PurgeReporting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class CachePurgeReport implements PurgeReporting<Set<String>> {

  public static final String PROCESS_DEF_CACHE = "PROC_DEF_CACHE";
  public static final String BPMN_MODEL_INST_CACHE = "BPMN_MODEL_INST_CACHE";
  public static final String CASE_DEF_CACHE = "CASE_DEF_CACHE";
  public static final String CASE_MODEL_INST_CACHE = "CASE_MODEL_INST_CACHE";
  public static final String DMN_DEF_CACHE = "DMN_DEF_CACHE";
  public static final String DMN_REQ_DEF_CACHE = "DMN_REQ_DEF_CACHE";
  public static final String DMN_MODEL_INST_CACHE = "DMN_MODEL_INST_CACHE";

  /**
   * Key: cache name
   * Value: values
   */
  Map<String, Set<String>> deletedCache = new HashMap<String, Set<String>>();

  @Override
  public void addPurgeInformation(String key, Set<String> value) {
    deletedCache.put(key, new HashSet<String>(value));
  }

  @Override
  public Map<String, Set<String>> getPurgeReport() {
    return deletedCache;
  }

  @Override
  public String getPurgeReportAsString() {
    StringBuilder builder = new StringBuilder();
    for (String key : deletedCache.keySet()) {
      builder.append("Cache: ").append(key)
             .append(" contains: ").append(getReportValue(key))
             .append("\n");
    }
    return builder.toString();
  }

  @Override
  public Set<String> getReportValue(String key) {
    return deletedCache.get(key);
  }

  @Override
  public boolean containsReport(String key) {
    return deletedCache.containsKey(key);
  }

  public boolean isEmpty() {
    return deletedCache.isEmpty();
  }
}

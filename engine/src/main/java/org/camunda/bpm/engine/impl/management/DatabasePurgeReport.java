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
package org.camunda.bpm.engine.impl.management;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class DatabasePurgeReport implements PurgeReporting<Long> {

  /**
   * Key: table name
   * Value: entity count
   */
  Map<String, Long> deletedEntities = new HashMap<String, Long>();
  boolean dbContainsLicenseKey;

  @Override
  public void addPurgeInformation(String key, Long value) {
    deletedEntities.put(key, value);
  }

  @Override
  public Map<String, Long> getPurgeReport() {
    return deletedEntities;
  }

  @Override
  public String getPurgeReportAsString() {
    StringBuilder builder = new StringBuilder();
    for (String key : deletedEntities.keySet()) {
      builder.append("Table: ").append(key)
        .append(" contains: ").append(getReportValue(key))
        .append(" rows\n");
    }
    return builder.toString();
  }

  @Override
  public Long getReportValue(String key) {
    return deletedEntities.get(key);
  }

  @Override
  public boolean containsReport(String key) {
    return deletedEntities.containsKey(key);
  }

  public boolean isEmpty() {
    return deletedEntities.isEmpty();
  }

  public boolean isDbContainsLicenseKey() {
    return dbContainsLicenseKey;
  }

  public void setDbContainsLicenseKey(boolean dbContainsLicenseKey) {
    this.dbContainsLicenseKey = dbContainsLicenseKey;
  }
}

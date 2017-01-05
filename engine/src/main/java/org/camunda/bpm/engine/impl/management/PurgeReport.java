/*
 * Copyright 2016 camunda services GmbH.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.camunda.bpm.engine.impl.management;

import org.camunda.bpm.engine.impl.persistence.deploy.cache.CachePurgeReport;

/**
 * The purge report contains information about the deleted rows for each table
 * and also the deleted values which are removed from the deployment cache.
 * If now entities are deleted since the database was already clean the purge report is empty.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class PurgeReport {

  private DatabasePurgeReport databasePurgeReport;
  private CachePurgeReport cachePurgeReport;

  public DatabasePurgeReport getDatabasePurgeReport() {
    return databasePurgeReport;
  }

  public void setDatabasePurgeReport(DatabasePurgeReport databasePurgeReport) {
    this.databasePurgeReport = databasePurgeReport;
  }

  public CachePurgeReport getCachePurgeReport() {
    return cachePurgeReport;
  }

  public void setCachePurgeReport(CachePurgeReport cachePurgeReport) {
    this.cachePurgeReport = cachePurgeReport;
  }

  public boolean isEmpty() {
    return cachePurgeReport.isEmpty() && databasePurgeReport.isEmpty();
  }
}

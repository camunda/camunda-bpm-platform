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
package org.camunda.bpm.engine.impl.persistence.entity;

import org.camunda.bpm.engine.impl.persistence.AbstractManager;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class PropertyManager extends AbstractManager {

  public PropertyEntity findPropertyById(String propertyId) {
    return getDbEntityManager().selectById(PropertyEntity.class, propertyId);
  }

  public void acquireExclusiveLock() {
    // We lock a special deployment lock property
    getDbEntityManager().lock("lockDeploymentLockProperty");

  }

  public void acquireExclusiveLockForHistoryCleanupJob() {
    // We lock a special history cleanup lock property
    getDbEntityManager().lock("lockHistoryCleanupJobLockProperty");

  }

  public void acquireExclusiveLockForStartup() {
    // We lock a special startup lock property
    getDbEntityManager().lock("lockStartupLockProperty");

  }

  public void acquireExclusiveLockForTelemetry() {
    // We lock a special telemetry lock property
    getDbEntityManager().lock("lockTelemetryLockProperty");
  }

  public void acquireExclusiveLockForInstallationId() {
    // We lock a special installation id lock property
    getDbEntityManager().lock("lockInstallationIdLockProperty");
  }

}

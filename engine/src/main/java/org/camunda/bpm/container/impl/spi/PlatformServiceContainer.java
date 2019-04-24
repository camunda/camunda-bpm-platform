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
package org.camunda.bpm.container.impl.spi;

import java.util.List;
import java.util.Set;

import javax.management.ObjectName;

import org.camunda.bpm.container.impl.spi.DeploymentOperation.DeploymentOperationBuilder;

/**
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Ronny Bräunlich
 *
 */
public interface PlatformServiceContainer {

  <S> void startService(ServiceType serviceType, String localName, PlatformService<S> service);

  <S> void startService(String serviceName, PlatformService<S> service);

  void stopService(ServiceType serviceType, String localName);

  void stopService(String serviceName);

  DeploymentOperationBuilder createDeploymentOperation(String name);

  DeploymentOperationBuilder createUndeploymentOperation(String name);

  /**
   * get a specific service by name or null if no such Service exists.
   *
   */
  <S> S getService(ServiceType type, String localName);

  /**
   * get the service value for a specific service by name or null if no such
   * Service exists.
   *
   */
  <S> S getServiceValue(ServiceType type, String localName);

  /**
   * @return all services for a specific {@link ServiceType}
   */
  <S> List<PlatformService<S>> getServicesByType(ServiceType type);

  /**
   * @return the service names ( {@link ObjectName} ) for all services for a given type
   */
  Set<String> getServiceNames(ServiceType type);

  /**
   * @return the values of all services for a specific {@link ServiceType}
   */
  <S> List<S> getServiceValuesByType(ServiceType type);

  void executeDeploymentOperation(DeploymentOperation operation);

  /**
   * A ServiceType is a collection of services that share a common name prefix.
   */
  public interface ServiceType {

    /**
     * Returns a wildcard name that allows to query the service container
     * for all services of the type represented by this ServiceType.
     */
    public String getTypeName();

  }


}
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
package org.camunda.bpm.engine.rest.util;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

import javax.ws.rs.core.Response.Status;
import java.util.Iterator;
import java.util.ServiceLoader;

public class EngineUtil {

  /**
   * Look up the process engine from the {@link ProcessEngineProvider}. If engineName is null, the default engine is returned.
   * @param engineName
   * @return
   */
  public static ProcessEngine lookupProcessEngine(String engineName) {

    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader.load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();

    if(iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      if (engineName == null) {
        return provider.getDefaultProcessEngine();
      } else {
        return provider.getProcessEngine(engineName);
      }
    } else {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, "Could not find an implementation of the "+ProcessEngineProvider.class+"- SPI");
    }
  }

  public static ProcessEngineProvider getProcessEngineProvider() {
    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader.load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();

    if (iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      return provider;
    } else {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, "No process engine provider found");
    }
  }
}

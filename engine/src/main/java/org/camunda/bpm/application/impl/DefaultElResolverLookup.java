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
package org.camunda.bpm.application.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationElResolver;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.javax.el.CompositeELResolver;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;

/**
 * @author Daniel Meyer
 *
 */
public class DefaultElResolverLookup {

  private final static ProcessApplicationLogger LOG = ProcessEngineLogger.PROCESS_APPLICATION_LOGGER;

  public final static ELResolver lookupResolver(AbstractProcessApplication processApplication) {

    ServiceLoader<ProcessApplicationElResolver> providers = ServiceLoader.load(ProcessApplicationElResolver.class);
    List<ProcessApplicationElResolver> sortedProviders = new ArrayList<ProcessApplicationElResolver>();
    for (ProcessApplicationElResolver provider : providers) {
      sortedProviders.add(provider);
    }

    if(sortedProviders.isEmpty()) {
      return null;

    } else {
      // sort providers first
      Collections.sort(sortedProviders, new ProcessApplicationElResolver.ProcessApplicationElResolverSorter());

      // add all providers to a composite resolver
      CompositeELResolver compositeResolver = new CompositeELResolver();
      StringBuilder summary = new StringBuilder();
      summary.append(String.format("ElResolvers found for Process Application %s", processApplication.getName()));

      for (ProcessApplicationElResolver processApplicationElResolver : sortedProviders) {
        ELResolver elResolver = processApplicationElResolver.getElResolver(processApplication);

        if (elResolver != null) {
          compositeResolver.add(elResolver);
          summary.append(String.format("Class %s", processApplicationElResolver.getClass().getName()));
        }
        else {
          LOG.noElResolverProvided(processApplication.getName(), processApplicationElResolver.getClass().getName());
        }
      }

      LOG.paElResolversDiscovered(summary.toString());

      return compositeResolver;
    }

  }

}

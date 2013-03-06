/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.camunda.bpm.engine.impl.javax.el.CompositeELResolver;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;

/**
 * @author Daniel Meyer
 *
 */
public class DefaultElResolverLookup {
  
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
      for (ProcessApplicationElResolver processApplicationElResolver : sortedProviders) {
        compositeResolver.add(processApplicationElResolver.getElResolver(processApplication));        
      }
      
      return compositeResolver;
    }
    
  }

}

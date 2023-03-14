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
package org.camunda.bpm.container.impl.jboss.util;

import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * <p>Utiliy class 
 * @author Daniel Meyer
 *
 */
public class BindingUtil {
  
  public static ServiceController<ManagedReferenceFactory> createJndiBindings(ServiceTarget target, ServiceName serviceName, String binderServiceName,  ManagedReferenceFactory managedReferenceFactory) {

    BinderService binderService = new BinderService(binderServiceName);
    ServiceBuilder<ManagedReferenceFactory> serviceBuilder = target
            .addService(serviceName, binderService)
            .addDependency(ContextNames.GLOBAL_CONTEXT_SERVICE_NAME, ServiceBasedNamingStore.class, binderService.getNamingStoreInjector());
    binderService.getManagedObjectInjector().inject(managedReferenceFactory);
            
    return serviceBuilder.install();
  }
  
}

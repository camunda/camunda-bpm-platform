/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.subsystem.impl.util;

import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;

import com.camunda.fox.platform.subsystem.impl.platform.ContainerPlatformService;


/**
 * Used by the {@link ContainerProcessEngineService} to create the jndi bindings
 * 
 * @author Daniel Meyer
 */
public class PlatformServiceReferenceFactory implements ManagedReferenceFactory {

  private final ContainerPlatformService service;

  public PlatformServiceReferenceFactory(ContainerPlatformService service) {
    this.service = service;
  }

  public ManagedReference getReference() {
    return new ManagedReference() {

      public void release() {
      }

      public Object getInstance() {
        return service;
      }
    };
  }
}

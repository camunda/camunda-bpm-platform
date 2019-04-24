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
package org.camunda.bpm.container.impl.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessApplicationService;

/**
 * <p>{@link ObjectFactory} that can be used for binding the {@link ProcessApplicationService} into JNDI</p> 
 * 
 * @author Roman Smirnov
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationServiceObjectFactory implements ObjectFactory {

  public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
    return BpmPlatform.getProcessApplicationService();
  }

}

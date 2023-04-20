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
package org.camunda.bpm.engine.cdi.impl.event;

import java.lang.annotation.Annotation;
import javax.enterprise.inject.spi.BeanManager;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.cdi.BusinessProcessEvent;
import org.camunda.bpm.engine.cdi.impl.util.BeanManagerLookup;
import org.camunda.bpm.engine.delegate.ExecutionListener;

/**
 * Generic {@link ExecutionListener} publishing events using the CDI event
 * infrastructure.
 *
 * @author Daniel Meyer
 */
public class CdiEventListener extends AbstractCdiEventListener {

  private static final long serialVersionUID = 1L;

  @Override
  protected void fireEvent(BusinessProcessEvent event, Annotation[] qualifiers) {
    getBeanManager().fireEvent(event, qualifiers);
  }

  protected BeanManager getBeanManager() {
    BeanManager bm = BeanManagerLookup.getBeanManager();
    if (bm == null) {
      throw new ProcessEngineException("No cdi bean manager available, cannot publish event.");
    }
    return bm;
  }

}

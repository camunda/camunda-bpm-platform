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

import org.jboss.msc.service.LifecycleEvent;
import org.jboss.msc.service.LifecycleListener;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

import java.util.Collection;

/**
 * <p>Service Listener that adds / removes services to / from a collection as they
 * are added / removed to the service controller.</p>
 *
 * @author Daniel Meyer
 *
 * @param <S>
 *          the type of the service to track
 */
public class ServiceTracker<S> implements LifecycleListener {

  protected Collection<S> serviceCollection;
  protected ServiceName typeToTrack;

  public ServiceTracker(ServiceName typeToTrack, Collection<S> serviceCollection) {
    this.serviceCollection = serviceCollection;
    this.typeToTrack = typeToTrack;
  }

  @Override
  @SuppressWarnings({ "unchecked" })
  public void handleEvent(final ServiceController<?> controller, final LifecycleEvent event) {

    if(!typeToTrack.isParentOf(controller.getName())) {
      return;
    }

    if(event.equals(LifecycleEvent.UP)) {
      serviceCollection.add((S) controller.getValue());
    } else  {
      serviceCollection.remove(controller.getValue());
    }

  }
}


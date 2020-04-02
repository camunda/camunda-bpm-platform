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
package org.camunda.bpm.container.impl.threading.ra.inflow;

import java.io.Serializable;

import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;




/**
 * 
 * @author Daniel Meyer
 */
@Activation(
  messageListeners = { JobExecutionHandler.class }
)
public class JobExecutionHandlerActivationSpec implements ActivationSpec, Serializable {

  private static final long serialVersionUID = 1L;
  
  private ResourceAdapter ra;
  /** Please check #CAM-9811  */
  private String dummyPojo;

  public void validate() throws InvalidPropertyException {
    // nothing to do (the endpoint has no activation properties)
  }

  public ResourceAdapter getResourceAdapter() {
    return ra;
  }

  public void setResourceAdapter(ResourceAdapter ra) {
    this.ra = ra;
  }

  public void setDummyPojo(String dummyPojo) {
    this.dummyPojo = dummyPojo;
  }

  public String getDummyPojo() {
    return dummyPojo;
  }

}

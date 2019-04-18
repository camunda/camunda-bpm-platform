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
package org.camunda.bpm.container.impl.threading.ra.outbound;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;




/**
 * 
 * @author Daniel Meyer
 *
 */
public class JcaExecutorServiceConnectionFactoryImpl implements JcaExecutorServiceConnectionFactory {

  private static final long serialVersionUID = 1L;

  protected Reference reference;
  protected JcaExecutorServiceManagedConnectionFactory mcf;
  protected ConnectionManager connectionManager;
  
  public JcaExecutorServiceConnectionFactoryImpl() {
  }
  
  public JcaExecutorServiceConnectionFactoryImpl(JcaExecutorServiceManagedConnectionFactory mcf, ConnectionManager cxManager) {
    this.mcf = mcf;
    this.connectionManager = cxManager;
  }

  public JcaExecutorServiceConnection getConnection() throws ResourceException {
    return (JcaExecutorServiceConnection) connectionManager.allocateConnection(mcf, null);
  }

  public Reference getReference() throws NamingException {
    return reference;
  }

  public void setReference(Reference reference) {
    this.reference = reference;
  }

}

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

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;


@ConnectionDefinition(
    connectionFactory = JcaExecutorServiceConnectionFactory.class, 
    connectionFactoryImpl = JcaExecutorServiceConnectionFactoryImpl.class, 
    connection = JcaExecutorServiceConnection.class, 
    connectionImpl = JcaExecutorServiceConnectionImpl.class
  )
public class JcaExecutorServiceManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation {

  private static final long serialVersionUID = 1L;
  
  protected ResourceAdapter ra;
  protected PrintWriter logwriter;
  
  public JcaExecutorServiceManagedConnectionFactory() {
  }

  public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
    return new JcaExecutorServiceConnectionFactoryImpl(this, cxManager);
  }

  public Object createConnectionFactory() throws ResourceException {
    throw new ResourceException("This resource adapter doesn't support non-managed environments");
  }

  public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
    return new JcaExecutorServiceManagedConnection(this);
  }
  
  @SuppressWarnings("rawtypes")
  public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
    ManagedConnection result = null;
    Iterator it = connectionSet.iterator();
    while (result == null && it.hasNext()) {
      ManagedConnection mc = (ManagedConnection) it.next();
      if (mc instanceof JcaExecutorServiceManagedConnection) {
        result = mc;
      }

    }
    return result;
  }

  public PrintWriter getLogWriter() throws ResourceException {
    return logwriter;
  }

  public void setLogWriter(PrintWriter out) throws ResourceException {
    logwriter = out;
  }

  public ResourceAdapter getResourceAdapter() {
    return ra;
  }

  public void setResourceAdapter(ResourceAdapter ra) {
    this.ra = ra;
  }

  @Override
  public int hashCode() {
    return 31;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null)
      return false;
    if (other == this)
      return true;
    if (!(other instanceof JcaExecutorServiceManagedConnectionFactory))
      return false;
    return true;
  }

}

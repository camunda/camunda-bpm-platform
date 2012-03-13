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
package com.camunda.fox.platform.deployer.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.activiti.engine.impl.interceptor.CommandExecutor;

import com.camunda.fox.platform.deployer.AbstractActivitiDeployer;

/**
 * Default implementation of {@link AbstractActivitiDeployer}, looking up the
 * {@link CommandExecutor} EJB bound by the activiti-enterprise-service.
 * 
 * @author Daniel Meyer
 */
public class JndiCommandExecutorDeployer extends AbstractActivitiDeployer {

  protected String commandExecutorJndiName = "/CommandExecutorBean/local";
  
  private static Logger log = Logger.getLogger(JndiCommandExecutorDeployer.class.getName());

  protected CommandExecutor getCommandExecutor() {
    try {
      return (CommandExecutor) new InitialContext().lookup(commandExecutorJndiName);
    } catch (NamingException e) {
      log.log(Level.WARNING, "Could not lookup command executor in Jndi: " + e.getMessage());      
      return null;
    }
  }
    
  public void setCommandExecutorJndiName(String commandExecutorJndiName) {
    this.commandExecutorJndiName = commandExecutorJndiName;
  }
}

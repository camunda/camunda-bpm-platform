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
package com.camunda.fox.platform.api;

import java.util.List;
import java.util.concurrent.Future;

import org.activiti.engine.ProcessEngine;

import com.camunda.fox.platform.spi.ProcessArchive;

/**
 * <p>The process archive service manages {@link ProcessArchive} deployments.</p>
 *
 * <p>This class allows to install and uninstall {@link ProcessArchive ProcessArchives} 
 * to a managed process engine and through that process, obtain a handle to 
 * a {@link ProcessEngine}.</p>
 * 
 * <p>Users of this class may look up an instance of the service through a lookup strategy
 * appropriate for the platform they are using (Examples: Jndi, OSGi Service Registry ...)</p>
 * 
 * @author Daniel Meyer
 */
public interface ProcessArchiveService {
  
  public Future<ProcessArchiveInstallOperation> installProcessArchive(ProcessArchive processArchive);

  public Future<ProcessArchiveUninstallOperation> unInstallProcessArchive(ProcessArchive processArchive);
  
  public Future<ProcessArchiveUninstallOperation> unInstallProcessArchive(String processArchiveName);
  
  public List<ProcessArchive> getInstalledProcessArchives();
  
  public List<ProcessArchive> getInstalledProcessArchives(ProcessEngine processEngine);
  
  public List<ProcessArchive> getInstalledProcessArchives(String processEngineName);
  
  // operations ////////////////////////////////////
  
  public static interface ProcessArchiveInstallOperation {
    
    /**
     * @return true if the {@link ProcessArchive} could successfully be installed 
     */
    public boolean wasSuccessful();
    
    /**
     * @return the {@link ProcessEngine} to which the {@link ProcessArchive} was installed.
     */
    public ProcessEngine getProcessenEngine();
    
    /**
     * @return the id of the deployment made to the {@link ProcessEngine}. 
     */
    public String processEngineDeploymentId();
    
    /**
     * @return the exception that was thrown while attempting to install the {@link ProcessArchive}
     */
    public Throwable getException();
  }
  
  public static interface ProcessArchiveUninstallOperation {
    
    /**
     * @return true if the {@link ProcessArchive} could successfully be uninstalled 
     */
    public boolean wasSuccessful();
        
    /**
     * @return the exception that was thrown while attempting to uninstall the {@link ProcessArchive}
     */
    public Throwable getException();
  }

}
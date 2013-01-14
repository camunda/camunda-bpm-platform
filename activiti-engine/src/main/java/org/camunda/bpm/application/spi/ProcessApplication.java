/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.application.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;

import com.camunda.fox.client.impl.ProcessArchiveImpl;
import com.camunda.fox.client.impl.parser.DefaultProcessesXmlParser;
import com.camunda.fox.client.impl.parser.spi.ProcessesXmlParser;
import com.camunda.fox.client.impl.schema.ProcessesXml;
import com.camunda.fox.client.impl.schema.ProcessesXml.ProcessArchiveXml;
import com.camunda.fox.client.impl.spi.ProcessArchiveExtension;
import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.api.ProcessArchiveService.ProcessArchiveInstallation;
import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.spi.ProcessArchive;

/**
 * 
 * 
 * @author Daniel Meyer
 *
 */
public abstract class ProcessApplication {
  
  private static final Logger log = Logger.getLogger(ProcessApplication.class.getName());
  
  public static final String PROCESSES_XML_FILE_LOCATION = "META-INF/processes.xml";
      
  protected ProcessArchiveService processArchiveService;
  
  protected ProcessEngineService processEngineService;
  
  protected List<ProcessApplicationExtension> processApplicationExtensions = new ArrayList<ProcessApplicationExtension>();
  
  protected Map<ProcessArchive, ProcessEngine> installedProcessArchives = new HashMap<ProcessArchive, ProcessEngine>();
  
  // lifecycle /////////////////////////////////////////////////////

  public void start() {
    lookupProcessEngineService();
    lookupArchiveService();
    fireBeforeProcessArchiveStart();
    installProcessArchives();
    fireAfterProcessArchiveStart();
  }

  protected abstract void lookupArchiveService();
  
  protected abstract void lookupProcessEngineService();

  public void stop() {
    fireBeforeProcessArchiveStop();
    uninstallProcessArchives();
    fireAfterProcessArchiveStop();
  }
  
  protected void installProcessArchives() {
    
    final ProcessesXmlParser parser = getProcessesXmlParser();
    List<ProcessesXml> processesXmls = parser.parseProcessesXml(PROCESSES_XML_FILE_LOCATION);
  
    if (processesXmls.size() > 0) {
      try {
        List<ProcessArchive> processArchives = getConfiguredProcessArchives(processesXmls);
        for (ProcessArchive processArchive : processArchives) {
          ProcessArchiveInstallation installation = processArchiveService.installProcessArchive(processArchive);
          installedProcessArchives.put(processArchive, installation.getProcessEngine());
        }
      } catch(RuntimeException e) {
        uninstallProcessArchives();
        throw new FoxPlatformException("Could not deploy process archives", e);          
      }
    } else {
      log.log(Level.INFO, "No " + PROCESSES_XML_FILE_LOCATION + " found. Not creating a process archive installation.");
    }
  }

  protected void uninstallProcessArchives() { 
    for (ProcessArchive processArchive : installedProcessArchives.keySet()) {
      try {
        processArchiveService.unInstallProcessArchive(processArchive);
      }catch (Exception e) {
        log.log(Level.WARNING, "Exception while uninstalling process archive '"+processArchive.getName(), e);
      }
    }
  }

  protected List<ProcessArchive> getConfiguredProcessArchives(List<ProcessesXml> processesXmls) {
    List<ProcessArchive> processArchives = new ArrayList<ProcessArchive>();
    List<String> processArchiveNamesSeen = new ArrayList<String>();
    for (ProcessesXml processesXml : processesXmls) {      
      for (ProcessArchiveXml processArchiveXml : processesXml.processArchives) {
        if(processArchiveXml.name == null) {
          setProcessArchiveName(processArchiveXml);
        }
        if(processArchiveNamesSeen.contains(processArchiveXml.name)) {
          throw new FoxPlatformException("Cannot install more than one process archive with name '" + processArchiveXml.name
                  + "'. Make sure to set different names when declaring more than a single process-archive in '"+PROCESSES_XML_FILE_LOCATION+"'.");
        } else {
          processArchiveNamesSeen.add(processArchiveXml.name);
        }
        
        if(processArchiveXml.configuration.processEngineName == null) {
          processArchiveXml.configuration.processEngineName = processEngineService.getDefaultProcessEngine().getName();
        }
        
        processArchives.add(new ProcessArchiveImpl(processArchiveXml, processesXml.metaFileUrl, getReference(), this));      
      }    
    }
    return processArchives;
  }
    
  // Configuration /////////////////////////////////////////////////////////
  
  /**
   * @return the name of this process application
   */
  public abstract String getName();
  
  /**
   * <p>Invoked by the process engine if the process application is registered with the process engine 
   * and the process engine is about to stop.</p>
   *  
   * <p>Override this method if the process application needs to execute custom behavior in this scenario.</p> 
   * 
   * @param processEngine the process engine to be stopped.
   */
  public void processEngineStopping(ProcessEngine processEngine) {
     
  }
   
  /**
   * <p>Returns a globally sharable reference to this process application. This reference may be safely passed 
   * to the process engine.</p>  
   * 
   * @return a globally sharable reference to this process application. 
   */
  public abstract ProcessApplicationReference getReference();

  // Runtime ////////////////////////////////////////////

  /**
   * The default implementation simply modifies the Context {@link ClassLoader}
   * 
   * @param callable the callable to be executed "within" this process application.
   * @return the result of the callback
   * @throws Exception 
   */
  public <T> T execute(Callable<T> callable) throws Exception {
    ClassLoader originalClassloader = ClassLoaderUtil.getContextClassloader();
    
    ClassLoader processApplicationClassloader = getProcessApplicationClassloader();
    
    try {
      ClassLoaderUtil.setContextClassloader(processApplicationClassloader);
      
      return executeCallback(callable);
      
    } finally {
      ClassLoaderUtil.setContextClassloader(originalClassloader);
    }
    
  }

  /**
   * Handles the actual execution of the callback
   * 
   * @param callback
   * @return
   * @throws Exception 
   */
  protected <T> T executeCallback(Callable<T> callback) throws Exception {
    return callback.call();
  }

  /**
   * <p>Override this method to provide an environment-specific {@link ClassLoader}.</p>
   * 
   * <p><strong>NOTE: the process engine must <em>never</em> cache any references to this {@link ClassLoader} 
   * or to classes obtained through this {@link ClassLoader}.</strong></p>
   * 
   * @return the {@link ClassLoader} that can be used to load classes and resources from this process application.
   */
  public ClassLoader getProcessApplicationClassloader() {
    // the default implementation uses the classloader that loaded 
    // the application-provided subclass of this class.    
    return getClass().getClassLoader();
  }
  
  // extensions support /////////////////////////////////////////////////
  

  protected ClassLoader getProcessApplicationExtensionClassloader() {
    // TODO: use SecutiryManager / Priviledged action
    if(Thread.currentThread().getContextClassLoader() != null) {
     return Thread.currentThread().getContextClassLoader(); 
    } else {
      return getClass().getClassLoader();
    }
  }

  protected ProcessesXmlParser getProcessesXmlParser() {
    ServiceLoader<ProcessesXmlParser> parserLoader = ServiceLoader.load(ProcessesXmlParser.class, getProcessApplicationExtensionClassloader());
    Iterator<ProcessesXmlParser> iterator = parserLoader.iterator();
    if(iterator.hasNext()) {
      return iterator.next();
    } else {
      return new DefaultProcessesXmlParser(getProcessApplicationClassloader());
    }
  }

  protected void setProcessArchiveName(ProcessArchiveXml processArchive) {
    if(processArchive.name == null || processArchive.name.length() == 0) {      
      processArchive.name = getName();
    }
  }

  public Map<ProcessArchive, ProcessEngine> getInstalledProcessArchives() {
    return installedProcessArchives;
  }

  public ProcessArchiveService getProcessArchiveService() {
    return processArchiveService;
  }

  public ProcessEngineService getProcessEngineService() {
    return processEngineService;
  }

  private void initProcessApplicationExtensions() {
    ServiceLoader<ProcessArchiveExtension> processArchiveExtensionLoader = ServiceLoader.load(ProcessArchiveExtension.class, getProcessApplicationClassloader());
    Iterator<ProcessArchiveExtension> loadableExtensions = processArchiveExtensionLoader.iterator();
    while (loadableExtensions.hasNext()) {
      ProcessArchiveExtension processArchiveExtension = (ProcessArchiveExtension) loadableExtensions.next();
      processApplicationExtensions.add(processArchiveExtension);
    }
    
    ServiceLoader<ProcessApplicationExtension> processApplicationExtensionLoader = ServiceLoader.load(ProcessApplicationExtension.class, getProcessApplicationClassloader());
    Iterator<ProcessApplicationExtension> extensions = processApplicationExtensionLoader.iterator();
    while (extensions.hasNext()) {
      ProcessApplicationExtension processArchiveExtension = (ProcessApplicationExtension) extensions.next();
      processApplicationExtensions.add(processArchiveExtension);
    }
    
  }

  protected void fireBeforeProcessArchiveStart() {
    initProcessApplicationExtensions();
    
    for (ProcessApplicationExtension processArchiveExtension : processApplicationExtensions) {
      try {
        processArchiveExtension.beforeProcessArchiveStart(this);
      }catch (Exception e) {
        throw new FoxPlatformException("Exception while invoking 'beforeProcessArchiveStart' for ProcessArchiveExtension "+processArchiveExtension.getClass(), e);
      }
    }
  }

  protected void fireAfterProcessArchiveStart() {
    for (ProcessApplicationExtension processArchiveExtension : processApplicationExtensions) {
      try {
        processArchiveExtension.afterProcessArchiveStart(this);
      }catch (Exception e) {
        log.log(Level.SEVERE, "Exception while invoking 'afterProcessArchiveStart' for ProcessArchiveExtension "+processArchiveExtension.getClass(), e);
      }
    }
  }

  protected void fireBeforeProcessArchiveStop() {
    for (ProcessApplicationExtension processArchiveExtension : processApplicationExtensions) {
      try {
        processArchiveExtension.beforeProcessArchiveStop(this);
      }catch (Exception e) {
        throw new FoxPlatformException("Exception while invoking 'beforeProcessArchiveStop' for ProcessArchiveExtension "+processArchiveExtension.getClass(), e);
      }
    }
  }

  protected void fireAfterProcessArchiveStop() {
    for (ProcessApplicationExtension processArchiveExtension : processApplicationExtensions) {
      try {
        processArchiveExtension.afterProcessArchiveStop(this);
      }catch (Exception e) {
        log.log(Level.SEVERE, "Exception while invoking 'afterProcessArchiveStop' for ProcessArchiveExtension "+processArchiveExtension.getClass(), e);
      }
    }
  }

}

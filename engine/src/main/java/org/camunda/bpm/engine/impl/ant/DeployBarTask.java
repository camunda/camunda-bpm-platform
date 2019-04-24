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
package org.camunda.bpm.engine.impl.ant;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngineInfo;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.LogUtil;


/**
 * @author Tom Baeyens
 */
public class DeployBarTask extends Task {
  
  String processEngineName = ProcessEngines.NAME_DEFAULT;
  File file;
  List<FileSet> fileSets;
  
  public void execute() throws BuildException {
    List<File> files = new ArrayList<File>();
    if (file!=null) {
      files.add(file);
    }
    if (fileSets!=null) {
      for (FileSet fileSet: fileSets) {
        DirectoryScanner directoryScanner = fileSet.getDirectoryScanner(getProject());
        File baseDir = directoryScanner.getBasedir();
        String[] includedFiles = directoryScanner.getIncludedFiles();
        String[] excludedFiles = directoryScanner.getExcludedFiles();
        List<String> excludedFilesList = Arrays.asList(excludedFiles);
        for (String includedFile: includedFiles) {
          if (!excludedFilesList.contains(includedFile)) {
            files.add(new File(baseDir, includedFile));
          }
        }
      }
    }
    
    Thread currentThread = Thread.currentThread();
    ClassLoader originalClassLoader = currentThread.getContextClassLoader(); 
    currentThread.setContextClassLoader(DeployBarTask.class.getClassLoader());
    
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
    
    try {
      log("Initializing process engine " + processEngineName);
      ProcessEngines.init();
      ProcessEngine processEngine = ProcessEngines.getProcessEngine(processEngineName);
      if (processEngine == null) {
        List<ProcessEngineInfo> processEngineInfos = ProcessEngines.getProcessEngineInfos();
        if( processEngineInfos != null && processEngineInfos.size() > 0 )
        {
          // Since no engine with the given name is found, we can't be 100% sure which ProcessEngineInfo
          // is causing the error. We should show ALL errors and process engine names / resource URL's.
          String message = getErrorMessage(processEngineInfos, processEngineName);
          throw new ProcessEngineException(message);
        }
        else
        	throw new ProcessEngineException("Could not find a process engine with name '" + processEngineName + "', no engines found. " +
        	        "Make sure an engine configuration is present on the classpath");
      }
      RepositoryService repositoryService = processEngine.getRepositoryService();

      log("Starting to deploy " + files.size() + " files");
      for (File file: files) {
        String path = file.getAbsolutePath();
        log("Handling file " + path);
        try {
          FileInputStream inputStream = new FileInputStream(file);
          try {
            log("deploying bar "+path);
            repositoryService
                .createDeployment()
                .name(file.getName())
                .addZipInputStream(new ZipInputStream(inputStream))
                .deploy();
          } finally {
            IoUtil.closeSilently(inputStream);
          }
        } catch (Exception e) {
          throw new BuildException("couldn't deploy bar "+path+": "+e.getMessage(), e);
        }
      }

    } finally {
      currentThread.setContextClassLoader(originalClassLoader);
    }
  }

  private String getErrorMessage(List<ProcessEngineInfo> processEngineInfos, String name) {
    StringBuilder builder = new StringBuilder("Could not find a process engine with name ");
    builder.append(name).append(", engines loaded:\n");
    for (ProcessEngineInfo engineInfo : processEngineInfos) {
      String engineName = (engineInfo.getName() != null) ? engineInfo.getName() : "unknown";
      builder.append("Process engine name: ").append(engineName);
      builder.append(" - resource: ").append(engineInfo.getResourceUrl());
      builder.append(" - status: ");

      if (engineInfo.getException() != null) {
        builder.append("Error while initializing engine. ");
        if (engineInfo.getException().indexOf("driver on UnpooledDataSource") != -1) {
          builder.append("Exception while initializing process engine! Database or database driver might not have been configured correctly.")
                  .append("Please consult the user guide for supported database environments or build.properties. Stacktrace: ")
                  .append(engineInfo.getException());
        } else {
          builder.append("Stacktrace: ").append(engineInfo.getException());
        }
      } else {
        // Process engine initialised without exception
        builder.append("Initialised");
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  public String getProcessEngineName() {
    return processEngineName;
  }
  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }
  public File getFile() {
    return file;
  }
  public void setFile(File file) {
    this.file = file;
  }
  public List<FileSet> getFileSets() {
    return fileSets;
  }
  public void setFileSets(List<FileSet> fileSets) {
    this.fileSets = fileSets;
  }
}

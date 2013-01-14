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
package com.camunda.fox.platform.impl.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.variable.ByteArrayType;
import org.activiti.engine.impl.variable.SerializableType;
import org.activiti.engine.impl.variable.ValueFields;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.application.spi.ProcessApplicationReference;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;

import com.camunda.fox.platform.impl.context.spi.ProcessArchiveServices;

/**
 * This allows using the right classloader for deserializing process variables
 * 
 * @author Daniel Meyer
 */
public class CmpeSerializableType extends SerializableType {
    
  private final ProcessArchiveServices processArchiveServices;

  public CmpeSerializableType(ProcessArchiveServices processArchiveServices) {
    this.processArchiveServices = processArchiveServices;
  }
  
  // TODO move some of this to activiti
  @Override
  public Object getValue(final ValueFields valueFields) {   
    
    final ClassLoader paClassLoader = getClassLoader(valueFields);    
    
    Object cachedObject = valueFields.getCachedValue();    
    if (cachedObject!=null) {
      return cachedObject;
    }
    byte[] bytes = (byte[]) new ByteArrayType().getValue(valueFields);
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    Object deserializedObject;
    try {
      ObjectInputStream ois = getObjectInputStream(paClassLoader, bais);
      deserializedObject = ois.readObject();
      valueFields.setCachedValue(deserializedObject);
      
      if (valueFields instanceof VariableInstanceEntity) {
        Context
          .getCommandContext()
          .getDbSqlSession()
          .addDeserializedObject(deserializedObject, bytes, (VariableInstanceEntity) valueFields);
      }
      
    } catch (Exception e) {
      throw new ActivitiException("coudn't deserialize object in variable '"+valueFields.getName()+"'", e);
    } finally {
      IoUtil.closeSilently(bais);
    }
    return deserializedObject;
    
  }


  protected ObjectInputStream getObjectInputStream(final ClassLoader paClassLoader, ByteArrayInputStream bais) throws IOException {
    return new ObjectInputStream(bais) {
      protected java.lang.Class<?> resolveClass(java.io.ObjectStreamClass desc) throws java.io.IOException ,ClassNotFoundException {
       try {
         return super.resolveClass(desc);
       } catch (Exception e) {
         String name = desc.getName();
         return paClassLoader.loadClass(name);
      }
      }
    };
  }

  protected ClassLoader getClassLoader(ValueFields valueFields) {
    
    ProcessEngineConfigurationImpl processEngineConfiguration = processArchiveServices.getProcessEngineController().getProcessEngineConfiguration();
    ProcessApplicationManager processApplicationManager = processEngineConfiguration.getProcessApplicationManager();

    ProcessApplicationReference processApplicationReference = null;
    
    if (valueFields instanceof VariableInstanceEntity) {
      String executionId = ((VariableInstanceEntity)valueFields).getExecutionId();
      
      ExecutionEntity executionEntity = Context.getCommandContext()
              .getExecutionManager()
              .findExecutionById(executionId);
      
      processApplicationReference = processApplicationManager.getProcessApplicationForDeployment(executionEntity.getProcessDefinition().getDeploymentId());
    } else if (valueFields instanceof HistoricVariableInstance) {
      HistoricVariableInstance historicVar = (HistoricVariableInstance) valueFields;
      
      String processInstanceId = historicVar.getProcessInstanceId();
      String processDefId = processEngineConfiguration.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();
      String deploymentId = processEngineConfiguration.getRepositoryService().createProcessDefinitionQuery().processDefinitionId(processDefId).singleResult().getDeploymentId();
      
      processApplicationReference = processApplicationManager.getProcessApplicationForDeployment(deploymentId);
    }
          
    if(processApplicationReference == null) {
     
      // use this classloader
      return getClass().getClassLoader();
      
    } else {
    
      try {
        return processApplicationReference.getProcessApplication().getProcessApplicationClassloader();
      } catch (ProcessApplicationUnavailableException e) {
        throw new ActivitiException("Process application with name '"+processApplicationReference.getName()+"' unavailable ", e);
      }
      
    }
  }

}

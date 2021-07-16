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
package org.camunda.bpm.quarkus.engine.extension.deployment.impl;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.cdi.ProcessVariables;
import org.camunda.bpm.engine.cdi.compat.CamundaTaskForm;
import org.camunda.bpm.engine.cdi.compat.FoxTaskForm;
import org.camunda.bpm.engine.cdi.impl.ProcessVariableLocalMap;
import org.camunda.bpm.engine.cdi.impl.ProcessVariableMap;
import org.camunda.bpm.engine.cdi.impl.context.DefaultContextAssociationManager;
import org.camunda.bpm.engine.cdi.jsf.TaskForm;
import org.camunda.bpm.quarkus.engine.extension.impl.CamundaEngineRecorder;
import org.jboss.jandex.DotName;

public class CamundaEngineProcessor {

  protected static final String FEATURE = "camunda-platform-engine";

  @BuildStep
  protected FeatureBuildItem feature() {
    return new FeatureBuildItem(FEATURE);
  }

  @BuildStep
  protected void unremovableBeans(BuildProducer<UnremovableBeanBuildItem> unremovableBeansProducer) {
    unremovableBeansProducer.produce(
        UnremovableBeanBuildItem.beanTypes(
            DotName.createSimple(DefaultContextAssociationManager.class.getName() + "$RequestScopedAssociation")));
  }

  @BuildStep
  protected void additionalBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeansProducer) {
    additionalBeansProducer.produce(
        AdditionalBeanBuildItem.builder()
            .setDefaultScope(DotNames.APPLICATION_SCOPED)
            .addBeanClasses(
                DefaultContextAssociationManager.class,
                BusinessProcess.class,
                ProcessVariableLocalMap.class,
                ProcessVariables.class,
                ProcessVariableMap.class,
                CamundaTaskForm.class,
                FoxTaskForm.class,
                TaskForm.class
            ).build());
  }

  @BuildStep
  @Record(RUNTIME_INIT)
  protected ProcessEngineBuildItem processEngine(CamundaEngineRecorder recorder,
                                                 BeanContainerBuildItem beanContainerBuildItem) {

    return new ProcessEngineBuildItem(recorder.createProcessEngine(beanContainerBuildItem.getValue()));
  }

  @BuildStep
  @Record(RUNTIME_INIT)
  void shutdown(CamundaEngineRecorder recorder,
                ProcessEngineBuildItem processEngine,
                ShutdownContextBuildItem shutdownContext) {

    recorder.registerShutdownTask(shutdownContext, processEngine.getProcessEngine());
  }
}

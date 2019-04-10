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
package org.camunda.bpm.engine.spring.test.application;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.spring.application.SpringProcessApplication;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class PostDeployWithNestedContext extends SpringProcessApplication {

  public class MyEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    public MyEvent(Object source) {
      super(source);
    }
  }

  boolean deployCalled = false;
  boolean triggered = false;
  boolean deployOnChildRefresh;
  
  @Override
  public void setApplicationContext(ApplicationContext mainContext) throws BeansException {
    super.setApplicationContext(mainContext);

    AnnotationConfigApplicationContext nestedContext = new AnnotationConfigApplicationContext();
    nestedContext.setParent(mainContext);
    
    deployCalled = false;
    nestedContext.refresh();
    deployOnChildRefresh = deployCalled;

    ((AbstractApplicationContext) mainContext).addApplicationListener(new ApplicationListener<MyEvent>() {

      @Override
      public void onApplicationEvent(MyEvent event) {
        triggered = true;
      }
    });
  }

  @PostDeploy
  public void registerProcessApplication(ProcessEngine processEngine) {
    deployCalled = true;
    applicationContext.publishEvent(new MyEvent(this));
  }
  
  public boolean isDeployOnChildRefresh() {
    return deployOnChildRefresh;
  }

  public boolean isLateEventTriggered() {
    return triggered;
  }

}

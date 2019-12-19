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
package org.camunda.bpm.qa.performance.engine.steps;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestRunContext;

public class WorkExternalTaskStep extends ProcessEngineAwareStep {

  protected String topic;
  protected String worker;
  protected int n;
  
  public WorkExternalTaskStep(ProcessEngine processEngine, String topic, String worker, int n) {
    super(processEngine);
    this.topic = topic;
    this.worker = worker;
    this.n = n;
  }

  @Override
  public void execute(PerfTestRunContext context) {
	  List<LockedExternalTask> tasks = processEngine.getExternalTaskService()
			  .fetchAndLock(n, worker).topic(topic, 1000L)
			  .execute();
      
	  for (LockedExternalTask task : tasks) {
		  try {
	    	processEngine.getExternalTaskService().complete(task.getId(), worker);
		  }catch(Exception e) {
		    e.printStackTrace();
		  }
	  }
  }

}

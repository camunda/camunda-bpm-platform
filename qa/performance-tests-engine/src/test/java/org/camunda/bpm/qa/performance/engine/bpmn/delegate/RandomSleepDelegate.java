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
package org.camunda.bpm.qa.performance.engine.bpmn.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Random;

/**
 * @author: Johannes Heinemann
 */
public class RandomSleepDelegate implements JavaDelegate {

  Random rand;

  public RandomSleepDelegate() {
    rand = new Random();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    Thread.sleep(getSleepTimeInMilliSec());
  }

  protected long getSleepTimeInMilliSec() {
    return getRandInt(50, 500);
  }

  protected int getRandInt(int min, int max){
    return rand.nextInt((max - min) + 1) + min;
  }
}

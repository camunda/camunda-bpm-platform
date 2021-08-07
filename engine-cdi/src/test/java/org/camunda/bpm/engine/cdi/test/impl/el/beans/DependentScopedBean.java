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
package org.camunda.bpm.engine.cdi.test.impl.el.beans;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Named;

/**
 * @author Daniel Meyer
 *
 */
@Named
@Dependent
public class DependentScopedBean {

  public static List<String> lifecycle = new ArrayList<String>();

  public void invoke() {
    lifecycle.add("bean-invoked");
  }

  public static void reset() {
    lifecycle.clear();
  }

  @PreDestroy
  public void preDestroy() {
    lifecycle.add("pre-destroy-invoked");
  }

  @PostConstruct
  public void postContruct() {
    lifecycle.add("post-construct-invoked");
  }

}

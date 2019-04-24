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
package org.camunda.bpm.engine.task;

import org.camunda.bpm.engine.TaskService;


/**
 * Contains constants for all types of identity links that can be used to involve a 
 * user or group with a certain task. 
 * 
 * @see TaskService#addUserIdentityLink(String, String, String)
 * @see TaskService#addGroupIdentityLink(String, String, String)
 * 
 * @author Joram Barrez
 */
public class IdentityLinkType {
  
  /* Activiti native roles */
  
  public static final String ASSIGNEE = "assignee";
  
  public static final String CANDIDATE = "candidate";

  public static final String OWNER = "owner";

}

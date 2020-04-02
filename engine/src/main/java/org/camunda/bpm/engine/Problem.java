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
package org.camunda.bpm.engine;

import java.util.List;

/**
 * Interface of a problem occurred during parsing
 */
public interface Problem {

  /** The message of this problem */
  String getMessage();

  /** The line where the problem occurs */
  int getLine();

  /** The column where the problem occurs */
  int getColumn();

  /**
   * The id of the main element causing the problem. It can be
   * <code>null</code> in case the element doesn't have an id.
   */
  String getMainElementId();

  /**
   * The ids of all involved elements in the problem. It can be an empty
   * list in case the elements do not have assigned ids.
   */
  List<String> getElementIds();

}
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
package org.camunda.bpm.engine.impl.core.handler;

import org.camunda.bpm.engine.impl.core.model.CoreActivity;

/**
 * <p>An implementation of this context should contain necessary
 * information to be accessed by a {@link ModelElementHandler}.</p>
 *
 * @author Roman Smirnov
 *
 */
public interface HandlerContext {

  /**
   * <p>This method returns an {@link CoreActivity activity}. The
   * returned activity represents a parent activity, which can
   * contain {@link CoreActivity activities}.</p>
   *
   * <p>The returned activity should be used as a parent activity
   * for a new {@link CoreActivity activity}.
   *
   * @return a {@link CoreActivity}
   */
  CoreActivity getParent();

}

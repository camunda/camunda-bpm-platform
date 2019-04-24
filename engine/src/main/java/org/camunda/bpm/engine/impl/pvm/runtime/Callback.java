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
package org.camunda.bpm.engine.impl.pvm.runtime;

/**
 * Represents a callback which should be called after work was done. This interface is similar to the
 * {@link java.util.concurrent.Callable} interface, with the exception that the callback method does not throw any
 * catching exception. Without this restriction the caller does not have to catch any exception.
 *
 * @param <P> the type of the callback parameter
 * @param <R> the type of the callback result
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public interface Callback<P, R> {

  /**
   * The callback which should be called/executed after work was done.
   *
   * @param param the parameter for the callback
   * @return the result of the callback
   */
  R callback(P param);
}

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.history.handler;

import java.util.List;

/**
 * A {@link CompositeHistoryEventHandler} implementation which additionally adds
 * to the list of {@link HistoryEventHandler} the {@link DbHistoryEventHandler}
 * which persists events to a database.
 * 
 * @author Alexander Tyatenkov
 * 
 */
public class CompositeDbHistoryEventHandler extends CompositeHistoryEventHandler {

  /**
   * Non-argument constructor that adds {@link DbHistoryEventHandler} to the
   * list of {@link HistoryEventHandler}.
   */
  public CompositeDbHistoryEventHandler() {
    super();
    addDefaultDbHistoryEventHandler();
  }

  /**
   * Constructor that takes a varargs parameter {@link HistoryEventHandler} that
   * consume the event and adds {@link DbHistoryEventHandler} to the list of
   * {@link HistoryEventHandler}.
   * 
   * @param historyEventHandlers
   *          the list of {@link HistoryEventHandler} that consume the event.
   */
  public CompositeDbHistoryEventHandler(final HistoryEventHandler... historyEventHandlers) {
    super(historyEventHandlers);
    addDefaultDbHistoryEventHandler();
  }

  /**
   * Constructor that takes a list of {@link HistoryEventHandler} that consume
   * the event and adds {@link DbHistoryEventHandler} to the list of
   * {@link HistoryEventHandler}.
   * 
   * @param historyEventHandlers
   *          the list of {@link HistoryEventHandler} that consume the event.
   */
  public CompositeDbHistoryEventHandler(final List<HistoryEventHandler> historyEventHandlers) {
    super(historyEventHandlers);
    addDefaultDbHistoryEventHandler();
  }

  /**
   * Add {@link DbHistoryEventHandler} to the list of
   * {@link HistoryEventHandler}.
   */
  private void addDefaultDbHistoryEventHandler() {
    historyEventHandlers.add(new DbHistoryEventHandler());
  }

}

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
package org.camunda.bpm.engine.externaltask;

import java.util.List;

/**
 * @author Thorben Lindhauer
 *
 */
public interface ExternalTaskQueryBuilder {

  /**
   * Specifies that tasks of a topic should be fetched and locked for
   * a certain amount of time
   *
   * @param topicName the name of the topic
   * @param lockDuration the duration in milliseconds for which tasks should be locked;
   *   begins at the time of fetching
   * @return
   */
  public ExternalTaskQueryTopicBuilder topic(String topicName, long lockDuration);

  /**
   * Performs the fetching. Locks candidate tasks of the given topics
   * for the specified duration.
   *
   * @return fetched external tasks that match the topic and that can be
   *   successfully locked
   */
  List<LockedExternalTask> execute();
}

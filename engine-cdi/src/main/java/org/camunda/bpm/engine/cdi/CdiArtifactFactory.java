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
package org.camunda.bpm.engine.cdi;

import org.camunda.bpm.engine.ArtifactFactory;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.engine.impl.DefaultArtifactFactory;

/**
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class CdiArtifactFactory implements ArtifactFactory {

  private ArtifactFactory defaultArtifactFactory = new DefaultArtifactFactory();

  @Override
  public <T> T getArtifact(Class<T> clazz) {
    T instance = ProgrammaticBeanLookup.lookup(clazz, true);

    if (instance == null) {
      // fall back to using newInstance()
      instance = defaultArtifactFactory.getArtifact(clazz);
    }

    return instance;
  }
}

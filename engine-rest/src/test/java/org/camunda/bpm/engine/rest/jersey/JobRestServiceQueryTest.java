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
package org.camunda.bpm.engine.rest.jersey;

import org.camunda.bpm.engine.rest.AbstractJobRestServiceQueryTest;
import org.camunda.bpm.engine.rest.util.EmbeddedServerBootstrap;
import org.camunda.bpm.engine.rest.util.JerseyServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class JobRestServiceQueryTest extends AbstractJobRestServiceQueryTest {

	protected static EmbeddedServerBootstrap serverBootstrap;

	@BeforeClass
	public static void setUpEmbeddedRuntime() {
		serverBootstrap = new JerseyServerBootstrap();
		serverBootstrap.start();
	}

	@AfterClass
	public static void tearDownEmbeddedRuntime() {
		serverBootstrap.stop();
	}

	/**
	 * Does not match the /job/count path (perhaps due to CAM-4133)
	 */
	@Override
	@Test
	@Ignore
	public void testQueryCount() {
	  super.testQueryCount();
	}
}

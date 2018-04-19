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
package org.camunda.bpm.client.rule;

import static org.camunda.bpm.client.util.TestUtil.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.junit.rules.ExternalResource;

public class ClientRule extends ExternalResource {

  public static final long LOCK_DURATION = 1000 * 60 * 5;
  public static final String BASE_URL = "http://localhost:48080/engine-rest";

  protected ExternalTaskClientBuilder builder;
  protected ExternalTaskClient client;

  public ClientRule() {
    this(() -> ExternalTaskClient.create()
      .baseUrl(BASE_URL)
      .lockDuration(LOCK_DURATION));
  }

  public ClientRule(Supplier<ExternalTaskClientBuilder> builderSupplier) {
    this.builder = builderSupplier.get();
  }

  public void before() {
    builder.disableAutoFetching();
    client = builder.build();
  }

  public void after() {
    client.stop();
    client = null;
  }

  public ExternalTaskClient client() {
    return client;
  }

  public void waitForFetchAndLockUntil(BooleanSupplier condition) {
    client.start();

    try {
      waitUntil(condition);
    }
    finally {
      client.stop();
    }

  }
}

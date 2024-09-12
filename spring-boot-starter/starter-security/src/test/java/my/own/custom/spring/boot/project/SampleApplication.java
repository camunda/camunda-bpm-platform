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
package my.own.custom.spring.boot.project;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@SpringBootApplication
public class SampleApplication {

  public static void main(String... args) {
    SpringApplication.run(SampleApplication.class, args);
  }

  @Bean
  public TestRestTemplate restTemplate(RestTemplateBuilder builder) {
    builder.requestFactory(() -> {
      var factory = new HttpComponentsClientHttpRequestFactory();
      var httpClient = HttpClientBuilder.create().disableRedirectHandling().build();
      factory.setHttpClient(httpClient);
      return factory;
    });
    return new TestRestTemplate(builder);
  }

}

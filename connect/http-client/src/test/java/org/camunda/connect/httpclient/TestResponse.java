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
package org.camunda.connect.httpclient;

import java.io.IOException;

import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;

public class TestResponse extends BasicHttpResponse implements CloseableHttpResponse {

  public TestResponse() {
    this(HttpVersion.HTTP_1_1, 200, "OK");
  }

  public TestResponse(ProtocolVersion ver, int code, String reason) {
    super(ver, code, reason);
  }

  public TestResponse statusCode(int statusCode) {
    setStatusCode(statusCode);
    return this;
  }

  public TestResponse header(String field, String value) {
    setHeader(field, value);
    return this;
  }

  public TestResponse payload(String payload) {
    return payload(payload, ContentType.TEXT_PLAIN);
  }

  public TestResponse payload(String payload, ContentType contentType) {
    if (payload != null) {
      setEntity(new StringEntity(payload, contentType));
    }
    else {
      setEntity(null);
    }
    return this;
  }

  public void close() throws IOException {

  }

}

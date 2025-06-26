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

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHttpResponse;



public class TestResponse extends BasicHttpResponse implements ClassicHttpResponse {

  private HttpEntity entity;

  public TestResponse() {
    this(200, "OK");
  }

  public TestResponse(int code, String reason) {
    super(code, reason);
  }

  public TestResponse code(int code) {
    setCode(code);
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
    } else {
      setEntity(null);
    }
    return this;
  }

  @Override
  public HttpEntity getEntity() {
    return entity;
  }

  @Override
  public void setEntity(HttpEntity entity) {
    this.entity = entity;
  }

  @Override
  public void close() {
    /* NOP */
  }
}

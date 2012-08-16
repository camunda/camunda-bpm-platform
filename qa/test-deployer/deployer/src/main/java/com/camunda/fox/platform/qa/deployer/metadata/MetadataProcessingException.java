/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.qa.deployer.metadata;

public class MetadataProcessingException extends RuntimeException {

  private static final long serialVersionUID = 7612693914215308357L;

  public MetadataProcessingException() {
    super();
  }

  public MetadataProcessingException(String message, Throwable cause) {
    super(message, cause);
  }

  public MetadataProcessingException(String message) {
    super(message);
  }

  public MetadataProcessingException(Throwable cause) {
    super(cause);
  }
}

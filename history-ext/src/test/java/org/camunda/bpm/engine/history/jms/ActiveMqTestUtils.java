/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.history.jms;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * @author Oleg Zhurakousky
 * @author Gunnar Hillert
 * 
 */
public class ActiveMqTestUtils {

  private static final Logger LOGGER = Logger.getLogger(ActiveMqTestUtils.class);

  public static void prepare() {
    LOGGER.info("Refreshing ActiveMQ data directory.");
    File activeMqTempDir = new File("activemq-data");
    deleteDir(activeMqTempDir);
  }

  private static void deleteDir(File directory) {
    if (directory.exists()) {
      String[] children = directory.list();
      if (children != null) {
        for (int i = 0; i < children.length; i++) {

          deleteDir(new File(directory, children[i]));
        }
      }
    }
    directory.delete();
  }
}

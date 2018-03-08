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
package org.camunda.bpm.engine.rest.impl;

import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.spi.FetchAndLockHandler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Tassilo Weidner
 */
public class FetchAndLockContextListener implements ServletContextListener {

  protected static FetchAndLockHandler fetchAndLockHandler;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    if (fetchAndLockHandler == null) {
      fetchAndLockHandler = lookupFetchAndLockHandler();
      fetchAndLockHandler.start();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    fetchAndLockHandler.shutdown();
  }

  public static FetchAndLockHandler getFetchAndLockHandler() {
    return fetchAndLockHandler;
  }

  protected FetchAndLockHandler lookupFetchAndLockHandler() {
    ServiceLoader<FetchAndLockHandler> serviceLoader = ServiceLoader.load(FetchAndLockHandler.class);
    Iterator<FetchAndLockHandler> iterator = serviceLoader.iterator();
    if(iterator.hasNext()) {
      return iterator.next();
    } else {
      throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,
        "Could not find an implementation of the " + FetchAndLockHandler.class.getSimpleName() + "- SPI");
    }
  }

}

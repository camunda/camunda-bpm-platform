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

// observe document if logout link is rendered and override it for SSO logout
const observer = new MutationObserver(() => {
  const logoutListItem = document.querySelector("li.account li.logout");
  if (logoutListItem) {
    observer.disconnect();

    const oldLogoutLink = logoutListItem.getElementsByTagName("a")[0];
    // create a clone so no listeners are attached anymore
    const logoutLink = oldLogoutLink.cloneNode(true);
    logoutListItem.replaceChild(logoutLink, oldLogoutLink);

    // find out the base url, i.e. the part until app-root
    const appRoot = document.querySelector("base").getAttribute("app-root");
    const idx = document.location.href.indexOf(appRoot);
    const baseUrl = document.location.href.substring(0, idx);
    
    logoutLink.href = baseUrl + "/logout";
  }
});
observer.observe(document, { attributes: false, childList: true, characterData: false, subtree: true });

// on page load forward user from login page to dashboard
if (window.location.hash === '#/login') {
  window.location.hash = "";
}
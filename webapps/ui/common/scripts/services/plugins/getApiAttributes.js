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

function getCSRFToken(CSRFCookieName) {
  return document.cookie.replace(
    new RegExp(`(?:(?:^|.*;*)${CSRFCookieName}*=*([^;]*).*$)|^.*$`),
    '$1'
  );
}

module.exports = function(params, CSRFCookieName = 'XSRF-TOKEN', appName) {
  const base = document.querySelector('base');
  const regex = new RegExp(`.*${appName}\/([^/]*).*`);
  const engine = window.location.href.replace(regex, '$1');

  return {
    api: {
      adminApi: base.getAttribute('admin-api').slice(0, -1),
      baseApi: base.getAttribute('engine-api').slice(0, -1),
      cockpitApi: base.getAttribute('cockpit-api').slice(0, -1),
      tasklistApi: base.getAttribute('tasklist-api').slice(0, -1),
      engineApi: base.getAttribute('engine-api') + 'engine/' + engine,
      engine,
      CSRFToken: getCSRFToken(CSRFCookieName)
    },
    ...params
  };
};

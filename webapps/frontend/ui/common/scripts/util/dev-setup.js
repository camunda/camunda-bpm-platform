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

const $ = require('jquery');

export const setupDev = () => {
  const base = $('base');
  const href = base.attr('href');
  const engine = window.location.href.match(/\/app\/[a-z]+\/([\w-]+)(|\/)/)[1];
  const newPath = href.replace('{ENGINE}', engine);
  base.attr('href', newPath);

  if (!window.location.href.includes(`/camunda/app/admin/${engine}/setup/`)) {
    fetch(`/camunda/api/admin/setup/${engine}/user/create`, {
      method: 'POST',
      body: JSON.stringify({}),
      headers: {
        'X-XSRF-TOKEN': document.cookie.replace('XSRF-TOKEN=', ''),
        'Content-Type': 'application/json'
      }
    }).then(response => {
      response
        .json()
        .then(json => {
          if (json.message !== 'Setup action not available') {
            window.location.href = `/camunda/app/admin/${engine}/setup/#setup`;
          }
        })
        .catch(() => {});
    });
  }
};

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

'use strict';

module.exports = function(ngModule, config) {
  ngModule.config([
    'UriProvider',
    function(UriProvider) {
      UriProvider.replace(':appRoot', config['app-root']);
      UriProvider.replace(':appName', 'tasklist');
      UriProvider.replace('app://', config.href);
      UriProvider.replace('adminbase://', config['app-root'] + '/app/admin/');
      UriProvider.replace(
        'tasklistbase://',
        config['app-root'] + '/app/tasklist/'
      );
      UriProvider.replace(
        'cockpitbase://',
        config['app-root'] + '/app/cockpit/'
      );
      UriProvider.replace('admin://', config['admin-api']);
      UriProvider.replace('plugin://', config['tasklist-api'] + 'plugin/');
      UriProvider.replace('engine://', config['engine-api']);

      UriProvider.replace(':engine', [
        '$window',
        function($window) {
          var uri = $window.location.href;

          var match = uri.match(/\/app\/tasklist\/([\w-]+)(|\/)/);
          if (match) {
            return match[1];
          } else {
            throw new Error('no process engine selected');
          }
        }
      ]);
    }
  ]);
};

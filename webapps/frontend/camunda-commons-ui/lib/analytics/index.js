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

var angular = require('angular');

var modalTemplate = require('./modal.html?raw');

// CAMUNDA_VERSION has a structure of 'major.minor.patch[-SNAPSHOT]', but we only need 'major.minor' for doc links
var camundaVersion = (CAMUNDA_VERSION || '').match(/([0-9]+.[0-9]+)/); // eslint-disable-line
camundaVersion = camundaVersion ? camundaVersion[0] : 'latest'; // if 'latest' is chosen, something went wrong

var modalController = [
  '$scope',
  '$sce',
  'Notifications',
  'telemetryResource',
  '$translate',
  function(scope, $sce, Notifications, telemetryResource, $translate) {
    scope.camundaVersion = camundaVersion;
    scope.loadingState = 'INITIAL';
    scope.form = {enableUsage: false};

    scope.close = function() {
      scope.$dismiss();
    };
    scope.save = function() {
      scope.loadingState = 'LOADING';
      telemetryResource.configure(!!scope.form.enableUsage, function(err) {
        if (!err) {
          scope.loadingState = 'DONE';
        } else {
          scope.loadingState = 'ERROR';
          Notifications.addError({
            status: $translate.instant('TELEMETRY_ERROR_STATUS'),
            message: $translate.instant('TELEMETRY_ERROR_MESSAGE')
          });
        }
      });
    };
  }
];

module.exports = angular
  .module('cam.commons.analytics', [])

  // Open Modal for Telemetry
  .run([
    '$rootScope',
    'camAPI',
    '$uibModal',
    function($rootScope, camAPI, $modal) {
      var telemetryResource = camAPI.resource('telemetry');

      $rootScope.$on('authentication.login.success', function(event) {
        // skip if default got prevented
        if (!event.defaultPrevented) {
          telemetryResource
            .get()
            .then(res => {
              if (res.enableTelemetry === null) {
                $modal.open({
                  template: modalTemplate,
                  controller: modalController,
                  size: 'md',
                  resolve: {
                    telemetryResource: function() {
                      return telemetryResource;
                    }
                  }
                });
              }
            })
            .catch(() => {});
        }
      });
    }
  ]);

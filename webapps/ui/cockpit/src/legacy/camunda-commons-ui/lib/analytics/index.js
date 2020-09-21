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

var angular = require("angular");

var modalTemplate = require("./modal.html");
var camundaLogo = require("../auth/page/logo.svg");

var previousUrl = "";

// As long as we don't have a global Modal service, this is how we find out if we just logged in.
window.addEventListener("hashchange", event => {
  previousUrl = event.oldURL;
});

var modalController = [
  "$scope",
  "$sce",
  "Notifications",
  "telemetryResource",
  "$translate",
  function(scope, $sce, Notifications, telemetryResource, $translate) {
    scope.loadingState = "INITIAL";
    scope.logo = $sce.trustAsHtml(camundaLogo);
    scope.enableUsage = false;

    scope.page = 1;
    scope.close = function() {
      scope.$dismiss();
    };
    scope.next = function() {
      scope.page++;
    };
    scope.save = function() {
      scope.loadingState = "LOADING";
      telemetryResource.configure(!!scope.enableUsage, function(err) {
        scope.loadingState = "DONE";
        if (!err) {
          scope.page++;
        } else {
          Notifications.addError({
            status: $translate.instant("TELEMETRY_ERROR_STATUS"),
            message: $translate.instant("TELEMETRY_ERROR_MESSAGE")
          });
        }
      });
    };
  }
];

module.exports = angular
  .module("cam.commons.analytics", [])

  // Open Modal for Telemetry
  .run([
    "camAPI",
    "$uibModal",
    function(camAPI, $modal) {
      var telemetryResource = camAPI.resource("telemetry");

      // Only if we logged in just now
      if (previousUrl.includes("login")) {
        telemetryResource
          .get()
          .then(res => {
            if (res.enableTelemetry === null) {
              $modal.open({
                template: modalTemplate,
                controller: modalController,
                size: "lg",
                resolve: {
                  telemetryResource: function() {
                    return telemetryResource;
                  }
                },
                appendTo: angular.element(".angular-app")
              });
            }
          })
          .catch(() => {});
      }
    }
  ]);

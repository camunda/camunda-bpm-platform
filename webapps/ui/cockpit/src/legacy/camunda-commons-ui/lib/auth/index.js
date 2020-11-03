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

var angular = require("../../../camunda-bpm-sdk-js/vendor/angular");

require("angular-route");
require("angular-resource");
require("angular-translate");

var commonsUtil = require("../util/index"),
  authenticationService = require("./service/authenticationService");
const { refreshUser } = require("HOC/withUser");

/**
 * @module cam.commons.auth
 */

/**
 * @memberof cam.commons
 */

var ngModule = angular.module("cam.commons.auth", [
  angular.module("ngRoute").name,
  commonsUtil.name,
  "pascalprecht.translate"
]);

ngModule
  // notification integration
  .run([
    "$rootScope",
    "Notifications",
    "$translate",
    "shouldDisplayAuthenticationError",
    "$location",
    function(
      $rootScope,
      Notifications,
      $translate,
      shouldDisplayAuthenticationError,
      $location
    ) {
      let redirecting = false;
      const handleLoginRequired = () => {
        if (redirecting) return;

        // Only refresh the user and redirect once
        redirecting = true;
        refreshUser().then(() => {
          $location.url("/login");
        });

        if (shouldDisplayAuthenticationError()) {
          Notifications.addError({
            status: $translate.instant("AUTH_FAILED_TO_DISPLAY_RESOURCE"),
            message: $translate.instant("AUTH_AUTHENTICATION_FAILED"),
            http: true,
            exclusive: ["http"]
          });
        }
      };

      $rootScope.$on("authentication.login.required", handleLoginRequired);
    }
  ])

  // ensure AuthenticationService is bootstraped
  .run(["AuthenticationService", function() {}])

  .service("AuthenticationService", authenticationService);

module.exports = ngModule;

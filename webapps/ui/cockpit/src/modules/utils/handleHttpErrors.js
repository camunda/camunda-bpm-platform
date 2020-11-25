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

import { addError } from "utils/notifications";
import translate from "utils/translation";

const shouldDisplayAuthenticationError = () =>
  !["#/login", "#/dashboard", "#/welcome", "#/"].includes(window.location.hash);

function handleHttpError(status, data) {
  switch (status) {
    case 500:
      if (data && data.message) {
        addError({
          status: translate("PAGES_STATUS_SERVER_ERROR"),
          message: data.message,
          exceptionType: data.exceptionType
        });
      } else {
        addError({
          status: translate("PAGES_STATUS_SERVER_ERROR"),
          message: translate("PAGES_MSG_SERVER_ERROR")
        });
      }
      break;

    case 0:
      addError({
        status: translate("PAGES_STATUS_REQUEST_TIMEOUT"),
        message: translate("PAGES_MSG_REQUEST_TIMEOUT")
      });
      break;

    case 401:
      if (shouldDisplayAuthenticationError()) {
        addError({
          status: translate("AUTH_FAILED_TO_DISPLAY_RESOURCE"),
          message: translate("AUTH_AUTHENTICATION_FAILED")
        });
      }
      break;

    case 403:
      if (data.type === "AuthorizationException") {
        var message;
        if (data.resourceId) {
          message = translate("PAGES_MSG_ACCESS_DENIED_RESOURCE_ID", {
            permissionName: data.permissionName.toLowerCase(),
            resourceName: data.resourceName.toLowerCase(),
            resourceId: data.resourceId
          });
        } else {
          var missingAuths = data.missingAuthorizations.map(function(
            missingAuth
          ) {
            return (
              "'" +
              missingAuth.permissionName +
              "'" +
              " " +
              missingAuth.resourceName +
              "s"
            );
          });

          message = translate("PAGES_MSG_ACCESS_DENIED", {
            missingAuths: missingAuths.join()
          });
        }

        addError({
          status: translate("PAGES_STATUS_ACCESS_DENIED"),
          message: message
        });
      } else {
        addError({
          status: translate("PAGES_STATUS_ACCESS_DENIED"),
          message: translate("PAGES_MSG_ACTION_DENIED")
        });
      }
      break;

    case 404:
      if (shouldDisplayAuthenticationError()) {
        addError({
          status: translate("PAGES_STATUS_NOT_FOUND"),
          message: translate("PAGES_MSG_NOT_FOUND")
        });
      }
      break;
    default:
      addError({
        status: translate("PAGES_STATUS_COMMUNICATION_ERROR"),
        message: translate("PAGES_MSG_COMMUNICATION_ERROR", {
          status: status
        })
      });
  }
}

window.addEventListener("unhandledrejection", async event => {
  const response = event.reason;
  // make sure it actually is a request
  if (typeof response.status === "number") {
    handleHttpError(response.status, await response.json());
  }
});

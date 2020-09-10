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

const notifications = [];
const consumers = [];

function escapeHtml(html) {
  var text = document.createTextNode(html);
  var div = document.createElement("div");
  div.appendChild(text);
  return div.innerHTML;
}

/**
 *
 * Notification object may specify the following fields:
 *   type: type of the notification (info, warning, danger, success)
 *   status: main status line
 *   message: detail message
 *   unsafe: boolean, indicates that status and message should not be sanitized
 *   duration: time duration in ms the notification should be shown to the user
 *   exclusive: boolean || array of attribute names that notification should be exclusive with
 *   scope: notification will be removed when the specified scope is destroyed
 *
 * @param notification {notification}
 * @returns {undefined}
 */

export function addError(error) {
  if (!error.type) {
    error.type = "danger";
  }

  add(error);
}

export function addMessage(message) {
  if (!message.type) {
    message.type = "info";
  }

  add(message);
}

export function add(notification) {
  const exclusive = notification.exclusive;

  if (!notification.unsafe) {
    notification.status = escapeHtml(notification.status);
    notification.message = escapeHtml(notification.message || "");
  }

  if (exclusive) {
    if (typeof exclusive == "boolean") {
      clearAll();
    } else {
      var filter = {};
      exclusive.forEach(key => {
        filter[key] = notification[key];
      });

      clear(filter);
    }
  }

  notifications.push(notification);

  for (var i = consumers.length - 1, c; (c = consumers[i]); i--) {
    // add to first interested consumer only
    if (c.add(notification)) {
      break;
    }
  }

  if (notification.duration) {
    window.setTimeout(function() {
      if (notification.scope) {
        delete notification.scope;
      }
      clear(notification);
    }, notification.duration);
  }

  if (notification.scope) {
    notification.scope.$on("$destroy", function() {
      // remove the scope from the notification object to resolve circular dependency
      // when clearing the notification
      delete notification.scope;
      clear(notification);
    });
  }
}

export function clear(notification) {
  var removeCandidates = [];

  if (typeof notification == "string") {
    notification = { status: notification };
    removeCandidates = notifications.filter(
      candidate => candidate.status === notification.status
    );
  }

  removeCandidates.push(notification);

  removeCandidates.forEach(e => {
    var idx = notifications.indexOf(e);
    if (idx !== -1) {
      notifications.splice(idx, 1);
    }

    consumers.forEach(consumer => {
      consumer.remove(e);
    });
  });
}

export function clearAll() {
  while (notifications.length) {
    var notification = notifications.pop();
    clear(notification);
  }
}

export function registerConsumer(consumer, index) {
  if (typeof index === "number" && consumers.length > index) {
    consumers.splice(index, 0, consumer);
    return index;
  } else {
    return consumers.push(consumer) - 1;
  }
}

export function unregisterConsumer(consumer) {
  var idx = consumers.indexOf(consumer);

  if (idx !== -1) {
    consumers.splice(idx, 1);
  }
}

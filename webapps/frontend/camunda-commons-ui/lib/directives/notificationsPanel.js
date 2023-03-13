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

require('angular-sanitize');

/**
   * @name notificationsPanel
   * @memberof cam.common.directives
   * @type angular.directive
   * @description Provides a widget for user notifications
   * @example
      TODO
   */
var notificationsTemplate =
  '<div class="notifications">' +
  '  <div uib-alert ng-repeat="notification in notifications" class="alert" ng-class="notificationClass(notification)">' +
  '    <button type="button" class="close" ng-click="removeNotification(notification)">&times;</button>' +
  '    <strong class="status" ng-bind-html="trustHTML(notification.status)"></strong> ' +
  '    <strong ng-if="notification.message">:</strong>' +
  '    <span class="message" ng-bind-html="trustHTML(notification.message)"></span>' +
  '  </div>' +
  '</div>';

module.exports = [
  'Notifications',
  '$filter',
  '$sce',
  function(Notifications, $filter, $sce) {
    return {
      restrict: 'EA',
      scope: {
        filter: '=notificationsFilter'
      },
      template: notificationsTemplate,
      link: function(scope) {
        var filter = scope.filter;

        function matchesFilter(notification) {
          if (!filter) {
            return true;
          }

          return !!$filter('filter')([notification], filter).length;
        }

        var notifications = (scope.notifications = []);

        var consumer = {
          add: function(notification) {
            if (matchesFilter(notification)) {
              notifications.push(notification);
              return true;
            } else {
              return false;
            }
          },
          remove: function(notification) {
            var idx = notifications.indexOf(notification);
            if (idx != -1) {
              notifications.splice(idx, 1);
            }
          }
        };

        Notifications.registerConsumer(consumer);

        scope.removeNotification = function(notification) {
          notifications.splice(notifications.indexOf(notification), 1);
        };

        scope.notificationClass = function(notification) {
          var classes = ['danger', 'error', 'success', 'warning', 'info'];

          var type = 'info';

          if (classes.indexOf(notification.type) > -1) {
            type = notification.type;
          }

          return 'alert-' + type;
        };

        scope.trustHTML = function(msg) {
          return $sce.trustAsHtml(msg);
        };

        scope.$on('$destroy', function() {
          Notifications.unregisterConsumer(consumer);
        });
      }
    };
  }
];

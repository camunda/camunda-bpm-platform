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

module.exports = [
  '$parse',
  '$rootScope',
  function($parse, $rootScope) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        var callbackFunc;

        attrs.$observe('isSidebarCollapsed', function(attribute) {
          callbackFunc = $parse(attribute);
          notififyCallback();
        });

        var removeRestoreListner = $rootScope.$on('restore', notififyCallback);
        var removeMaximizeListener = $rootScope.$on(
          'maximize',
          notififyCallback
        );
        var removeResizeListener = $rootScope.$on('resize', notififyCallback);

        scope.$on('$destroy', function() {
          removeRestoreListner();
          removeMaximizeListener();
          removeResizeListener();
        });

        function notififyCallback() {
          var collapsed = isCollapsed();

          if (callbackFunc && typeof callbackFunc === 'function') {
            callbackFunc(scope, {
              collapsed: collapsed
            });
          }
        }

        function isCollapsed() {
          return element.hasClass('collapsed');
        }
      }
    };
  }
];

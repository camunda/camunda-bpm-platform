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

var fs = require('fs');

var template = require('./groupCreate.html')();

var Controller = [
  '$scope',
  'page',
  'GroupResource',
  'Notifications',
  '$location',
  '$translate',
  function(
    $scope,
    pageService,
    GroupResource,
    Notifications,
    $location,
    $translate
  ) {
    $scope.$root.showBreadcrumbs = true;

    pageService.titleSet($translate.instant('GROUP_CREATE_NEW_GROUP'));

    pageService.breadcrumbsClear();

    pageService.breadcrumbsAdd([
      {
        label: $translate.instant('GROUP_CREATE_LABEL_GROUP'),
        href: '#/groups'
      },
      {
        label: $translate.instant('GROUP_CREATE_LABEL_NEW_GROUP'),
        href: '#/group-create'
      }
    ]);

    // data model for new group
    $scope.group = {
      id: '',
      name: '',
      type: ''
    };

    $scope.createGroup = function() {
      var group = $scope.group;
      GroupResource.createGroup(group).$promise.then(
        function() {
          Notifications.addMessage({
            type: 'success',
            status: $translate.instant('NOTIFICATIONS_STATUS_SUCCESS'),
            message: $translate.instant('GROUP_CREATE_MESSAGE_SUCCESS', {
              group: group.id
            })
          });
          $location.path('/groups');
        },
        function() {
          Notifications.addError({
            status: $translate.instant('NOTIFICATIONS_STATUS_FAILED'),
            message: $translate.instant('GROUP_CREATE_MESSAGE_ERROR', {
              group: group.id
            })
          });
        }
      );
    };
  }
];

module.exports = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/group-create', {
      template: template,
      controller: Controller,
      authentication: 'required'
    });
  }
];

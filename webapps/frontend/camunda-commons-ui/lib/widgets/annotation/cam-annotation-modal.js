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

var template = require('./cam-annotation-modal.html')();

module.exports = (annotation, readOnly, callback) => {
  return {
    template,

    controller: [
      '$scope',
      'Notifications',
      'configuration',
      'camAPI',
      '$translate',
      function($scope, Notifications, configuration, camAPI, $translate) {
        $scope.readOnly = readOnly;
        $scope.maxAnnotationLength = configuration.getUserOperationLogAnnotationLength();
        $scope.text = annotation || '';
        $scope.dirty = false;
        $scope.valid = true;

        $scope.loadingState = 'INITIAL';

        $scope.updateAnnotation = () => {
          callback($scope.text)
            .then(() => {
              Notifications.addMessage({
                status: $translate.instant('SUCCESS'),
                message: $translate.instant(
                  'PLGN_AUDIT_EDIT_NOTIFICATION_SUCCESS'
                ),
                exclusive: true
              });
              $scope.dirty = false;
            })
            .catch(err => {
              Notifications.addError({
                status: $translate.instant('ERROR'),
                message:
                  $translate.instant('PLGN_AUDIT_EDIT_NOTIFICATION_FAILURE') +
                  err,
                exclusive: true
              });
              $scope.text = annotation;
            })
            .finally(() => {
              $scope.loadingState = 'DONE';
            });
        };
      }
    ]
  };
};

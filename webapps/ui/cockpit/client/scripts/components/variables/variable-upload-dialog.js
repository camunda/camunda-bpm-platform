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

var template = fs.readFileSync(
  __dirname + '/variable-upload-dialog.html',
  'utf8'
);

var Controller = [
  '$uibModalInstance',
  '$scope',
  'Notifications',
  'Uri',
  'basePath',
  'variable',
  '$translate',
  '$cookies',
  function(
    $modalInstance,
    $scope,
    Notifications,
    Uri,
    basePath,
    variable,
    $translate,
    $cookies
  ) {
    var BEFORE_UPLOAD = 'beforeUpload',
      PERFORM_UPLOAD = 'performUpload',
      UPLOAD_SUCCESS = 'uploadSuccess',
      UPLOAD_FAILED = 'uploadFailed';

    $scope.status = BEFORE_UPLOAD;

    $scope.variable = variable;

    var file;

    $scope.$on('$routeChangeStart', function() {
      $modalInstance.dismiss();
    });

    $scope.upload = function() {
      // progress listeners

      function uploadProgress(evt) {
        $scope.$apply(function() {
          $scope.status = PERFORM_UPLOAD;
          if (evt.lengthComputable) {
            $scope.progress = Math.round((evt.loaded * 100) / evt.total);
          }
        });
      }

      function uploadComplete(xhr) {
        $scope.$apply(function() {
          if (xhr.status === 204) {
            $scope.status = UPLOAD_SUCCESS;
            Notifications.addMessage({
              status: $translate.instant('VARIABLE_UPLOAD_FILE'),
              message: $translate.instant('VARIABLE_UPLOAD_MESSAGE_ADD')
            });
          } else {
            $scope.status = UPLOAD_FAILED;
            Notifications.addError({
              status: $translate.instant('VARIABLE_UPLOAD_FILE'),
              message: $translate.instant('VARIABLE_UPLOAD_MESSAGE_ERR'),
              exclusive: true
            });
          }
        });
      }

      function uploadFailed() {
        $scope.$apply(function() {
          $scope.status = UPLOAD_FAILED;
          Notifications.addError({
            status: $translate.instant('VARIABLE_UPLOAD_FILE'),
            message: $translate.instant('VARIABLE_UPLOAD_MESSAGE_ERR'),
            exclusive: true
          });
        });
      }

      // perform HTML 5 file opload (not supported by IE 9)
      var fd = new FormData();
      fd.append('data', file);
      fd.append('valueType', variable.type);

      var xhr = new XMLHttpRequest();

      xhr.upload.addEventListener('progress', uploadProgress, false);
      xhr.addEventListener(
        'load',
        function() {
          uploadComplete(xhr);
        },
        false
      );
      xhr.addEventListener('error', uploadFailed, false);
      xhr.addEventListener('abort', uploadFailed, false);
      xhr.open('POST', Uri.appUri(basePath + '/data'));

      var token = $cookies.get('XSRF-TOKEN');
      if (token) {
        xhr.setRequestHeader('X-XSRF-TOKEN', token);
      }

      xhr.send(fd);
    };

    $scope.setFile = function(element) {
      file = element.files[0];
    };
  }
];

module.exports = {
  template: template,
  controller: Controller
};

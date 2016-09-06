'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/variable-upload-dialog.html', 'utf8');

var Controller = [
  '$modalInstance',
  '$scope',
  'Notifications',
  'Uri',
  'basePath',
  'variable',
  function(
    $modalInstance,
    $scope,
    Notifications,
    Uri,
    basePath,
    variable
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
            $scope.progress = Math.round(evt.loaded * 100 / evt.total);
          }
        });
      }

      function uploadComplete(xhr) {
        $scope.$apply(function() {

          if(xhr.status === 204) {
            $scope.status = UPLOAD_SUCCESS;
            Notifications.addMessage({
              status: 'File',
              message: 'The file has been uploaded successfully.'
            });
          }
          else {
            $scope.status = UPLOAD_FAILED;
            Notifications.addError({
              status: 'File',
              message: 'The file could not be uploaded successfully.',
              exclusive: true
            });
          }
        });
      }

      function uploadFailed() {
        $scope.$apply(function() {
          $scope.status = UPLOAD_FAILED;
          Notifications.addError({
            status: 'File',
            message: 'The file could not be uploaded successfully.',
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
      xhr.addEventListener('load', function() {
        uploadComplete(xhr);
      }, false);
      xhr.addEventListener('error', uploadFailed, false);
      xhr.addEventListener('abort', uploadFailed, false);
      xhr.open('POST', Uri.appUri(basePath + '/data'));
      xhr.send(fd);

    };

    $scope.setFile = function(element) {
      file = element.files[0];
    };

  }];

module.exports = {
  template: template,
  controller: Controller
};

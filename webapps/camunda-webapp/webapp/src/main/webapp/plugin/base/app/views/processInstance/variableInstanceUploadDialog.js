ngDefine('cockpit.plugin.base.views', function(module, $) {

  var VariableInstanceUpluadController = [ 
    '$scope', 
    '$location', 
    'Notifications',
    'dialog', 
    'Uri',
    'variableInstance',
    function(
      $scope, 
      $location, 
      Notifications,
      dialog, 
      Uri,
      variableInstance) {

    var BEFORE_UPLOAD = 'beforeUpload',
        PERFORM_UPLOAD = 'performUpload',
        UPLOAD_SUCCESS = 'uploadSuccess',
        UPLOAD_FAILED = 'uploadFailed';

    $scope.variableInstance = variableInstance;
    $scope.status = BEFORE_UPLOAD;

    $scope.upload = function () {

      // progress listeners

      function uploadProgress(evt) {
        $scope.$apply(function(){
          $scope.status = PERFORM_UPLOAD;
          if (evt.lengthComputable) {
            $scope.progress = Math.round(evt.loaded * 100 / evt.total)
          }
        });
      }

      function uploadComplete(evt) {
        $scope.$apply(function(){
          $scope.status = UPLOAD_SUCCESS;
          Notifications.addMessage({'status': 'Success', 'message': 'File upload successfull.'});
        });
      }

      function uploadFailed(evt) {
        $scope.$apply(function(){
          $scope.status = UPLOAD_FAILED;          
          Notifications.addError({'status': 'Failed', 'message': 'File upload failed.', 'exclusive': ['type']});
        });
      }

      // perform HTML 5 file opload (not supported by IE 9)
      var fd = new FormData();
      fd.append("data", $scope.file);
      var xhr = new XMLHttpRequest();
      xhr.upload.addEventListener("progress", uploadProgress, false);
      xhr.addEventListener("load", uploadComplete, false);
      xhr.addEventListener("error", uploadFailed, false);
      xhr.addEventListener("abort", uploadFailed, false);
      xhr.open("POST", $scope.getVariableUploadUrl());
      xhr.send(fd);

    }

    $scope.setFile = function(element) {      
      $scope.file = element.files[0];
    };

    $scope.getVariableUploadUrl = function () {
      return Uri.appUri('engine://engine/:engine/execution/'+variableInstance.executionId+'/localVariables/'+variableInstance.name+"/data");
    }

    $scope.$on('$routeChangeStart', function () {
      dialog.close($scope.status);
    });

    $scope.close = function (status) {
      dialog.close(status);
    };
  }];

  module.controller('VariableInstanceUpluadController', VariableInstanceUpluadController);

});

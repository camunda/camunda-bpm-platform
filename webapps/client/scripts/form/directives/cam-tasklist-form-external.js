define([
  'angular',
  'text!./cam-tasklist-form-external.html'
], function(
  angular,
  template
) {
  'use strict';


  return [function() {

    return {

      restrict: 'EAC',

      require: '^camTasklistForm',

      scope: true,

      template: template,

      link : function($scope, $elment, attrs, formController) {

        $scope.$watch('tasklistForm', function (value) {
          if (value) {
            formController.notifyFormInitialized();
          }
        });

        $scope.$watch('options', function (value) {
          if (value) {
            value.showCompleteButton = false;
          }
        });

      }

    };

  }];

});
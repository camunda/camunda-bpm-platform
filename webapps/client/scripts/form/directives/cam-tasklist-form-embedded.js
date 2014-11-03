define([
  'angular',
  'text!./cam-tasklist-form-embedded.html'
], function(
  angular,
  template
) {
  'use strict';


  return [
    'CamForm',
    'camAPI',
  function(
    CamForm,
    camAPI
  ){

    return {

      restrict: 'EAC',

      require: '^camTasklistForm',

      scope: true,

      template: template,

      link : function($scope, $element, attrs, formController) {

        var container = $element.find('.form-container');
        var camForm = null;
        var form = $scope.form = {
          '$valid': false,
          '$invalid': true
        };

        $scope.$watch('tasklistForm', function (value) {
          if (value) {
            showForm(container, value, formController.getParams());
          }
        });

        $scope.$watch(function() {
          return form && form.$valid;
        }, function(value) {
          formController.notifyFormValidated(!value);
        });

        function showForm(container, tasklistForm, params) {
          var formUrl = tasklistForm.key;

          params = angular.copy(params);

          delete params.processDefinitionKey;

          angular.extend(params, {
            containerElement: container,
            client: camAPI,
            formUrl: formUrl,
            done: done
          });

          camForm = new CamForm(params);

        }

        var done = function (err, _camForm) {
          if (err) {
            return formController.notifyFormInitializationFailed(err);
          }
          camForm = _camForm;

          var formName = _camForm.formElement.attr('name');
          var camFormScope = _camForm.formElement.scope();

          if (!camFormScope) {
            return;
          }

          form = camFormScope[formName];
          formController.notifyFormInitialized();
        };

        var complete = function (callback) {
          camForm.submit(callback);
        };

        var save = function(callback) {
          camForm.store(callback);
        };

        var restore = function(callback) {
          camForm.restore(callback);
        };

        formController.registerCompletionHandler(complete);
        formController.registerSaveHandler(save);
        formController.registerRestoreHandler(restore);

        $scope.$on('authentication.login.required', function() {
          save();
        });

      }

    };

  }];

});

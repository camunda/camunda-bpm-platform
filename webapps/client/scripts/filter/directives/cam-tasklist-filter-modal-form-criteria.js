define([
  'angular',
  'text!./cam-tasklist-filter-modal-form-criteria.html',
  './cam-tasklist-filter-modal-criteria'
], function(
  angular,
  template,
  criteria
) {
  'use strict';

  var each = angular.forEach;
  var copy = angular.copy;

  var criteriaExpressionSupport = {};
  var criteriaHelp              = {};
  var criteriaValidator         = {};

  var defaultValidate = function () {
    return { valid : true };
  };

  each(criteria, function(group) {
    each(group.options, function(criterion) {
      criteriaExpressionSupport[criterion.name] = criterion.expressionSupport;
      criteriaHelp[criterion.name]              = criterion.help      || group.help;
      criteriaValidator[criterion.name]         = criterion.validate  || group.validate || defaultValidate;
    });
  });

  return [function() {

    return {

      restrict: 'A',
      require: '^camTasklistFilterModalForm',
      scope: {
        filter: '=',
        accesses: '='
      },

      template: template,

      link: function ($scope, $element, attrs, parentCtrl) {

        var emptyCriterion = {
          key: '',
          value: ''
        };

        $scope.criteria = criteria;
        $scope.criteriaExpressionSupport = criteriaExpressionSupport;
        $scope.criteriaHelp = criteriaHelp;

        $scope.query = $scope.filter.query = $scope.filter.query || [];

        // register handler to show or hide the accordion hint /////////////////

        var showHintProvider = function () {
          for (var i = 0, nestedForm; !!(nestedForm = nestedForms[i]); i++) {
            var queryParamKey = nestedForm.queryParamKey;
            var queryParamValue = nestedForm.queryParamValue;

            if (queryParamKey.$dirty && queryParamKey.$invalid) {
              return true;
            }

            if (queryParamValue.$dirty && queryParamValue.$invalid) {
              return true;
            }
          }

          return false;
        };

        parentCtrl.registerHintProvider('filterCriteriaForm', showHintProvider);

        // handles each nested form//////////////////////////////////////////////

        var nestedForms = [];
        $scope.addForm = function (_form) {
          nestedForms.push(_form);
        };

        // criterion interaction ///////////////////////////////////////////////

        $scope.addCriterion = function() {
          var _emptyCriteria = copy(emptyCriterion);
          $scope.query.push(_emptyCriteria);
        };

        $scope.removeCriterion = function(delta) {
          $scope.filter.query = $scope.query = parentCtrl.removeArrayItem($scope.query, delta);
          nestedForms = parentCtrl.removeArrayItem(nestedForms, delta);
        };

        $scope.valueChanged = function(queryParam, control) {
          control.$setValidity('number', true);
          control.$setValidity('date', true);
          if (queryParam.value) {
            if (control.$pristine) {
              control.$setViewValue(queryParam.value);
            }
            var key = getCriterionName(queryParam.key);
            var validationResult = criteriaValidator[key](queryParam.value);

            if (!validationResult.valid) {
              control.$setValidity(validationResult.error, false);
            }
          }

        };

        // helper //////////////////////////////////////////////////////////////

        $scope.getQueryParamKeys = function() {
          var result = [];

          for (var i = 0, entry; !!(entry = $scope.query[i]); i++) {
            var criterionName = getCriterionName(entry.key);
            result.push(criterionName);

            if (criteriaExpressionSupport[criterionName]) {
              result.push(criterionName + 'Expression');
            }
          }

          return result;
        };

        var getCriterionName = $scope.getCriterionName = function(name) {
          if (!name) { return name; }
          var simple = name.replace('Expression', '');
          return simple;
        };

        var getCriteriaHelp = $scope.getCriteriaHelp = function (key) {
          key = getCriterionName(key);

          return criteriaHelp[key];
        };

        $scope.isCriteriaHelpAvailable = function (key) {
          return !!getCriteriaHelp(key);
        };

      }

    };

  }];

});

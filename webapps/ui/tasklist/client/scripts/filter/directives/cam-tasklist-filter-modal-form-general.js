'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-filter-modal-form-general.html', 'utf8');

module.exports = [function() {

  return {

    restrict: 'A',
    require: '^camTasklistFilterModalForm',
    scope: {
      filter: '=',
      accesses: '='
    },

    template: template,

    link: function($scope, $element, attrs, parentCtrl) {

        // init //////////////////////////////////////////////////////////

      var _form = $scope.filterGeneralForm;

      var controls = [];
      controls.push(_form.filterColor);
      controls.push(_form.filterName);
      controls.push(_form.filterPriority);
      controls.push(_form.filterDescription);
      controls.push(_form.filterRefresh);

        // register hint provider ////////////////////////////////////////

      var showHintProvider = function() {
        for (var i = 0, control; (control = controls[i]); i++) {
          if (control.$dirty && control.$invalid) {
            return true;
          }
        }
        return false;
      };

      parentCtrl.registerHintProvider('filterGeneralForm', showHintProvider);
    }

  };

}];

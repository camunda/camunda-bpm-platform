/**
 * @module  cam.tasklist.filter
 * @belongsto cam.tasklist
 *
 * Filters are predefined filters for tasks.
 */

'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),

  /* directives */
    camTasklistFilters = require('./directives/cam-tasklist-filters'),
    camTasklistFilterModalForm = require('./directives/cam-tasklist-filter-modal-form'),
    camTasklistFilterModalFormGeneral = require('./directives/cam-tasklist-filter-modal-form-general'),
    camTasklistFilterModalFormCriteria = require('./directives/cam-tasklist-filter-modal-form-criteria'),
    camTasklistFilterModalFormVariable = require('./directives/cam-tasklist-filter-modal-form-variable'),
    camTasklistFilterModalFormPermission = require('./directives/cam-tasklist-filter-modal-form-permission'),

  /* controllers */
    camTasklistFiltersCtrl = require('./controllers/cam-tasklist-filters-ctrl'),

  /* modals */
    camTasklistFilterModal = require('./modals/cam-tasklist-filter-modal');


var filterModule = angular.module('cam.tasklist.filter', [
  'ui.bootstrap'
]);

  /* directives */
filterModule.directive('camTasklistFilters', camTasklistFilters);
filterModule.directive('camTasklistFilterModalForm', camTasklistFilterModalForm);
filterModule.directive('camTasklistFilterModalFormGeneral', camTasklistFilterModalFormGeneral);
filterModule.directive('camTasklistFilterModalFormCriteria', camTasklistFilterModalFormCriteria);
filterModule.directive('camTasklistFilterModalFormVariable', camTasklistFilterModalFormVariable);
filterModule.directive('camTasklistFilterModalFormPermission', camTasklistFilterModalFormPermission);

  /* controllers */
filterModule.controller('camFiltersCtrl', camTasklistFiltersCtrl);


  /* modals */
filterModule.controller('camFilterModalCtrl', camTasklistFilterModal);

module.exports = filterModule;

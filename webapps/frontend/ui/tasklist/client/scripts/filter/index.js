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

var filterModule = angular.module('cam.tasklist.filter', ['ui.bootstrap']);

/* directives */
filterModule.directive('camTasklistFilters', camTasklistFilters);
filterModule.directive(
  'camTasklistFilterModalForm',
  camTasklistFilterModalForm
);
filterModule.directive(
  'camTasklistFilterModalFormGeneral',
  camTasklistFilterModalFormGeneral
);
filterModule.directive(
  'camTasklistFilterModalFormCriteria',
  camTasklistFilterModalFormCriteria
);
filterModule.directive(
  'camTasklistFilterModalFormVariable',
  camTasklistFilterModalFormVariable
);
filterModule.directive(
  'camTasklistFilterModalFormPermission',
  camTasklistFilterModalFormPermission
);

/* controllers */
filterModule.controller('camFiltersCtrl', camTasklistFiltersCtrl);

/* modals */
filterModule.controller('camFilterModalCtrl', camTasklistFilterModal);

module.exports = filterModule;

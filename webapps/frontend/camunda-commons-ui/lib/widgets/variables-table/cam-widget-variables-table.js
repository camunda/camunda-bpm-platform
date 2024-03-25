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

var angular = require('camunda-bpm-sdk-js/vendor/angular'),
  varUtils = require('../variable/cam-variable-utils'),
  template = require('./cam-widget-variables-table.html?raw'),
  confirmationTemplate = require('./cam-widget-variables-deletion-dialog.html?raw');

var typeUtils = varUtils.typeUtils;

var emptyVariable = {
  variable: {
    name: null,
    type: null,
    value: null,
    valueInfo: {}
  },
  additions: {}
};

function deepCopy(original) {
  return JSON.parse(JSON.stringify(original));
}

function noopPromise(info /*, i*/) {
  return {
    then: function(success /*, error*/) {
      success(angular.copy(info.variable));
      // error('Not implemented');
    }
  };
}

module.exports = [
  '$uibModal',
  '$translate',
  '$document',
  function($modal, $translate, $document) {
    return {
      template: template,

      scope: {
        variables: '=?camVariables',
        headers: '=?camHeaders',
        editable: '=?camEditable',
        validatable: '@',

        isVariableEditable: '=?',
        deleteVar: '=?onDelete',
        saveVar: '=?onSave',
        onSaved: '=?',
        editVar: '=?onEdit',
        downloadVar: '=?onDownload',
        uploadVar: '=?onUpload',
        onSortChange: '&',
        onValidation: '&',
        onChangeStart: '&',
        onChangeEnd: '&',
        onToggleEditMode: '=?',
        defaultSort: '=?',
        ignoreTypes: '=?'
      },

      link: function($scope) {
        /**
         * Starting with clipboard.js 2.0.9 creating a
         * fake textarea for values not of type string is broken.
         * Cf. https://github.com/camunda/camunda-bpm-platform/issues/4190
         * @param value the value that is not a string.
         * @returns {string} a string value.
         */
        $scope.asString = value => value + '';

        if ($scope.validatable) {
          const onHideValidationPopover = e => {
            const modalWindow = angular.element('.modal');
            const modalBackdrop = angular.element('.modal-backdrop');
            if (
              (modalWindow.length === 0 && modalBackdrop.length === 0) ||
              (modalWindow !== e.target &&
                modalBackdrop !== e.target &&
                !modalWindow[0].contains(e.target) &&
                !modalBackdrop[0].contains(e.target))
            ) {
              $scope.$apply(() => {
                $scope.variables.forEach(
                  variable => (variable.showFailures = false)
                );
              });
            }
          };
          $document.on('click', onHideValidationPopover);
          $scope.$on('$destroy', () =>
            $document.off('click', onHideValidationPopover)
          );
        }

        var backups = [];

        function _getVar(v) {
          return ($scope.variables[v] || {}).variable;
        }

        function hasChanged(v) {
          if (
            !$scope.variables ||
            !$scope.variables[v] ||
            !$scope.variables[v]._copy
          ) {
            return false;
          }
          var yep;
          var now = _getVar(v);
          var before = $scope.variables[v]._copy;

          yep = !now || !before;

          yep = yep || now.name !== before.name;
          yep = yep || now.type !== before.type;
          yep = yep || now.value !== before.value;

          // if (now.valueInfo) {}
          // yep = yep || (now.valueInfo !== before.valueInfo);

          return yep;
        }

        // Array of header classes
        $scope.headerClasses = [];
        $scope.headers.forEach(function(column) {
          $scope.headerClasses.push(column.class);
        });

        $scope.editable = $scope.editable || $scope.headerClasses;

        $scope.variableTypes = angular.copy(varUtils.types);
        $scope.variableTypes = $scope.ignoreTypes
          ? $scope.variableTypes.filter(
              types => !$scope.ignoreTypes.includes(types)
            )
          : $scope.variableTypes;
        $scope.defaultValues = varUtils.defaultValues;
        $scope.isPrimitive = varUtils.isPrimitive($scope);
        $scope.isBinary = varUtils.isBinary($scope);
        $scope.useCheckbox = varUtils.useCheckbox($scope);

        ['uploadVar', 'deleteVar', 'saveVar'].forEach(function(name) {
          $scope[name] = angular.isFunction($scope[name])
            ? $scope[name]
            : noopPromise;
        });

        // Set default sorting
        $scope.sortObj = $scope.defaultSort;

        // Order Icons
        $scope.orderClass = function(forColumn) {
          forColumn = forColumn || $scope.sortObj.sortBy;
          var icons = {
            none: 'minus',
            desc: 'chevron-down',
            asc: 'chevron-up'
          };
          return (
            'glyphicon-' +
            icons[
              forColumn === $scope.sortObj.sortBy
                ? $scope.sortObj.sortOrder
                : 'none'
            ]
          );
        };

        // On-click function to order Columns
        $scope.changeOrder = function(column) {
          $scope.sortObj.sortBy = column;
          $scope.sortObj.sortOrder =
            $scope.sortObj.sortOrder === 'desc' ? 'asc' : 'desc';
          // pass sorting to updateView function in parent scope.
          $scope.onSortChange({sortObj: $scope.sortObj});
        };

        var variableModalConfig = function(variable, template, readonly) {
          return {
            template: template,

            controller: varUtils.modalCtrl,

            windowClass: 'cam-widget-variable-dialog',

            resolve: {
              variable: function() {
                return angular.copy(_getVar(variable));
              },
              readonly: readonly
            }
          };
        };

        $scope.editVar = angular.isFunction($scope.editVar)
          ? $scope.editVar
          : function(info, v) {
              var readonly = function() {
                return !$scope.isEditable('value', $scope.variables[v]);
              };
              const result = $modal.open(
                variableModalConfig(v, varUtils.templateDialog, readonly)
              ).result;
              result.then(() => (info.changed = true)).catch(angular.noop);
              return result;
            };

        $scope.readStringVar = angular.isFunction($scope.readStringVar)
          ? $scope.readStringVar
          : function(v) {
              var readonly = function() {
                return true;
              };

              return $modal.open(
                variableModalConfig(v, varUtils.templateStringDialog, readonly)
              ).result;
            };

        $scope.downloadLink = angular.isFunction($scope.downloadVar)
          ? $scope.downloadVar
          : function(info) {
              return (
                '/camunda/api/engine/engine/default/variable-instance/' +
                info.variable.id +
                '/data'
              );
            };

        function validate(info, i) {
          if (!info.variable.name || !info.variable.type) {
            info.valid = false;
          } else if (
            info.variable.value === null ||
            ['String', 'Object', 'Null'].indexOf(info.variable.type) > -1
          ) {
            info.valid = true;
          } else {
            info.valid = typeUtils.isType(
              info.variable.value,
              info.variable.type
            );
          }

          if (info.valid) {
            // save the variable in the appropriate type
            if (
              info.variable.type &&
              info.variable.value !== null &&
              $scope.isPrimitive(info.variable.type)
            ) {
              var newTyped;

              if (info.variable.type !== 'Boolean') {
                newTyped = typeUtils.convertToType(
                  info.variable.value,
                  info.variable.type
                );
              } else {
                newTyped = info.variable.value
                  ? info.variable.value !== 'false'
                  : false;
              }

              // only change value if newType has different type, to avoid infinite recursion
              if (typeof info.variable.value !== typeof newTyped) {
                info.variable.value = newTyped;
              }
            }
          }

          info.changed = hasChanged(i);
        }

        function initVariables() {
          ($scope.variables || []).forEach(function(info, i) {
            info.valid = true;

            var varPath = 'variables[' + i + '].variable';

            function wrapedValidate() {
              validate(info, i);
            }

            $scope.$watch(varPath + '.value', wrapedValidate);
            $scope.$watch(varPath + '.name', wrapedValidate);
            $scope.$watch(varPath + '.type', wrapedValidate);

            $scope.$watch('variables[' + i + '].editMode', function(
              now,
              before
            ) {
              if (angular.isUndefined(now)) {
                return;
              }

              if (now === true) {
                info._copy = deepCopy(info.variable);
              } else if (now === false && before === true && info._copy) {
                info.variable.type = info._copy.type;
                info.variable.name = info._copy.name;
                info.variable.value = info._copy.value;

                delete info._copy;
              }
            });

            validate(info, i);
            backups[i] = info.variable.value;
          });
        }
        $scope.$watch('variables', initVariables);
        $scope.$on('variable.added', () => initVariables());
        initVariables();

        $scope.canEditVariable = angular.isFunction($scope.isVariableEditable)
          ? $scope.isVariableEditable
          : function() {
              return true;
            };

        $scope.isEditable = function(what, info) {
          return info.editMode && $scope.editable.indexOf(what) > -1;
        };

        $scope.hasEditDialog = function(type) {
          return (
            type &&
            ['object', 'string', 'json', 'xml'].indexOf(type.toLowerCase()) > -1
          );
        };

        $scope.rowClasses = function(info /*, v*/) {
          return [
            info.editMode ? 'editing' : null,
            info.valid ? null : 'ng-invalid',
            info.valid ? null : 'ng-invalid-cam-variable-validator'
          ];
        };

        $scope.colClasses = function(info, headerClass /*, v*/) {
          return [
            $scope.isEditable(headerClass, info) ? 'editable' : null,
            'type-' + (info.variable.type || '').toLowerCase(),
            'col-' + headerClass
          ];
        };

        $scope.isNull = function(v) {
          return $scope.variables[v].variable.value === null;
        };

        $scope.setNull = function(v) {
          var variable = _getVar(v);
          backups[v] = variable.value;
          variable.value = null;
        };

        $scope.setNonNull = function(v) {
          var variable = _getVar(v);
          variable.value = backups[v] || $scope.defaultValues[variable.type];
        };

        $scope.editVariableValue = function(v) {
          var info = $scope.variables[v];
          $scope
            .editVar(info, v)
            .then(function(result) {
              _getVar(v).value = result.value;
              _getVar(v).valueInfo = result.valueInfo;
            })
            .catch(angular.noop);
        };

        $scope.addVariable = function() {
          $scope.variables.push(angular.copy(emptyVariable));
        };

        $scope.deleteVariable = function(v) {
          var info = $scope.variables[v];
          $modal
            .open({
              controller: [
                '$scope',
                function($scope) {
                  $scope.body = $translate.instant(
                    'CAM_WIDGET_VARIABLES_TABLE_DIALOGUE',
                    {name: info.variable.name}
                  );
                  $scope.submit = function() {
                    $scope.$close();
                    deleteVariable(v);
                  };

                  $scope.dismiss = function() {
                    $scope.$close();
                  };
                }
              ],
              template: confirmationTemplate
            })
            .result.catch(angular.noop);
        };

        var deleteVariable = function(v) {
          var info = $scope.variables[v];

          $scope.deleteVar(info, v).then(
            function() {
              $scope.variables.splice($scope.variables.indexOf(info), 1);
            },
            function(/*err*/) {
              // console.error(err);
            }
          );
        };

        $scope.saveVariable = function(v) {
          var info = $scope.variables[v];
          $scope.enableEditMode(info, false);
          $scope.saveVar(info, v).then(
            function(saved) {
              info.variable.name = saved.name;
              var type = (info.variable.type = saved.type);
              info.variable.value = saved.value;
              delete info._copy;

              if (type !== 'Object') {
                delete info.variable.valueInfo;
              } else {
                info.variable.valueInfo = saved.valueInfo;
              }
              $scope.onSaved && $scope.onSaved(v, info);
            },
            function(/*err*/) {
              // console.error(err);
              $scope.enableEditMode(info, true);
            }
          );
        };

        $scope.uploadVariable = function(v) {
          var info = $scope.variables[v];

          $scope.uploadVar(info, v).then(
            function(/*saved*/) {
              delete info._copy;
              $scope.enableEditMode(info, false);
            },
            function(/*err*/) {
              // console.error(err);
              $scope.enableEditMode(info, false);
            }
          );
        };

        $scope.enableEditMode = function(info, enableEditMode) {
          $scope.onToggleEditMode &&
            $scope.onToggleEditMode(info, enableEditMode);
          info.editMode = enableEditMode;
          if (enableEditMode) {
            var uncompletedCount = 0;
            $scope.variables.forEach(function(variable) {
              if (variable.editMode) {
                uncompletedCount++;
              }
            });
            if (uncompletedCount === 1) {
              $scope.onChangeStart();
            }
          } else {
            var completedCount = 0;
            $scope.variables.forEach(function(variable) {
              if (!variable.editMode) {
                completedCount++;
              }
            });
            if (completedCount === $scope.variables.length) {
              $scope.onChangeEnd();
            }
          }
        };
      }
    };
  }
];

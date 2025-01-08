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
  copy = angular.copy,
  $ = require('jquery'),
  template = require('./cam-widget-search.html?raw');

var dateRegex = /(\d\d\d\d)-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)(?:.(\d\d\d)| )?$/;

function getType(value) {
  if (value && typeof value === 'string' && value.match(dateRegex)) {
    return 'date';
  }
  return typeof value;
}

var isValid = function(search) {
  return (
    search.type.value &&
    (!search.extended || search.name.value) &&
    (search.basic || search.operator.value) &&
    (search.basic || search.value.value) &&
    (getType(search.value.value) === 'date' || !search.enforceDates)
  );
};

var validateOperator = function(operator) {
  if (!operator.value) {
    operator.value = operator.values[0];
    return;
  }
  var idx = operator.values
    .map(function(el) {
      return el.key;
    })
    .indexOf(operator.value.key);
  operator.value = operator.values[idx === -1 ? 0 : idx];
};

var parseValue = function(value, enforceString) {
  if (enforceString) {
    return '' + value;
  }
  if (!isNaN(value) && value.trim() !== '') {
    // value must be transformed to number
    return +value;
  }
  if (value === 'true') {
    return true;
  }
  if (value === 'false') {
    return false;
  }
  if (value === 'NULL') {
    return null;
  }
  return value;
};

// global flag for all instances to ignore URL updates to update searches
var IGNORE_URL_UPDATE = false;

module.exports = [
  '$timeout',
  '$location',
  'search',
  'widgetLocalConf',
  '$translate',
  function($timeout, $location, searchService, widgetLocalConf, $translate) {
    // check if browser is affected by IE focus bug:
    // https://connect.microsoft.com/IE/feedback/details/810538/ie-11-fires-input-event-on-focus
    var checkIEfocusBug = function(cb) {
      // special timeout so we do not fall into an apply cycle
      $timeout(
        function() {
          // create input field to make "feature" detection of the bug
          var el = document.createElement('input');
          el.setAttribute('type', 'text');

          // bug only happens when placeholder is set
          el.setAttribute('placeholder', 'set');

          // this event listener is only called when the browser is affected by the bug
          var weAreScrewed = false;
          el.addEventListener('input', function() {
            // we are affected by the IE focus bug and cannot use the placeholder attribute on the search input field
            weAreScrewed = true;
          });

          // perform the test
          document.body.appendChild(el);
          el.focus();
          document.body.removeChild(el);

          // the event is handled asynchronously, so we have to wait for the result
          $timeout(function() {
            cb(weAreScrewed);
          });
        },
        0,
        false
      );
    };

    return {
      restrict: 'A',

      scope: {
        types: '=camWidgetSearchTypes',
        translations: '=camWidgetSearchTranslations',
        operators: '=camWidgetSearchOperators',
        searches: '=?camWidgetSearchSearches',
        validSearches: '=?camWidgetSearchValidSearches',
        storageGroup: '=?camWidgetSearchStorageGroup',
        searchId: '@camWidgetSearchId',
        total: '=?camWidgetSearchTotal',
        matchAny: '=?camWidgetSearchMatchAny',
        disableTypeaheadAutoselect:
          '=?camWidgetSearchDisableTypeaheadAutoselect'
      },

      link: function($scope, element) {
        angular.forEach($scope.translations, function(value, key) {
          $scope.translations[key] = $translate.instant(value);
        });

        $scope.types.map(function(el) {
          el.id.value = $translate.instant(el.id.value);
          if (el.operators) {
            el.operators = el.operators.map(function(op) {
              op.value = $translate.instant(op.value);
              return op;
            });
          }

          if (el.options && typeof el.options[0] === 'object') {
            el.mappedOptions = el.options.map(({key, value}) => {
              return {
                key: key,
                value: $translate.instant(value)
              };
            });

            el.options = el.mappedOptions.map(({value}) => value);
          }
          return el;
        });

        angular.forEach($scope.operators, function(operatorGroupedByType) {
          angular.forEach(operatorGroupedByType, function(operator) {
            operator.value = $translate.instant(operator.value);
          });
        });

        $scope.isMatchAnyActive = typeof $scope.matchAny !== 'undefined';

        $scope.caseHandeling = {};
        $scope.switchMatchType = function() {
          if ($scope.isMatchAnyActive) {
            $scope.matchAny = !$scope.matchAny;
          }
        };

        $scope.focused = false;
        var formElement = angular.element(element).find('form')[0];
        formElement.addEventListener(
          'focus',
          function() {
            $timeout(function() {
              $scope.focused = true;
            });
          },
          true
        );
        formElement.addEventListener(
          'blur',
          function() {
            $timeout(function() {
              $scope.focused = false;
            });
          },
          true
        );

        var searchHasVariableQuery = function() {
          return ($scope.searches || []).some(element => {
            return element.caseOptions;
          });
        };

        $scope.searchHasVariableQuery = searchHasVariableQuery();

        // test for IE focus bug
        checkIEfocusBug(function(hasBug) {
          if (hasBug) {
            // if we are afftected by the focus bug, we cannot set a placeholder on the input field
            // add another indication for the search field
            var node = document.createElement('div');
            node.textContent = $scope.translations.inputPlaceholder + ':';
            element[0].insertBefore(node, element[0].firstChild);
            $scope.$root.$broadcast('plugin:search:change');
          } else {
            // if we are not affected by the focus bug, we can set the placeholder on the input field
            element[0]
              .querySelector('input.main-field')
              .setAttribute(
                'placeholder',
                $scope.translations.inputPlaceholder
              );
          }
        });

        $scope.searchTypes = $scope.types.map(function(el) {
          return el.id;
        });

        $scope.getRightPadding = function() {
          if (element.width() > 400) {
            return '125px';
          }

          return '12px';
        };

        var defaultType = $scope.types.reduce(function(done, type) {
          return done || (type.default ? type : null);
        }, null);

        var getTypes = function() {
          // check which classes are allowed
          var aggregatedTypeKeys = $scope.searches
            .map(function(el) {
              return el.type.value.key;
            })
            .reduce(function(aggregatedList, type) {
              if (aggregatedList.indexOf(type) === -1) {
                aggregatedList.push(type);
              }
              return aggregatedList;
            }, []);

          var allowedGroups = aggregatedTypeKeys
            .map(function(el) {
              return getConfigByTypeKey(el)
                ? getConfigByTypeKey(el).groups
                : null;
            })
            .filter(function(el) {
              return !!el;
            })
            .reduce(function(groupsArray, groups) {
              if (groupsArray) {
                if (groupsArray.length === 0) {
                  return angular.copy(groups);
                }
                for (var i = 0; i < groupsArray.length; i++) {
                  if (groups.indexOf(groupsArray[i]) === -1) {
                    groupsArray.splice(i, 1);
                    i--;
                  }
                }
                if (groupsArray.length === 0) {
                  return null;
                } else {
                  return groupsArray;
                }
              } else {
                return null;
              }
            }, []);

          if (allowedGroups === null) {
            return [];
          } else if (allowedGroups.length === 0) {
            return $scope.searchTypes;
          } else {
            return $scope.searchTypes.filter(function(el) {
              var groups = getConfigByTypeKey(el.key).groups;
              if (!groups) return true;
              for (var i = 0; i < groups.length; i++) {
                if (allowedGroups.indexOf(groups[i]) > -1) {
                  return true;
                }
              }
              return false;
            });
          }
        };

        var getConfigByTypeKey = function(typeKey) {
          return $scope.types.reduce(function(done, type) {
            return done || (type.id.key === typeKey ? type : null);
          }, null);
        };

        var getOperators = function(config, value) {
          return (
            config.operators ||
            $scope.operators[getType(parseValue(value, config.enforceString))]
          );
        };

        var filteredSearches = function(original) {
          const getKeyAndValue = (mappedOptions, search) => {
            let key = null;
            let value = null;
            const inOperator = search.operator === 'In';
            if (mappedOptions) {
              const options = mappedOptions.filter(
                option => inOperator && search.value.includes(option.key)
              );
              if (inOperator) {
                const keys = options.map(option => option.key);
                if (keys.length) {
                  key = keys;
                }
                value = options.map(option => option.value).join(', ');
              } else {
                const option = (mappedOptions || []).find(
                  option => option.key === search.value
                );
                key = option?.key;
                value = option?.value;
              }
            } else if (inOperator) {
              value = search.value.join(',');
            }

            if (!value) {
              value = search.value;
            }

            return {
              key: key,
              value: value
            };
          };

          return original
            .map(function(search) {
              var config = getConfigByTypeKey(search.type);
              if (config) {
                var newSearch = {
                  extended: config.extended,
                  basic: config.basic,
                  type: {
                    values: getTypes(),
                    value: getTypes().reduce(function(done, type) {
                      return done || (type.key === search.type ? type : null);
                    }, null),
                    tooltip: $scope.translations.type
                  },

                  name: {
                    value: search.name,
                    tooltip: $scope.translations.name
                  },

                  options: config.options,

                  operator: {
                    tooltip: $scope.translations.operator
                  },

                  value: {
                    ...getKeyAndValue(config.mappedOptions, search),
                    tooltip: $scope.translations.value
                  },
                  allowDates: config.allowDates,
                  enforceDates: config.enforceDates,
                  potentialNames: config.potentialNames || [],
                  enforceString: config.enforceString,
                  caseOptions: config.caseOptions
                };
                newSearch.operator.values = getOperators(
                  config,
                  newSearch.value.value
                );
                newSearch.operator.value = newSearch.operator.values.reduce(
                  function(done, op) {
                    return done || (op.key === search.operator ? op : null);
                  },
                  null
                );

                newSearch.valid = isValid(newSearch);
                return newSearch;
              } else {
                if (search.type === 'variableNamesIgnoreCase')
                  $scope.caseHandeling.ignoreNames = true;
                if (search.type === 'variableValuesIgnoreCase')
                  $scope.caseHandeling.ignoreValues = true;
              }
            })
            .filter(function(search) {
              return search;
            });
        };

        var searchId = $scope.searchId || 'search';

        var getSearchesFromURL = function() {
          var urlSearches = JSON.parse(
            ($location.search() || {})[searchId + 'Query'] || '[]'
          );
          return filteredSearches(urlSearches);
        };

        $scope.searches = $scope.searches || [];
        $scope.searches = getSearchesFromURL();
        $scope.validSearchesBuffer = $scope.searches.reduce(function(
          valid,
          search
        ) {
          if (search.valid) {
            valid.push(search);
          }
          return valid;
        },
        []);
        $scope.validSearches = angular.copy($scope.validSearchesBuffer);

        var selectNextInvalidElement = function(startIndex, startField) {
          var search = $scope.searches[startIndex];
          if (!search.valid) {
            if (
              search.extended &&
              !search.name.value &&
              startField !== 'name'
            ) {
              search.name.inEdit = true;
              return;
            } else if (startField !== 'value') {
              search.value.inEdit = true;
              return;
            }
          }
          for (var i = 1; i < $scope.searches.length; i++) {
            var idx = (i + startIndex) % $scope.searches.length;
            search = $scope.searches[idx];
            if (!search.valid) {
              if (search.extended && !search.name.value) {
                search.name.inEdit = true;
              } else {
                search.value.inEdit = true;
              }
              return;
            }
          }
        };

        $scope.createSearch = function(type) {
          if (!type && !$scope.inputQuery) {
            return;
          }

          var value = !type ? $scope.inputQuery : '';

          type = (type && getConfigByTypeKey(type.key)) || defaultType;

          var operators = getOperators(type, value);

          $scope.searches.push({
            extended: type.extended,
            basic: type.basic,
            type: {
              values: getTypes(),
              value: type.id,
              tooltip: $scope.translations.type
            },
            name: {
              value: '',
              inEdit: type.extended,
              tooltip: $scope.translations.name
            },
            operator: {
              value: operators[0],
              values: operators,
              tooltip: $scope.translations.operator
            },
            options: type.options,
            value: {
              value: value,
              inEdit: !type.extended && !value,
              tooltip: $scope.translations.value
            },
            allowDates: type.allowDates,
            enforceDates: type.enforceDates,
            potentialNames: type.potentialNames,
            enforceString: type.enforceString,
            caseOptions: type.caseOptions
          });
          var search = $scope.searches[$scope.searches.length - 1];
          search.valid = isValid(search);

          // To those who think, WHAT THE HECK IS THIS?!:
          //
          // Typeahead thinks, it is a good idea to focus the input field after selecting an option via mouse click
          // (see https://github.com/angular-ui/bootstrap/blob/e909b922a2ce09792a733652e5131e9a95b35e5b/src/typeahead/typeahead.js#L274)
          // We do not want this. Since they are registering their focus event per timeout AFTER we register our
          // blur event per timeout, the field is focussed in the end. How to prevent this? More timeouts x_x
          if (!value) {
            $timeout(function() {
              $timeout(function() {
                $scope.inputQuery = '';
                $(element[0].querySelector('.search-container > input')).blur();
              });
            });
          } else {
            $scope.inputQuery = '';
          }
        };

        $scope.deleteSearch = function(idx) {
          $scope.searches.splice(idx, 1);
          $timeout(function() {
            $(element[0].querySelector('.search-container > input')).focus();
          });
        };

        const hasOption = (string, value) =>
          string
            .toUpperCase()
            .split(',')
            .map(strOpt => strOpt.trim())
            .includes(value.toUpperCase());

        $scope.handleChange = function(idx, field, before, value, evt) {
          var config;
          var search = $scope.searches[idx];
          if (field === 'type') {
            config = getConfigByTypeKey(value.key);

            search.extended = config.extended;
            search.basic = config.basic;
            search.allowDates = config.allowDates;

            if (!search.enforceDates && config.enforceDates) {
              search.value.value = '';
            }
            search.enforceDates = config.enforceDates;
            search.operator.values = getOperators(config, search.value.value);
            validateOperator(search.operator);
          } else if (field === 'value') {
            if (idx === $scope.searches.length - 1) {
              $timeout(function() {
                $(
                  element[0].querySelector('.search-container > input')
                ).focus();
              });
            }
            config = getConfigByTypeKey(search.type.value.key);
            if (!config.operators) {
              search.operator.values = getOperators(config, search.value.value);
              validateOperator(search.operator);
            }
          }
          search.valid = isValid(search);

          if (evt && evt.keyCode === 13) {
            selectNextInvalidElement(idx, field);
          }

          const mappedOptions = $scope.types.find(
            type => type.id.key === search.type.value.key
          )?.mappedOptions;
          if (mappedOptions) {
            if (search.operator.value.key === 'In') {
              const keys = mappedOptions
                .filter(option => hasOption(search.value.value, option.value))
                .map(option => option.key);
              search.value.key = keys.length ? keys : undefined;
            } else {
              search.value.key = mappedOptions.find(
                option => search.value.value === option.value
              )?.key;
            }
          } else {
            if (search.operator.value.key === 'In') {
              search.value.key = search.value.value
                .split(',')
                .map(value => value.trim());
            } else {
              search.value.key = search.value.value;
            }
          }
        };

        $scope.onKeydown = function(evt) {
          if ([38, 40, 13].indexOf(evt.keyCode) !== -1) {
            var dd = $(
              element[0].querySelectorAll('.dropdown-menu[id^="typeahead"]')
            );
            if (dd.length === 0) {
              $timeout(function() {
                angular.element(evt.target).triggerHandler('input');
              });
            }
          }
        };

        var extractSearches = function(searches) {
          const getValue = search => {
            const mappedOptions = $scope.types.find(
              type => type.id.key === search.type.value.key
            )?.mappedOptions;

            let value = null;
            if (mappedOptions) {
              if (search.operator.value.key === 'In') {
                const values = mappedOptions
                  .filter(option => hasOption(search.value.value, option.value))
                  .map(option => option.key);

                if (values.length) {
                  value = values;
                }
              } else {
                value = mappedOptions.find(
                  option => search.value.value === option.value
                )?.key;
              }
            } else if (search.operator.value.key === 'In') {
              value = search.value.value.split(',').map(value => value.trim());
            }

            if (!value) {
              value = search.value.value;
            }

            return value;
          };

          var out = [];
          angular.forEach(searches, function(search) {
            out.push({
              type: search.type.value.key,
              operator: search.operator.value.key,
              value: getValue(search),
              name: search.name.value
            });
          });

          return out;
        };

        var defaultSearchObject = {
          basic: true,
          type: {
            values: getTypes(),
            value: {},
            tooltip: ''
          },
          name: {
            value: '',
            inEdit: '',
            tooltip: ''
          },
          operator: {
            value: {
              key: 'eq',
              value: '='
            },
            values: [],
            tooltip: $scope.translations.operator
          },
          value: {
            value: '',
            inEdit: false,
            tooltip: $scope.translations.value
          },
          valid: false
        };

        var handleSearchesUpdate = function() {
          var searches = $scope.searches;
          // add valid searches to validSearchesBuffer
          angular.forEach(searches, function(search) {
            if (
              search.valid &&
              $scope.validSearchesBuffer.indexOf(search) === -1
            ) {
              $scope.validSearchesBuffer.push(search);
            }
          });

          // remove invalid searches from valid searches
          $scope.validSearchesBuffer = $scope.validSearchesBuffer.filter(
            search => {
              return search.valid && searches.indexOf(search) !== -1;
            }
          );

          if ($scope.searchHasVariableQuery) {
            if ($scope.caseHandeling.ignoreNames) {
              let search = angular.copy(defaultSearchObject);
              search.type.value.key = 'variableNamesIgnoreCase';
              $scope.validSearchesBuffer.push(search);
            }
            if ($scope.caseHandeling.ignoreValues) {
              let search = angular.copy(defaultSearchObject);
              search.type.value.key = 'variableValuesIgnoreCase';
              $scope.validSearchesBuffer.push(search);
            }
          }

          var queryObj = {};
          queryObj[searchId + 'Query'] = JSON.stringify(
            extractSearches($scope.validSearchesBuffer)
          );

          if ($scope.isMatchAnyActive) {
            var newLocation;

            if (
              $scope.matchAny &&
              !$location.search().hasOwnProperty(searchId + 'OrQuery') // eslint-disable-line
            ) {
              newLocation = $location.url() + '&' + searchId + 'OrQuery';
            } else if (!$scope.matchAny) {
              newLocation = $location
                .url()
                .replace('&' + searchId + 'OrQuery', '');
            }

            $location.url(newLocation);
            $location.replace();
          }

          $scope.searchHasVariableQuery = searchHasVariableQuery();

          // ignore URL updates for all search widget instances for this update
          IGNORE_URL_UPDATE = true;

          searchService.updateSilently(
            queryObj,
            !$location.search()[searchId + 'Query']
          );

          // listen to URL changes again AFTER the locationchange event has fired
          $timeout(function() {
            IGNORE_URL_UPDATE = false;
          });

          updateSearchTypes();
        };

        $scope.$watch(
          '[searches, matchAny, caseHandeling]',
          handleSearchesUpdate,
          true
        );

        $scope.$on('$locationChangeSuccess', function() {
          $scope.matchAny = $location
            .search()
            .hasOwnProperty(searchId + 'OrQuery'); // eslint-disable-line

          if (
            !IGNORE_URL_UPDATE &&
            $location.search().hasOwnProperty(searchId + 'Query') // eslint-disable-line
          ) {
            // make new array of searches from the url
            var searches = getSearchesFromURL();

            // if something has changed in the valid searches
            var compareSearches = $scope.validSearchesBuffer.filter(search => {
              return search.valid;
            });

            if (!angular.equals(searches, compareSearches)) {
              // now add all invalid searches which exist within the original search array, but are not in the URL
              angular.forEach($scope.searches, function(search) {
                if (!search.valid) {
                  searches.push(search);
                }
              });

              // empty the valid searches buffer (will be automatically refilled by the listener on the searches)
              $scope.validSearchesBuffer = [];

              // replace the original search array with the new one
              $scope.searches = searches;
            }
          }
        });

        var copyValid;
        $scope.$watch(
          'validSearchesBuffer',
          function() {
            $timeout.cancel(copyValid);
            copyValid = $timeout(function() {
              $scope.validSearches = angular.copy($scope.validSearchesBuffer);
            });
          },
          true
        );

        var updateSearchTypes = function() {
          var types = getTypes();
          $scope.dropdownTypes = types;
          for (var i = 0; i < $scope.searches.length; i++) {
            $scope.searches[i].type.values = types;
          }
        };
        $scope.$watch(
          'types',
          function() {
            //in case if array of types changed - update dropdown values
            $scope.searchTypes = $scope.types.map(function(el) {
              return el.id;
            });
            $scope.dropdownTypes = getTypes();

            // Currently we only allow to change the potential names of a type, to support changing the filter
            // in the tasklist while preserving existing search queries
            angular.forEach($scope.searches, function(search) {
              search.potentialNames = getConfigByTypeKey(search.type.value.key)
                ? getConfigByTypeKey(search.type.value.key).potentialNames || []
                : null;
            });
          },
          true
        );

        $scope.dropdownTypes = getTypes();

        /////////////////////////////////////////////////////////////////////
        // criteria persistence
        /////////////////////////////////////////////////////////////////////

        var searchCriteriaStorage = ($scope.searchCriteriaStorage = {
          group: null,
          nameInput: '',
          available: {}
        });
        var stored = {};

        var types = $scope.storageGroup
          ? [$scope.storageGroup]
          : $scope.types
              .map(function(item) {
                return item.groups;
              })
              .reduce(function(current, previous) {
                return (current || []).concat(previous);
              })
              .filter(function(value) {
                return value;
              });

        var groups = [];
        for (var i = 0; i < types.length; i++) {
          if (groups.indexOf(types[i]) < 0 && types[i]) groups.push(types[i]);
        }

        if (!groups.length && $scope.storageGroup) {
          groups.push($scope.storageGroup);
        }

        groups.forEach(function(group) {
          stored[group] = {};
        });

        $scope.$watch(
          'validSearches',
          function determineGroup() {
            if ($scope.storageGroup) {
              searchCriteriaStorage.group = $scope.storageGroup;
              filterCriteria();
              return;
            }

            var _group = null;
            $scope.validSearches.forEach(function(search) {
              if (_group) return;

              var key = search.type.value.key;
              $scope.types.forEach(function(type) {
                if (_group) return;

                // I know that sucks...
                // I mean... it sucks that type.groups is supposed to be an array
                // because if the array has more than 1 item, we can't reliably
                // determine which group is the relevant one
                // (unless we iterate more... which would be the worst nightmare
                // of the guy who will have to maintain that code)
                if (type.id.key === key && (type.groups || []).length === 1) {
                  _group = (type.groups || [])[0];
                }
              });
            });

            searchCriteriaStorage.group = _group;

            filterCriteria();
          },
          true
        );

        function filterCriteria() {
          searchCriteriaStorage.available = {};
          if (searchCriteriaStorage.group) {
            $scope.isSearchCriteriaStorageGrouped = false;
            searchCriteriaStorage.available = copy(
              stored[searchCriteriaStorage.group]
            );
            return;
          }
          $scope.isSearchCriteriaStorageGrouped = true;
          groups.forEach(function(group) {
            searchCriteriaStorage.available[group] = copy(stored[group] || {});
          });
        }

        function groupAndName(str, group) {
          if (group) {
            return {
              group: group,
              name: str
            };
          } else if (searchCriteriaStorage.group) {
            return {
              group: searchCriteriaStorage.group,
              name: str
            };
          }
        }

        stored = widgetLocalConf.get('searchCriteria', stored);
        filterCriteria();

        $scope.$watch('storageGroup', function() {
          if ($scope.storageGroup && groups.indexOf($scope.storageGroup) < 0) {
            return;
          }
          searchCriteriaStorage.group = $scope.storageGroup;
          filterCriteria();
        });

        $scope.storedCriteriaInputClick = function($evt) {
          $evt.stopPropagation();
        };

        $scope.searchCriteriaInputKeydown = function($evt) {
          if ($evt.keyCode === 13) {
            return $scope.storedCriteriaSaveClick($evt);
          }
        };

        $scope.hasCriteriaSets = function() {
          if (groups.length > 1) {
            for (var key in searchCriteriaStorage.available) {
              if (
                Object.keys(searchCriteriaStorage.available[key]).length > 0
              ) {
                return true;
              }
            }
            return false;
          } else {
            return !!Object.keys(searchCriteriaStorage.available || {}).length;
          }
        };

        $scope.loadCriteriaSet = function($evt, name, group) {
          $scope.caseHandeling = {
            ignoreNames: false,
            ignoreValues: false
          };

          var info = groupAndName(name, group);
          if (!info) return;
          var original = stored[info.group][info.name];
          $scope.searches = filteredSearches(original);
          // provided by Harry Potter, DO NOT REMOVE
          if ($scope.isMatchAnyActive) {
            $scope.matchAny = original[original.length - 1]['matchAny'];
          }

          handleSearchesUpdate();
        };

        $scope.dropCriteriaSet = function($evt, name, group) {
          $evt.stopPropagation();

          var info = groupAndName(name, group);
          if (!info) return;
          delete stored[info.group][info.name];

          widgetLocalConf.set('searchCriteria', stored);
          filterCriteria();
        };

        $scope.storedCriteriaSaveClick = function($evt) {
          $evt.stopPropagation();

          var name = searchCriteriaStorage.nameInput;
          if (!name) {
            return;
          }

          stored[searchCriteriaStorage.group] =
            stored[searchCriteriaStorage.group] || {};
          stored[searchCriteriaStorage.group][name] = extractSearches(
            $scope.validSearchesBuffer
          );

          if ($scope.isMatchAnyActive) {
            stored[searchCriteriaStorage.group][name].push({
              matchAny: $scope.matchAny
            });
          }

          stored[searchCriteriaStorage.group][name].push({
            caseHandeling: angular.copy($scope.caseHandeling)
          });

          widgetLocalConf.set('searchCriteria', stored);
          filterCriteria();
          searchCriteriaStorage.nameInput = '';
        };
      },

      template: template
    };
  }
];

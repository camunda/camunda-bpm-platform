'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  'search',  '$location', 'searchWidgetUtils',
  function(search, $location, searchWidgetUtils) {
    return function($scope, setDefaultTab, options) {
      var processData = $scope.processData;
      var processInstance = $scope.processInstance;

      options = options || {};

      // filter
      $scope.filter = parseFilterFromUri();
      processData.provide('filter', parseFilterFromUri());

      processData.observe(['filter', 'instanceIdToInstanceMap', 'activityIdToInstancesMap'], autoCompleteFilter);

      $scope.$on('$locationChangeSuccess', function() {
        var newFilter = parseFilterFromUri($scope.filter);

        if ($location.path().indexOf(processInstance.id) > -1) {
          if (searchWidgetUtils.shouldUpdateFilter(newFilter, $scope.filter, ['activityIds', 'activityInstanceIds', 'page'])) {
            processData.set('filter', newFilter);
          }

          setDefaultTab($scope.processInstanceTabs);
        }
      });

      function parseFilterFromUri(lastFilter) {
        var params = search();
        var activityInstanceIds = searchWidgetUtils.getActivityIdsFromUrlParams('activityInstanceIdIn', params);
        var activityIds = params.activityIds ? params.activityIds.split(',') : [];
        var ignoreActivityIds = shouldActivityIdsBeIgnored(lastFilter, activityIds, activityInstanceIds);

        // if activity ids haven't changed but instance ids have changed
        // then just ignore activity ids from url and let filter auto-complete them
        // because it means that user removed or added instance id to filter by hand
        if (ignoreActivityIds) {
          activityIds = [];
        }

        return {
          activityIds: activityIds,
          activityInstanceIds: activityInstanceIds,
          page: parseInt(params.page, 10) || undefined,
          replace: ignoreActivityIds || !lastFilter
        };
      }

      function shouldActivityIdsBeIgnored(lastFilter, activityIds, activityInstanceIds) {
        return lastFilter && angular.equals(activityIds, lastFilter.activityIds) &&
          !angular.equals(activityInstanceIds, lastFilter.activityInstanceIds);
      }

      function serializeFilterToUri(newFilter, replace) {
        var activityInstanceIds = angular.isArray(newFilter.activityInstanceIds) ? newFilter.activityInstanceIds : [];
        var activityIds = angular.isArray(newFilter.activityIds) ? newFilter.activityIds : [];
        var urlParams = search();
        var searches = JSON.parse(urlParams.searchQuery || '[]');

        //when there is no searchQuery present and there is no ids to add to searchQuery don't change anything
        if (!urlParams.searchQuery && !activityInstanceIds.length && !activityIds.length) {
          searches = null;
        } else {
          searches = searchWidgetUtils.replaceActivitiesInSearchQuery(searches, 'activityInstanceIdIn', activityInstanceIds);
        }

        search.updateSilently({
          searchQuery: searches? JSON.stringify(searches) : null,
          activityIds: activityIds.length ? activityIds.join(',') : null
        }, replace);

        $scope.filter = newFilter;
      }

      /**
       * Auto complete a filter based on the given filter data.
       *
       * It performs the following logic
       *
       *   - If activity instances are selected, select the associated activities unless they are explicitly specified.
       *   - If an activity is selected, select the associated activity instances unless they are explicitly specified.
       *
       * @param  {Object} filter the filter to auto complete
       * @param  {Object} instanceIdToInstanceMap a activity instance id -> activity instance map
       * @param  {*} activityIdToInstancesMap a activity id -> activity instance map
       */
      function autoCompleteFilter(newFilter, instanceIdToInstanceMap, activityIdToInstancesMap) {
        var activityIds = newFilter.activityIds || [],
            activityInstanceIds = newFilter.activityInstanceIds || [],
            page = parseInt(newFilter.page, 10) || null,
            scrollToBpmnElement = newFilter.scrollToBpmnElement,
            // if filter has been changed from outside this component,
            // newFilter is different from cached filter
            externalUpdate = newFilter !== $scope.filter,
            completedFilter,
            replace = newFilter.replace;

        delete newFilter.replace;

        angular.forEach(activityInstanceIds, function(instanceId) {
          var instance = instanceIdToInstanceMap[instanceId] || {},
              activityId = instance.activityId || instance.targetActivityId,
              idx = activityIds.indexOf(activityId);

          if (idx === -1 && activityId) {
            activityIds.push(activityId);
          }
        });

        angular.forEach(activityIds, function(activityId) {
          var instanceList = activityIdToInstancesMap[activityId],
              foundOne = false,
              instanceIds = [];

          if (instanceList) {

            for (var i = 0, instance; (instance = instanceList[i]); i++) {
              var idx = activityInstanceIds.indexOf(instance.id);

              if (idx !== -1) {
                foundOne = true;
                break;
              }

              instanceIds.push(instance.id);
            }

            if (!foundOne) {
              activityInstanceIds = activityInstanceIds.concat(instanceIds);
            }
          }
        });

        // delete activity and activity instances which do not exist
        if (options.shouldRemoveActivityIds) {
          for(var i = 0; i < activityIds.length; i++) {
            if(!activityIdToInstancesMap[activityIds[i]]) {
              activityIds.splice(i, 1);
              i--;
            }
          }
        }
        for(i = 0; i < activityInstanceIds.length; i++) {
          if(!instanceIdToInstanceMap[activityInstanceIds[i]]) {
            activityInstanceIds.splice(i, 1);
            i--;
          }
        }

        if (activityIds.length > 0) {
          var newScrollTo = activityIds[activityIds.length - 1];

          if (newScrollTo !== scrollToBpmnElement) {
            scrollToBpmnElement = newScrollTo;
          }
        }

        completedFilter = {
          activityIds: activityIds,
          activityInstanceIds: activityInstanceIds,
          scrollToBpmnElement: scrollToBpmnElement,
          page: page
        };

        // update filter only if actual changes happened above
        // (auto completion took place)
        if (!angular.equals(completedFilter, $scope.filter)) {
          // update cached filters
          $scope.filter = completedFilter;

          // notify external components of filter change
          processData.set('filter', $scope.filter);
        }

        // update uri only if filter change is triggered from
        // external view component
        if (externalUpdate && $scope.filter) {
          // serialize filter to url
          serializeFilterToUri($scope.filter, replace);
        }
      }
    };
  }
];

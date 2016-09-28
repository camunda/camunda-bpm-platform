'use strict';

module.exports = ['$rootScope', function($rootScope) {
  var activeLoaders = [];
  var eventName = 'LoaderService:active-loaders-changed';

  return {
    startLoading: startLoading,
    addStatusListener: addStatusListener
  };

  /**
   * Starts new loading and return function that stop loading when called.
   *
   * @returns {stopLoading}
   */
  function startLoading() {
    var stopLoading = function() {
      activeLoaders = activeLoaders.filter(function(loader) {
        return loader !== stopLoading;
      });
      fireChanges();
    };

    activeLoaders.push(stopLoading);
    fireChanges();

    return stopLoading;
  }

  function fireChanges() {
    $rootScope.$broadcast(eventName);
  }

  /**
   * Adds new status listener, that will be called when loading status changes.
   * Returns function that removes listener.
   * Listeners uses $scope to listen to LoaderService:active-loaders-changed, so there is no need to manually
   * remove listener when $scope is destroyed.
   *
   * @param $scope
   * @param callback
   * @returns {*}
   */
  function addStatusListener($scope, callback) {
    callback(getLoadingStatus());

    return $scope.$on(eventName, function() {
      var status = getLoadingStatus();

      callback(status);
    });
  }

  function getLoadingStatus() {
    return activeLoaders.length === 0 ? 'LOADED' : 'LOADING';
  }
}];

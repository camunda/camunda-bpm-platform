/**
 *  Hack to support amd with angular-scenario
 *
 *  @see http://stackoverflow.com/questions/15499997/how-to-use-angular-scenario-with-requirejs
 */
(function() {
  var setUpAndRun = angular.scenario.setUpAndRun;

  angular.scenario.setUpAndRun = function(config) {
    amdSupport();
//        if (config.useamd) {
//            amdSupport();
//        }
    return setUpAndRun.apply(this, arguments);
  };

  function amdSupport() {
    var getFrame_ = angular.scenario.Application.prototype.getFrame_;

    /**
     *  This function should be added to angular-scenario to support amd. It overrides the load behavior to wait from
     *  the inner amd frame to be ready.
     */
    angular.scenario.Application.prototype.getFrame_ = function() {
      var frame = getFrame_.apply(this, arguments);
      var load = frame.load;

      frame.load = function(fn) {
        if (typeof fn === 'function') {
          angular.element(window).bind('message', function(e) {
            if (e.data && e.source === frame.prop('contentWindow') && e.data.type === 'loadamd') {
              fn.call(frame, e);
            }
          });
          return this;
        }
        return load.apply(this, arguments);
      }

      return frame;
    };
  }
})();
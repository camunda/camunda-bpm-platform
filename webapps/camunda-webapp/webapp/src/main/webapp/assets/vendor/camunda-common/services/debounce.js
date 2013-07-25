ngDefine('camunda.common.services.debounce', function(module) {

  var DebounceFactory = [ '$timeout', function($timeout) {

    /**
     * Debounce a function call, making it callable an arbitrary number of times before it is actually executed once.
     *
     * @param fn {function} the function to debounce
     * @param wait {number} the timeout after which the function is actually called
     *
     * @return {function} the function that can be called an arbitrary number of times
     *                    that will only be called when the wait interval elapsed
     */
    return function debounce(fn, wait) {
      var timer;

      return function() {
        var context = this,
            args = arguments;

        if (timer) {
          $timeout.cancel(timer);
        }

        timer = $timeout(function() {
          timer = null;
          fn.apply(context, args);
        }, wait);
      };
    };
  }];

  module.factory("debounce", DebounceFactory);
});

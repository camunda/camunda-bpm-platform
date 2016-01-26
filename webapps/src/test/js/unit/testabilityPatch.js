
define([ 'angular' ], function(angular) {
  /**
   * Here is the problem: http://bugs.jquery.com/ticket/7292
   * basically jQuery treats change event on some browsers (IE) as a
   * special event and changes it form 'change' to 'click/keydown' and
   * few others. This horrible hack removes the special treatment
   */
  if (window._jQuery) _jQuery.event.special.change = undefined;

  if (window.bindJQuery) bindJQuery();

  beforeEach(function() {
    // all this stuff is not needed for module tests, where jqlite and publishExternalAPI and jqLite are not global vars
    if (window.publishExternalAPI) {
      publishExternalAPI(angular);

      // workaround for IE bug https://plus.google.com/104744871076396904202/posts/Kqjuj6RSbbT
      // IE overwrite window.jQuery with undefined because of empty jQuery var statement, so we have to
      // correct this, but only if we are not running in jqLite mode
      if (!_jqLiteMode && _jQuery !== jQuery) {
        jQuery = _jQuery;
      }

      // This resets global id counter;
      uid = ['0', '0', '0'];

      // reset to jQuery or default to us.
      bindJQuery();
    }


    angular.element(document.body).html('').removeData();
  });

  afterEach(function() {
    if (this.$injector) {
      var $rootScope = this.$injector.get('$rootScope');
      var $rootElement = this.$injector.get('$rootElement');
      var $log = this.$injector.get('$log');
      // release the injector
      dealoc($rootScope);
      dealoc($rootElement);

      // check $log mock
      $log.assertEmpty && $log.assertEmpty();
    }

    // complain about uncleared jqCache references
    var count = 0;

    // This line should be enabled as soon as this bug is fixed: http://bugs.jquery.com/ticket/11775
    //var cache = jqLite.cache;
    var cache = angular.element.cache;

    forEachSorted(cache, function(expando, key){
      angular.forEach(expando.data, function(value, key){
        count ++;
        if (value.$element) {
          dump('LEAK', key, value.$id, sortedHtml(value.$element));
        } else {
          dump('LEAK', key, angular.toJson(value));
        }
      });
    });
    if (count) {
      throw new Error('Found jqCache references that were not deallocated! count: ' + count);
    }


    // copied from Angular.js
    // we need these two methods here so that we can run module tests with wrapped angular.js
    function sortedKeys(obj) {
      var keys = [];
      for (var key in obj) {
        if (obj.hasOwnProperty(key)) {
          keys.push(key);
        }
      }
      return keys.sort();
    }

    function forEachSorted(obj, iterator, context) {
      var keys = sortedKeys(obj);
      for ( var i = 0; i < keys.length; i++) {
        iterator.call(context, obj[keys[i]], keys[i]);
      }
      return keys;
    }
  });

  function dealoc(obj) {
    var jqCache = angular.element.cache;
    if (obj) {
      if (angular.isElement(obj)) {
        cleanup(angular.element(obj));
      } else {
        for(var key in jqCache) {
          var value = jqCache[key];
          if (value.data && value.data.$scope == obj) {
            delete jqCache[key];
          }
        }
      }
    }

    function cleanup(element) {
      element.unbind().removeData();
      for ( var i = 0, children = element.contents() || []; i < children.length; i++) {
        cleanup(angular.element(children[i]));
      }
    }
  }

  window.dealoc = dealoc;
});

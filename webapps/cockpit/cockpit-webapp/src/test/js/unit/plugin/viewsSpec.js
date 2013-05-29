define([ 'cockpit-plugin', 'jquery' ], function(p, $) {

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('plugin', function() {

    describe('view directive', function() {
      var element;

      function createElement(content) {
        return $(content).appendTo(document.body);
      }

      function putIntoCache(url, content) {
        return function($templateCache) {
          $templateCache.put(url, [200, content, {}]);
        };
      }

      afterEach(function() {
        $(document.body).html('');
        dealoc(element);
      });

      // load app that uses the directive
      beforeEach(module('cockpit.plugin'));

      it('initiates view from provider', inject(putIntoCache('myUrl', '<div>{{ variable }}</div>'), function($rootScope, $compile) {

        var called = false;
        var viewScope;

        $rootScope.myViewProvider = {
          controller: function($scope) {
            $scope.variable = 'myVar';

            called = true;
            viewScope = $scope;
          },
          url: 'myUrl'
        };

        element = createElement('<view provider="myViewProvider"></view>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        expect(element.text()).toBe('myVar');
        expect(called).toBe(true);

        expect(viewScope.$parent).toBe($rootScope);
      }));

      it('exposes read variables', inject(putIntoCache('myUrl', '<div>{{ variable }}</div>'), function($rootScope, $compile) {

        var viewScope;

        $rootScope.myViewProvider = {
          controller: function($scope) {
            viewScope = $scope;

            $scope.overrideVar = 'overridden';
          },
          url: 'myUrl'
        };

        $rootScope.vars = {
          read: [ 'simpleVar', 'complexVar', 'overrideVar' ]
        };

        $rootScope.simpleVar = 'simple';
        $rootScope.complexVar = {};
        $rootScope.overrideVar = 'original';

        element = createElement('<view vars="vars" provider="myViewProvider"></view>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        expect(viewScope.simpleVar).toBe($rootScope.simpleVar);
        expect(viewScope.complexVar).toBe($rootScope.complexVar);
        expect($rootScope.overrideVar).toBe('original');

        $rootScope.simpleVar = 'simple-changed';

        $rootScope.$digest();

        expect(viewScope.simpleVar).toBe($rootScope.simpleVar);
      }));

      it('exposes write variables', inject(putIntoCache('myUrl', '<div>{{ variable }}</div>'), function($rootScope, $compile) {

        var viewScope;

        $rootScope.myViewProvider = {
          controller: function($scope) {
            viewScope = $scope;

            $scope.simpleVar = 'simple';
            $scope.complexVar = {};
            $scope.nonWriteVar = 'original';
          },
          url: 'myUrl'
        };

        $rootScope.vars = {
          write: [ 'simpleVar', 'complexVar' ]
        };

        element = createElement('<view vars="vars" provider="myViewProvider"></view>');
        element = $compile(element)($rootScope);

        $rootScope.$digest();

        expect($rootScope.simpleVar).toBe(viewScope.simpleVar);
        expect($rootScope.complexVar).toBe(viewScope.complexVar);
        expect($rootScope.nonWriteVar).not.toBeDefined();

        viewScope.simpleVar = 'simple-changed';

        $rootScope.$digest();

        expect($rootScope.simpleVar).toBe(viewScope.simpleVar);
      }));
    });
  });
});
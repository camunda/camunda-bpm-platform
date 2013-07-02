define([ 'cockpit-plugin', 'angular', 'camunda-common/services/uri' ], function(p, angular) {

  /**
   * @see http://docs.angularjs.org/guide/dev_guide.unit-testing
   *      for how to write unit tests in AngularJS
   */
  return describe('plugins', function() {

    describe('ViewsService', function() {

      it('is configurable', function() {

        var viewProvider = {
          id: 'myViewProvider',
          controller: function($scope) {

          },
          url: 'myUrl'
        };

        var sampleModule = angular.module('my-module', [ 'cockpit.plugin', 'camunda.common.services.uri' ]);
        sampleModule.config(function(ViewsProvider) {
          expect(ViewsProvider).toBeDefined();

          ViewsProvider.registerDefaultView('some-view', viewProvider);
        });

        // bootstrap module
        module('my-module');

        inject(function(Views) {

          var provider = Views.getProvider({ component: 'some-view' });
          expect(provider).toBe(viewProvider);

        });
      });

      it('is overridable', function() {

        var defaultViewProvider = {
          id: 'myDefaultViewProvider',
          controller: function($scope) {

          },
          url: 'myUrl'
        };

        var overrideViewProvider = {
          id: 'myViewProvider',
          priority: 10,
          controller: function($scope) {

          },
          url: 'myUrl'
        };

        var sampleModule = angular.module('my-module', [ 'cockpit.plugin', 'camunda.common.services.uri' ]);
        sampleModule.config(function(ViewsProvider) {
          expect(ViewsProvider).toBeDefined();

          ViewsProvider.registerDefaultView('some-view', defaultViewProvider);
          ViewsProvider.registerView('some-view', overrideViewProvider);
        });

        // bootstrap module
        module('my-module');

        inject(function(Views) {

          var provider = Views.getProvider({ component: 'some-view' });
          expect(provider).toBe(overrideViewProvider);

        });
      });

      it('filters', function() {

        var viewProvider1 = {
          id: 'viewProvider1'
        };

        var viewProvider2 = {
          id: 'viewProvider2'
        };

        var sampleModule = angular.module('my-module', [ 'cockpit.plugin', 'camunda.common.services.uri' ]);
        sampleModule.config(function(ViewsProvider) {
          expect(ViewsProvider).toBeDefined();

          ViewsProvider.registerDefaultView('some-view', viewProvider1);
          ViewsProvider.registerDefaultView('some-view', viewProvider2);
        });

        // bootstrap module
        module('my-module');

        inject(function(Views) {

          var provider1 = Views.getProvider({ component: 'some-view', id: 'viewProvider1' });
          expect(provider1).toBe(viewProvider1);

          var provider2 = Views.getProvider({ component: 'some-view', id: 'viewProvider2' });
          expect(provider2).toBe(viewProvider2);

        });
      });
    });
  });
});
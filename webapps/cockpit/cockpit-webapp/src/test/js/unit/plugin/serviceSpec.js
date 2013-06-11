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

        var sampleModule = angular.module('my-module', [ 'cockpit.plugin', 'camunda.common.services' ]);
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

        var sampleModule = angular.module('my-module', [ 'cockpit.plugin', 'camunda.common.services' ]);
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
    });
  });
});
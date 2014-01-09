/**
 * bootstrap script of the cockpit application
 */

(function(document, window, require) {

  var baseUrl = '../../../';
  var APP_NAME = 'cockpit';
  var pluginPackages = window.PLUGIN_PACKAGES || [];

  /**
   * @see http://stackoverflow.com/questions/15499997/how-to-use-angular-scenario-with-requirejs
   */
  function ensureScenarioCompatibility() {

    var html = document.getElementsByTagName('html')[0];

    html.setAttribute('ng-app', APP_NAME);
    if (html.dataset) {
      html.dataset.ngApp = APP_NAME;
    }

    if (top !== window) {
      window.parent.postMessage({ type: 'loadamd' }, '*');
    }
  }

  /**
   * Bootstrap the angular application
   */
  function bootstrapApp(angular) {
    angular.bootstrap(document, [ APP_NAME ]);

    // ensure compatibility with scenario runner
    ensureScenarioCompatibility();
  }

  require([baseUrl +'require-conf'], function(rjsConf) {
    require({
      baseUrl: baseUrl,
      paths: rjsConf.paths,
      shim: rjsConf.shim,
      packages: rjsConf.packages.concat(pluginPackages)
    });

    require([
      'angular',
      'angular-resource',
      'angular-sanitize',
      'angular-ui',
      'ngDefine',
      'bootstrap',
      'jquery-ui'
    ], function(angular) {
      require([
        APP_NAME,
        'domReady!'
      ], function() {
        bootstrapApp(angular);
      });
    });
  });


})(document, window || this, require);

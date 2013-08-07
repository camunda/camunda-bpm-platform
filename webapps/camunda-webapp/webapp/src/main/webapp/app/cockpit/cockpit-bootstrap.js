/**
 * bootstrap script of the cockpit application
 */

(function(document, window, require) {

  var pluginPackages = window.PLUGIN_PACKAGES || [];

  require({
    baseUrl: '../../../',
    paths: {
      'ngDefine' : 'assets/vendor/requirejs-angular-define/ngDefine',
      'domReady' : 'assets/vendor/require/domReady',
      'jquery' : 'assets/vendor/jquery/jquery',
      'jquery-mousewheel' : 'assets/vendor/jquery/jquery.mousewheel',
      'jquery-overscroll' : 'assets/vendor/jquery/jquery.overscroll',
      'jquery-ui' : 'assets/vendor/jquery-ui/jquery-ui-1.10.3.custom.min',
      'bootstrap' : 'assets/vendor/bootstrap/js/bootstrap',
      'bootstrap-slider' : 'assets/vendor/bootstrap-slider/bootstrap-slider',
      'angular' : 'assets/vendor/angular/angular',
      'angular-resource' : 'assets/vendor/angular/angular-resource',
      'angular-sanitize' : 'assets/vendor/angular/angular-sanitize',
      'angular-cookies' : 'assets/vendor/angular/angular-cookies',
      'angular-data-depend' : 'assets/vendor/angular-data-depend/dataDepend'
    },
    shim: {
      'jquery-mousewheel' : { deps: [ 'jquery' ] },
      'jquery-overscroll' : { deps: [ 'jquery' ] },
      'jquery-ui' : { deps: [ 'jquery' ] },
      'bootstrap' : { deps: [ 'jquery' ] },
      'bootstrap-slider' : { deps: [ 'jquery' ] },
      'angular' : { deps: [ 'jquery' ], exports: 'angular' },
      'angular-resource': { deps: [ 'angular' ] },
      'angular-sanitize': { deps: [ 'angular' ] },
      'angular-cookies': { deps: [ 'angular' ] }
    },
    packages: [
      { name: 'cockpit', location: 'app/cockpit', main: 'cockpit' },
      { name: 'cockpit-plugin', location: 'app/plugin' },
      { name: 'camunda-common', location: 'assets/vendor/camunda-common' },
      { name: 'bpmn', location : 'assets/vendor/cabpmn' },
      { name: 'dojo', location : 'assets/vendor/dojo/dojo' },
      { name: 'dojox', location : 'assets/vendor/dojo/dojox' }
    ].concat(pluginPackages)
  });

  var APP_NAME = 'cockpit';

  /**
   *
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

  require([ 'angular', 'angular-resource', 'angular-sanitize', 'angular-cookies', 'ngDefine', 'bootstrap', 'jquery-ui' ], function(angular) {
    require([ APP_NAME, 'domReady!' ], function() {
      bootstrapApp(angular);
    });
  });

})(document, window || this, require);
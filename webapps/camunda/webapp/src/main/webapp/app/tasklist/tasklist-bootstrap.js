/**
 * bootstrap script of the tasklist application
 */

(function(document, window, require) {

  require({
    baseUrl: '../../../',
    paths: {
      'ngDefine' : 'assets/vendor/requirejs-angular-define/ngDefine',
      'domReady' : 'assets/vendor/require/domReady',
      'jquery' : 'assets/vendor/jquery/jquery',
      'bootstrap' : 'assets/vendor/bootstrap/js/bootstrap',
      'bootstrap-slider' : 'assets/vendor/bootstrap-slider/bootstrap-slider',
      'angular' : 'assets/vendor/angular/angular',
      'angular-resource' : 'assets/vendor/angular/angular-resource',
      'angular-sanitize' : 'assets/vendor/angular/angular-sanitize',
      'angular-cookies' : 'assets/vendor/angular/angular-cookies'
    },
    shim: {
      'bootstrap' : { deps: [ 'jquery' ] },
      'bootstrap-slider' : { deps: [ 'jquery' ] },
      'angular' : { deps: [ 'jquery' ], exports: 'angular' },
      'angular-resource': { deps: [ 'angular' ] },
      'angular-sanitize': { deps: [ 'angular' ] },
      'angular-cookies': { deps: [ 'angular' ] }
    },
    packages: [
      { name: 'tasklist', location: 'app/tasklist', main: 'tasklist' },
      { name: 'camunda-common', location: 'assets/vendor/camunda-common' },
      { name: 'bpmn', location : 'assets/vendor/cabpmn' },
      { name: 'dojo', location : 'assets/vendor/dojo/dojo' },
      { name: 'dojox', location : 'assets/vendor/dojo/dojox' }
    ]
  });

  var APP_NAME = 'tasklist';

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

  require([ 'angular', 'angular-resource', 'angular-sanitize', 'angular-cookies', 'ngDefine', 'bootstrap' ], function(angular) {
    require([ APP_NAME, 'domReady!' ], function() {
      bootstrapApp(angular);
    });
  });

})(document, window || this, require);
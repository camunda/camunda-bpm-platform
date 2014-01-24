(function(factory) {
  'use strict';
  if (typeof module !== 'undefined' && module.exports) {
    module.exports = factory();
  }
  else {
    define([], factory);
  }
}(function() {
  'use strict';
  /**
   * A UMD module that provides some configuration for
   * {@link http://requirejs.org/docs/api.html#config|require.js}.
   *
   * @module require-conf
   *
   * @example
   *    require(['../../require-conf'], function(rjsConf) {
   *
   *      // configure require.js...
   *      require(rjsConf);
   *
   *      // ...start loading dependencies...
   *      require([
   *        'backbone',
   *        'whatever'
   *      ], function(Backbone, Whatever) {
   *        // ...to do something
   *      });
   *    });
   *
   *
   * @property {string} conf.baseUrl
   *  For CommonJS modules (following the CommonJS scaffolding guid lines).
   *  {@link http://requirejs.org/docs/api.html#config-packages|See the require.js docs for packages configuration}
   *
   * @property {Object.<String, String>} conf.paths
   *  Keys are module names and values are paths or URLs.
   *  {@link http://requirejs.org/docs/api.html#config-paths|See the require.js docs for paths configuration}
   *
   * @property {Object.<String, (Object|Array)>} conf.shim
   *  Keys are module names and values are information on how to shim the modules.
   *  {@link http://requirejs.org/docs/api.html#config-shim|See the require.js docs for shim configuration}
   *
   * @property {Array.<Object>} conf.packages
   *  For CommonJS modules (following the CommonJS scaffolding guide lines).
   *  {@link http://requirejs.org/docs/api.html#config-packages|See the require.js docs for packages configuration}
   *
   * @property {object} conf.utils
   *  Holder for project setup utilities
   *
   * @property conf.utils.ensureScenarioCompatibility
   *  Utility to ensure compatibility of test scenarios loaded with require.js
   *  {@link http://stackoverflow.com/questions/15499997/how-to-use-angular-scenario-with-requirejs}
   * @static
   *
   * @property conf.utils.bootAngular
   *  Utility function to bootsrap angular applications
   * @param angular {Object} - angular, obviously
   * @param appName {string} - the package name of the application
   * @static
   */
  var conf = {};


  conf.baseUrl = '/camunda/',

  conf.paths = {
    'ngDefine':              'assets/vendor/requirejs-angular-define/src/ngDefine',
    'ngParse':               'assets/vendor/requirejs-angular-define/src/ngParse',
    'domReady':              'assets/vendor/requirejs-domready/domReady',
    'jquery':                'assets/vendor/jquery/jquery',
    'jquery-mousewheel':     'assets/vendor/jquery-mousewheel/jquery.mousewheel',
    'jquery-overscroll':     'assets/vendor/jquery-overscroll/src/jquery.overscroll',
    // 'jquery-ui':             'assets/vendor/jquery-ui/ui/jquery-ui',
    'jquery-ui':             'assets/vendor/jquery-ui/index',
    'bootstrap':             'assets/vendor/bootstrap/docs/assets/js/bootstrap',
    'bootstrap-slider':      'assets/vendor/bootstrap-slider/bootstrap-slider',
    'angular':               'assets/vendor/angular/index',
    'angular-resource':      'assets/vendor/angular-resource/index',
    'angular-sanitize':      'assets/vendor/angular-sanitize/index',
    'angular-ui':            'app/common/ui-bootstrap-dialog-tpls-0.5.0',
    'angular-data-depend':   'assets/vendor/angular-data-depend/src/dataDepend'
  };

  conf.shim = {
    'jquery-mousewheel' :     ['jquery'],
    'jquery-overscroll' :     ['jquery'],
    'jquery-ui' :             ['jquery'],
    'bootstrap' :             ['jquery'],
    'bootstrap-slider' :      ['jquery'],
    'angular' :               {
                                deps: ['jquery'],
                                exports: 'angular'
                              },
    'angular-resource':       ['angular'],
    'angular-sanitize':       ['angular'],
    'angular-ui':             ['angular']
  };

  conf.packages = [
    {
      name: 'admin',
      location: 'app/admin',
      main: 'admin'
    },
    {
      name: 'cockpit',
      location: 'app/cockpit',
      main: 'cockpit'
    },
    {
      name: 'tasklist',
      location: 'app/tasklist',
      main: 'tasklist'
    },
    {
      name: 'cockpit-plugin',
      location: 'app/plugin'
    },

    {
      name: 'camunda-common',
      location: 'app/common'
    },

    {
      name: 'bpmn',
      location : 'assets/vendor/camunda-bpmn.js/src/bpmn'
    },
    {
      name: 'dojo',
      location : 'assets/vendor/dojo/dojo'
    },
    {
      name: 'dojox',
      location : 'assets/vendor/dojo/dojox'
    }
  ];

  conf.utils = {};

  function ensureScenarioCompatibility(appName) {
    var html = document.getElementsByTagName('html')[0];

    html.setAttribute('ng-app', appName);
    if (html.dataset) {
      html.dataset.ngApp = appName;
    }

    if (top !== window) {
      window.parent.postMessage({ type: 'loadamd' }, '*');
    }
  };
  conf.utils.ensureScenarioCompatibility = ensureScenarioCompatibility;

  conf.utils.bootAngular = function(angular, appName) {
    angular.bootstrap(document, [ appName ]);

    ensureScenarioCompatibility(appName);
  };

  /* live-reload
  // loads livereload client library (without breaking other scripts execution)
  require(['jquery'], function($) {
    $('body').append('<script src="//localhost:LIVERELOAD_PORT/livereload.js?snipver=1"></script>');
  });
  /* */

  return conf;
}));

module.exports = function(config, requireJsConfig) {
  'use strict';
  var commons = require('./../../../camunda-commons-ui');
  var _ = commons.utils._;
  var rjsConf = commons.requirejs();

  var deps = [
    'requirejs',
    'camunda-commons-ui',
    'angular-resource',
    'angular-sanitize',
    'angular-route',
    'angular-bootstrap'
  ];

  var options = {
      stubModules: [
        'json',
        'text'
      ],

      preserveLicenseComments: false,
      generateSourceMaps: true,

      baseUrl: './<%= pkg.gruntConfig.tasklistSourceDir %>',

      paths: _.extend(rjsConf.paths, {
        'camunda-tasklist-ui': 'scripts/camunda-tasklist-ui',
      }),

      shim: _.extend(rjsConf.shim, {}),

      packages: rjsConf.packages.concat([
        {
          name: 'camunda-commons-ui/auth',
          main: 'index'
        },
        {
          name: 'api',
          main: 'index'
        },
        {
          name: 'process',
          main: 'index'
        },
        {
          name: 'filter',
          main: 'index'
        },
        {
          name: 'tasklist',
          main: 'index'
        },
        {
          name: 'task',
          main: 'index'
        },
        {
          name: 'variable',
          main: 'index'
        },
        {
          name: 'user',
          main: 'index'
        },
        {
          name: 'widgets',
          main: 'index'
        },
        {
          name: 'form',
          main: 'index'
        }
      ])
  };

  requireJsConfig.tasklist_dependencies = {
    options: _.merge({}, options, {
        create: true,
        name: 'camunda-tasklist-ui-deps',
        out: '<%= pkg.gruntConfig.tasklistBuildTarget %>/scripts/deps.js',
        include: deps
    })
  };

  requireJsConfig.tasklist_scripts = {
    options: _.merge({}, options, {
        name: 'camunda-tasklist-ui',
        out: '<%= pkg.gruntConfig.tasklistBuildTarget %>/scripts/camunda-tasklist-ui.js',
        exclude: deps,
        include: [
          'scripts/config/date',
          'scripts/config/routes',
          'scripts/config/locales',
          'scripts/config/tooltip',
          'scripts/config/uris',

          'scripts/controller/cam-tasklist-app-ctrl',
          'scripts/controller/cam-tasklist-view-ctrl',
          'scripts/services/cam-tasklist-assign-notification',
          'scripts/services/cam-tasklist-configuration',

          'scripts/user/index',
          'scripts/variable/index',
          'scripts/tasklist/index',
          'scripts/task/index',
          'scripts/process/index',
          'scripts/navigation/index',
          'scripts/form/index',
          'scripts/filter/index',
          'scripts/api/index'
        ],

        onModuleBundleComplete: commons.livereloadSnippet(config.grunt)
    })
  };
};

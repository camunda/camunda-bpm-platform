/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var child_process = require('child_process');
var fs = require('fs');

module.exports = function(grunt) {
  'use strict';

  require('load-grunt-tasks')(grunt);
  require('./grunt/tasks/terser')(grunt);

  var pkg = require('./package.json');
  var protractorConfig =
    grunt.option('protractorConfig') || 'ui/common/tests/ci.conf.js';

  var config = pkg.gruntConfig || {};

  config.grunt = grunt;
  config.pkg = pkg;
  config.protractorConfig = protractorConfig;

  var browserifyConf = {};

  require('./ui/welcome/grunt/config/browserify')(config, browserifyConf);
  require('./ui/admin/grunt/config/browserify')(config, browserifyConf);
  require('./ui/tasklist/grunt/config/browserify')(config, browserifyConf);
  require('./ui/cockpit/grunt/config/browserify')(config, browserifyConf);
  require('./camunda-bpm-sdk-js/grunt/config/browserify')(
    config,
    browserifyConf
  );

  var copyConf = require('./grunt/config/copy');
  require('./ui/welcome/grunt/config/copy')(config, copyConf);
  require('./ui/admin/grunt/config/copy')(config, copyConf);
  require('./ui/cockpit/grunt/config/copy')(config, copyConf);
  require('./ui/tasklist/grunt/config/copy')(config, copyConf);
  require('./camunda-bpm-sdk-js/grunt/config/copy')(config, copyConf);

  var lessConf = {};
  require('./grunt/config/less')(config, lessConf, {
    appName: 'welcome',
    sourceDir: pkg.gruntConfig.welcomeSourceDir,
    buildTarget: pkg.gruntConfig.welcomeBuildTarget
  });

  require('./grunt/config/less')(config, lessConf, {
    appName: 'admin',
    sourceDir: pkg.gruntConfig.adminSourceDir,
    buildTarget: pkg.gruntConfig.adminBuildTarget
  });
  require('./grunt/config/less')(config, lessConf, {
    appName: 'admin',
    sourceDir: pkg.gruntConfig.pluginSourceDir + '/admin/plugins',
    buildTarget: pkg.gruntConfig.pluginBuildTarget + '/admin/app',
    plugin: true
  });

  require('./grunt/config/less')(config, lessConf, {
    appName: 'cockpit',
    sourceDir: pkg.gruntConfig.cockpitSourceDir,
    buildTarget: pkg.gruntConfig.cockpitBuildTarget
  });
  require('./grunt/config/less')(config, lessConf, {
    appName: 'cockpit',
    sourceDir: pkg.gruntConfig.pluginSourceDir + '/cockpit/plugins',
    buildTarget: pkg.gruntConfig.pluginBuildTarget + '/cockpit/app',
    plugin: true
  });

  require('./grunt/config/less')(config, lessConf, {
    appName: 'tasklist',
    sourceDir: pkg.gruntConfig.tasklistSourceDir,
    buildTarget: pkg.gruntConfig.tasklistBuildTarget
  });
  require('./grunt/config/less')(config, lessConf, {
    appName: 'tasklist',
    sourceDir: pkg.gruntConfig.pluginSourceDir + '/tasklist/plugins',
    buildTarget: pkg.gruntConfig.pluginBuildTarget + '/tasklist/app',
    plugin: true
  });

  require('./grunt/config/less')(config, lessConf, {
    appName: 'commons-ui',
    sourceDir: 'camunda-commons-ui/resources/less/',
    buildTarget: 'camunda-commons-ui'
  });

  var localesConf = {};
  require('./grunt/config/localescompile')(config, localesConf, {
    appName: 'tasklist',
    sourceDir: pkg.gruntConfig.tasklistSourceDir,
    buildTarget: pkg.gruntConfig.tasklistBuildTarget
  });

  require('./grunt/config/localescompile')(config, localesConf, {
    appName: 'welcome',
    sourceDir: pkg.gruntConfig.welcomeSourceDir,
    buildTarget: pkg.gruntConfig.welcomeBuildTarget
  });

  require('./grunt/config/localescompile')(config, localesConf, {
    appName: 'admin',
    sourceDir: pkg.gruntConfig.adminSourceDir,
    buildTarget: pkg.gruntConfig.adminBuildTarget
  });

  require('./grunt/config/localescompile')(config, localesConf, {
    appName: 'cockpit',
    sourceDir: pkg.gruntConfig.cockpitSourceDir,
    buildTarget: pkg.gruntConfig.cockpitBuildTarget
  });

  var watchConf = {
    commons_styles: {
      options: {
        liverreload: false
      },
      files: [
        'ui/common/styles/**/*.less',
        'node_modules/camunda-commons-ui/{lib,resources}/**/*.less'
      ],
      tasks: ['less']
    }
  };
  require('./ui/welcome/grunt/config/watch')(config, watchConf);
  require('./ui/tasklist/grunt/config/watch')(config, watchConf);
  require('./ui/cockpit/grunt/config/watch')(config, watchConf);
  require('./ui/admin/grunt/config/watch')(config, watchConf);
  require('./ui/common/grunt/config/watch')(config, watchConf);

  var uglifyConf = {};
  require('./grunt/config/uglify')(config, uglifyConf);
  require('./ui/welcome/grunt/config/uglify')(config, uglifyConf);
  require('./ui/admin/grunt/config/uglify')(config, uglifyConf);
  require('./ui/tasklist/grunt/config/uglify')(config, uglifyConf);
  require('./ui/cockpit/grunt/config/uglify')(config, uglifyConf);
  require('./camunda-bpm-sdk-js/grunt/config/uglify')(config, uglifyConf);

  var eslintConf = {};
  require('./ui/welcome/grunt/config/eslint')(config, eslintConf);
  require('./ui/admin/grunt/config/eslint')(config, eslintConf);
  require('./ui/tasklist/grunt/config/eslint')(config, eslintConf);
  require('./ui/cockpit/grunt/config/eslint')(config, eslintConf);
  require('./ui/common/grunt/config/eslint')(config, eslintConf);
  require('./camunda-bpm-sdk-js/grunt/config/eslint')(config, eslintConf);

  var stylelintConf = {};
  require('./ui/welcome/grunt/config/stylelint')(config, stylelintConf);
  require('./ui/admin/grunt/config/stylelint')(config, stylelintConf);
  require('./ui/tasklist/grunt/config/stylelint')(config, stylelintConf);
  require('./ui/common/grunt/config/stylelint')(config, stylelintConf);
  require('./camunda-commons-ui/grunt/config/stylelint')(config, stylelintConf);

  require('./grunt/tasks/license-check')(grunt);

  grunt.initConfig({
    buildMode: 'dev',

    pkg: pkg,

    browserify: browserifyConf,

    persistify: browserifyConf,

    copy: copyConf,

    less: lessConf,

    localescompile: localesConf,

    clean: require('./grunt/config/clean')(config),

    watch: watchConf,

    eslint: eslintConf,

    stylelint: stylelintConf,

    terser: uglifyConf,

    ensureLibs: {
      thirdParty: {}
    },

    protractor: require('./grunt/config/protractor')(config),

    karma: {
      unit: {
        configFile: './ui/common/unit-tests/karma.conf.js',
        singleRun: process.env.KARMA_SINGLE_RUN || false,
        client: {
          mocha: {
            timeout: 10000
          }
        }
      }
    }
  });

  require('./grunt/tasks/license-header')(grunt, false);
  require('./camunda-commons-ui/grunt/tasks/localescompile')(grunt);
  var licensebookConfig = {
    enabled: false,
    includedFiles: new Set()
  };

  require('./camunda-commons-ui/grunt/tasks/persistify')(
    grunt,
    __dirname,
    licensebookConfig
  );
  require('./camunda-commons-ui/grunt/tasks/ensureLibs')(
    grunt,
    __dirname,
    licensebookConfig
  );

  grunt.loadNpmTasks('grunt-karma');

  grunt.registerTask('build-sdk-js', [
    'eslint:sdk-js',
    'clean:sdk-js',
    'copy:sdk-js',
    'browserify:sdk-js',
    'terser:sdk-js'
  ]);

  grunt.registerTask('build', function(mode, app) {
    if (typeof app !== 'undefined') {
      console.log(' ------------  will build ' + app + ' -------------');
      var objs = [
        browserifyConf,
        copyConf,
        lessConf,
        localesConf,
        watchConf,
        uglifyConf,
        eslintConf,
        stylelintConf
      ];
      for (var i = 0; i < objs.length; i++) {
        var obj = objs[i];
        for (var key in obj) {
          if (
            obj.hasOwnProperty(key) &&
            key.toLowerCase().indexOf(app) === -1 &&
            key !== 'options' &&
            key.toLowerCase().indexOf('webapp') === -1 &&
            key.toLowerCase().indexOf('sdk') === -1
          ) {
            delete obj[key];
          }
        }
      }
    }

    grunt.config.data.buildMode = mode || 'prod';

    var tasksToRun = [];

    tasksToRun.push('license-check');
    if (grunt.config.data.buildMode === 'prod') {
      tasksToRun.push('eslint');
    } else {
      grunt.loadNpmTasks('grunt-stylelint');
      tasksToRun.push('newer:eslint', 'stylelint');
    }

    tasksToRun.push('clean', 'ensureLibs', 'persistify', 'copy', 'less');

    if (
      typeof app === 'undefined' ||
      app === 'tasklist' ||
      app === 'cockpit' ||
      app === 'admin'
    ) {
      tasksToRun.push('localescompile');
    }

    if (grunt.config.data.buildMode === 'prod') {
      tasksToRun.push('terser');
    }

    tasksToRun.push('license-header');

    grunt.task.run(tasksToRun);
  });

  grunt.registerTask('auto-build', function(app) {
    if (app) {
      grunt.task.run(['build:dev:' + app, 'watch']);
    } else {
      grunt.task.run(['build:dev', 'watch']);
    }
  });

  grunt.registerTask('ensureSelenium', function() {
    // set correct webdriver version
    var done, path;

    if (
      fs.existsSync(
        'node_modules/grunt-protractor-runner/node_modules/protractor/config.json'
      )
    ) {
      // npm 2
      path = 'node_modules/grunt-protractor-runner/node_modules/protractor/';
    } else if (fs.existsSync('node_modules/protractor/config.json')) {
      // npm 3+
      path = 'node_modules/protractor/';
    }

    fs.writeFileSync(
      path + 'config.json',
      '    {\n' +
        '      "webdriverVersions": {\n' +
        '        "selenium": "2.47.1",\n' +
        '        "chromedriver": "2.24",\n' +
        '        "iedriver": "2.47.0"\n' +
        '      }\n' +
        '    }'
    );

    // async task
    done = this.async();

    child_process.execFile(
      'node',
      [__dirname + '/' + path + 'bin/webdriver-manager', '--chrome', 'update'],
      function(err) {
        done();
      }
    );
  });

  grunt.registerTask('default', ['build']);

  grunt.registerTask('test-e2e', ['ensureSelenium', 'protractor:e2e']);
};

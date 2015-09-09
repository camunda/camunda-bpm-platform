module.exports = function(grunt) {
  'use strict';

  require('load-grunt-tasks')(grunt);

  var pkg = require('./package.json');
  var protractorConfig = grunt.option('protractorConfig') || 'src/test/js/e2e/ci.conf.js';

  var config = pkg.gruntConfig || {};

  config.grunt = grunt;
  config.pkg = pkg;
  config.protractorConfig = protractorConfig;

  var requireJsConf = {
    options: {
      optimize: '<%= (buildMode === "prod" ? "uglify2" : "none") %>',
    }
  };

  require('./grunt/config/requirejs')(config, requireJsConf);
  require('camunda-admin-ui/grunt/config/requirejs')(config, requireJsConf);
  require('camunda-cockpit-ui/grunt/config/requirejs')(config, requireJsConf);
  require('camunda-tasklist-ui/grunt/config/requirejs')(config, requireJsConf);

  var copyConf = { };
  require('camunda-admin-ui/grunt/config/copy')(config, copyConf);
  require('camunda-cockpit-ui/grunt/config/copy')(config, copyConf);
  require('camunda-tasklist-ui/grunt/config/copy')(config, copyConf);

  var lessConf = { };
  require('camunda-commons-ui/grunt/config/less')(config, lessConf, {
    appName: 'admin',
    sourceDir: pkg.gruntConfig.adminSourceDir,
    buildTarget: pkg.gruntConfig.adminBuildTarget,
  });
  require('camunda-commons-ui/grunt/config/less')(config, lessConf, {
    appName: 'cockpit',
    sourceDir: pkg.gruntConfig.cockpitSourceDir,
    buildTarget: pkg.gruntConfig.cockpitBuildTarget,
  });
  require('camunda-commons-ui/grunt/config/less')(config, lessConf, {
    appName: 'tasklist',
    sourceDir: pkg.gruntConfig.tasklistSourceDir,
    buildTarget: pkg.gruntConfig.tasklistBuildTarget,
  });

  var localesConf = { };
  require('camunda-commons-ui/grunt/config/localescompile')(config, localesConf, {
    appName: 'tasklist',
    sourceDir: pkg.gruntConfig.tasklistSourceDir,
    buildTarget: pkg.gruntConfig.tasklistBuildTarget,
  });

  var watchConf = { };
  require('./grunt/config/watch')(config, watchConf);
  require('camunda-tasklist-ui/grunt/config/watch')(config, watchConf);
  require('camunda-cockpit-ui/grunt/config/watch')(config, watchConf);
  require('camunda-admin-ui/grunt/config/watch')(config, watchConf);

  // watch the SDK
  watchConf.watchSdk = {
    options: {
      livereload: false
    },
    files: ['node_modules/camunda-commons-ui/node_modules/camunda-bpm-sdk-js/lib/**/*.js'],
    tasks: ['grunt:buildSdk']
  };

  var gruntConf = {
    buildSdk: {
      gruntfile: 'node_modules/camunda-commons-ui/node_modules/camunda-bpm-sdk-js/Gruntfile.js',
      tasks: ['browserify:distAngular', 'browserify:distTypeUtils']
    }
  };


  grunt.initConfig({
    buildMode:        'dev',

    pkg:              pkg,

    requirejs:        requireJsConf,

    copy:             copyConf,

    less:             lessConf,

    localescompile:   localesConf,

    clean:            require('./grunt/config/clean')(config),

    watch:            watchConf,

    protractor:       require('./grunt/config/protractor')(config),

    grunt:            gruntConf
  });

  require('camunda-commons-ui/grunt/tasks/localescompile')(grunt);

  grunt.registerTask('build', function(mode, app) {


    if(typeof app !== 'undefined') {
      console.log(' ------------  will build ' + app + ' -------------');
      var objs = [requireJsConf, copyConf, lessConf, localesConf, watchConf, gruntConf];
      for(var i = 0; i < objs.length; i++) {
        var obj = objs[i];
        for (var key in obj) {
          if (obj.hasOwnProperty(key) && key.toLowerCase().indexOf(app) === -1 && key !== 'options' &&
                                         key.toLowerCase().indexOf('webapp') === -1 &&
                                         key.toLowerCase().indexOf('Sdk') === -1) {
              delete obj[key];
          }
        }
      }
    }

    grunt.config.data.buildMode = mode || 'prod';

    if(typeof app !== 'undefined' && app !== 'tasklist') {
      grunt.task.run([
        'clean',
        'grunt',
        'requirejs',
        'copy',
        'less'
      ]);
    } else {
      grunt.task.run([
        'clean',
        'grunt',
        'requirejs',
        'copy',
        'localescompile',
        'less'
      ]);
    }

  });

  grunt.registerTask('auto-build', function(app) {
    if(app) {
      grunt.task.run([
        'build:dev:' + app,
        'watch'
      ]);
    } else {
      grunt.task.run([
        'build:dev',
        'watch'
      ]);
    }
  });

  grunt.registerTask('default', ['build']);

  grunt.registerTask('test-e2e', ['protractor:e2e']);
};

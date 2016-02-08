var child_process = require('child_process');

module.exports = function(grunt) {
  'use strict';

  require('load-grunt-tasks')(grunt);

  var pkg = require('./package.json');
  var protractorConfig = grunt.option('protractorConfig') || 'ui/common/tests/ci.conf.js';

  var config = pkg.gruntConfig || {};

  config.grunt = grunt;
  config.pkg = pkg;
  config.protractorConfig = protractorConfig;

  var browserifyConf = { };

  require('./ui/admin/grunt/config/browserify')(config, browserifyConf);
  require('./ui/tasklist/grunt/config/browserify')(config, browserifyConf);
  require('./ui/cockpit/grunt/config/browserify')(config, browserifyConf);

  var copyConf = require('./grunt/config/copy');
  require('./ui/admin/grunt/config/copy')(config, copyConf);
  require('./ui/cockpit/grunt/config/copy')(config, copyConf);
  require('./ui/tasklist/grunt/config/copy')(config, copyConf);

  var lessConf = { };
  require('./grunt/config/less')(config, lessConf, {
    appName: 'admin',
    sourceDir: pkg.gruntConfig.adminSourceDir,
    buildTarget: pkg.gruntConfig.adminBuildTarget,
  });
  require('./grunt/config/less')(config, lessConf, {
    appName: 'cockpit',
    sourceDir: pkg.gruntConfig.cockpitSourceDir,
    buildTarget: pkg.gruntConfig.cockpitBuildTarget,
  });
  require('./grunt/config/less')(config, lessConf, {
    appName: 'tasklist',
    sourceDir: pkg.gruntConfig.tasklistSourceDir,
    buildTarget: pkg.gruntConfig.tasklistBuildTarget,
  });

  var localesConf = { };
  require('./grunt/config/localescompile')(config, localesConf, {
    appName: 'tasklist',
    sourceDir: pkg.gruntConfig.tasklistSourceDir,
    buildTarget: pkg.gruntConfig.tasklistBuildTarget,
  });

  var watchConf = {
    commons_styles: {
      options: {
        liverreload: false,
      },
      files: [
        'ui/common/styles/**/*.less',
        'node_modules/camunda-commons-ui/{lib,resources}/**/*.less'
      ],
      tasks: ['less']
    }
  };
  require('./ui/tasklist/grunt/config/watch')(config, watchConf);
  require('./ui/cockpit/grunt/config/watch')(config, watchConf);
  require('./ui/admin/grunt/config/watch')(config, watchConf);

  var uglifyConf = {};
  require('./grunt/config/uglify')(config, uglifyConf);
  require('./ui/admin/grunt/config/uglify')(config, uglifyConf);
  require('./ui/tasklist/grunt/config/uglify')(config, uglifyConf);
  require('./ui/cockpit/grunt/config/uglify')(config, uglifyConf);


  grunt.initConfig({
    buildMode:        'dev',

    pkg:              pkg,

    browserify:       browserifyConf,

    copy:             copyConf,

    less:             lessConf,

    localescompile:   localesConf,

    uglify:           uglifyConf,

    clean:            require('./grunt/config/clean')(config),

    watch:            watchConf,

    protractor:       require('./grunt/config/protractor')(config)
  });

  require('camunda-commons-ui/grunt/tasks/localescompile')(grunt);

  grunt.registerTask('build', function(mode, app) {


    if(typeof app !== 'undefined') {
      console.log(' ------------  will build ' + app + ' -------------');
      var objs = [browserifyConf, copyConf, lessConf, localesConf, watchConf, uglifyConf];
      for(var i = 0; i < objs.length; i++) {
        var obj = objs[i];
        for (var key in obj) {
          if (obj.hasOwnProperty(key) && key.toLowerCase().indexOf(app) === -1 && key !== 'options' &&
                                         key.toLowerCase().indexOf('webapp') === -1 &&
                                         key.toLowerCase().indexOf('sdk') === -1) {
              delete obj[key];
          }
        }
      }
    }

    grunt.config.data.buildMode = mode || 'prod';

    var tasksToRun = [
      'clean',
      'browserify',
      'copy',
      'less'
    ];

    if(typeof app === 'undefined' || app === 'tasklist') {
      tasksToRun.push('localescompile');
    }

    if(grunt.config.data.buildMode === 'prod') {
      tasksToRun.push('uglify');
    }

    grunt.task.run(tasksToRun);

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

  grunt.registerTask('ensureSelenium', function() {
    // async task
    var done = this.async();

    child_process.execFile('node', [__dirname + '/node_modules/grunt-protractor-runner/node_modules/protractor/bin/webdriver-manager', '--chrome', 'update'], function(err) {
      done();
    });
  });

  grunt.registerTask('default', ['build']);

  grunt.registerTask('test-e2e', ['ensureSelenium', 'protractor:e2e']);
};

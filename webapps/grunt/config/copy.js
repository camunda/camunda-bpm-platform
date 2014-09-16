module.exports = function(config) {
  'use strict';
  var grunt = config.grunt;
  var productionRemoveExp = /<!-- #production-remove.*\/production-remove -->/igm;
  var prod = grunt.option('target') === 'dist';


  function productionRemove(content) {
    if (!prod) { return content; }
    grunt.log.writeln('Removing development snippets');
    return content.replace(productionRemoveExp, '');
  }


  function livereloadPort(content, srcpath) {
    if (srcpath.slice(-4) !== 'html' || prod) {
      return content;
    }

    grunt.log.writeln('Replacing "LIVERELOAD_PORT" with "'+ config.livereloadPort +'"');
    return content.replace('LIVERELOAD_PORT', config.livereloadPort);
  }


  function appConf(content, srcpath) {
    if (srcpath.slice(-4) !== 'html') { return content; }

    var tasklistConf = 'var tasklistConf = '+ JSON.stringify({
      apiUri: '/camunda/api/engine',
      mock: false,

      // overrides the settings above
      resources: {
      }
    }, null, 2) +';';

    grunt.log.writeln('Wrote application configuration');
    return content.replace('var tasklistConf = {};', tasklistConf);
  }


  function copyReplace(content, srcpath) {

    content = productionRemove(content, srcpath);
    content = appConf(content, srcpath);
    content = livereloadPort(content, srcpath);
    return content;
  }

  return {
    options: {},

    index: {
      options: {
        process: copyReplace
      },
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>',
          src: [
            'index.html'
          ],
          dest: '<%= buildTarget %>/',
        }
      ]
    },

    assets: {
      files: [
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>',
          src: [
            '*.{ico,txt}'
          ],
          dest: '<%= buildTarget %>/',
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/fonts',
          src: ['*/*.{eot,svg,ttf,woff}'],
          dest: '<%= buildTarget %>/fonts/'
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/bower_components/bootstrap/fonts',
          src: ['**'],
          dest: '<%= buildTarget %>/fonts/bootstrap/'
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/images',
          src: ['**'],
          dest: '<%= buildTarget %>/images/'
        }
      ]
    },

    sdk: {
      files: [
        {
          expand: true,
          cwd: 'node_modules/camunda-commons-ui',
          src: [
            'lib/**/*.js',
            'lib/*.js',
            // 'resources/locales/**/*.json',
            // 'resources/locales/*.json'
          ],
          dest: '<%= buildTarget %>/vendor/camunda-commons-ui'
        },
        {
          expand: true,
          cwd: 'node_modules/camunda-bpm-sdk-js/dist',
          src: ['**/*.js'],
          dest: '<%= pkg.gruntConfig.clientDir %>/vendor/'
        // },
        // {
        //   expand: true,
        //   cwd: 'node_modules/camunda-bpm-sdk-js/dist',
        //   src: ['**/*.js'],
        //   // dest: '<%= pkg.gruntConfig.clientDir %>/vendor/'
        //   dest: '<%= buildTarget %>/vendor/'
        }
      ]
    }
  };
};

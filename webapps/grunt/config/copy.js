module.exports = function(config) {
  var grunt = config.grunt;
  var productionRemoveExp = /<!-- #production-remove.*\/production-remove -->/igm;
  var prod = grunt.option('target') === 'dist';


  function productionRemove(content, srcpath) {
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
      mock: true,
      // overrides the settings above
      resources: {
        'process-definition': {
          mock: false
        }
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
    assets: {
      options: {
        process: copyReplace
      },
      files: [
        {
          expand: true,
          cwd: 'client',
          src: [
            '*.{ico,txt}',
            'index.html'
          ],
          dest: '<%= buildTarget %>/',
        },
        {
          expand: true,
          cwd: 'client/fonts',
          src: ['*/*.{eot,svg,ttf,woff}'],
          dest: '<%= buildTarget %>/fonts/'
        },
        {
          expand: true,
          cwd: 'client/bower_components/bootstrap/fonts',
          src: ['**'],
          dest: '<%= buildTarget %>/fonts/bootstrap/'
        },
        {
          expand: true,
          cwd: 'client/images',
          src: ['**'],
          dest: '<%= buildTarget %>/images/'
        // },
        // // -----------------------
        // {
        //   expand: true,
        //   cwd: 'client/scripts',
        //   src: ['**/*.{jpg,png,gif,webp}'],
        //   dest: '<%= buildTarget %>/scripts/'
        }
      ]
    },
    sdk: {
      files: [
        {
          src: 'node_modules/camunda-bpm-sdk-js/dist/camunda-bpm-sdk.js',
          dest: 'client/bower_components/camunda-bpm-sdk-js/index.js'
        },
        {
          src: 'node_modules/camunda-bpm-sdk-js/dist/camunda-bpm-sdk-mock.js',
          dest: 'client/bower_components/camunda-bpm-sdk-js-mock/index.js'
        }
      ]
    }
  };
};

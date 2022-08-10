const {CleanWebpackPlugin} = require('clean-webpack-plugin');
const HtmlWebPackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

const path = require('path');
const webpack = require('webpack');

let jsLoaders = ['babel-loader'];
let entry = {
  /* Cockpit */
  "camunda/app/cockpit/camunda-cockpit-ui": {
    import: './ui/cockpit/client/scripts/camunda-cockpit-ui.js',
    dependOn: 'camunda/lib/deps'
  },
  "camunda/app/cockpit/plugin": {
    import: './ui/cockpit/plugins/cockpitPlugins.js',
    dependOn: 'camunda/lib/deps'
  },

  /* Tasklist */
  "camunda/app/tasklist/camunda-tasklist-ui": {
    import: './ui/tasklist/client/scripts/camunda-tasklist-ui.js',
    dependOn: 'camunda/lib/deps'
  },
  "camunda/app/tasklist/tasklistPlugins": {
    import: './ui/tasklist/plugins/tasklistPlugins.js',
    dependOn: 'camunda/lib/deps'
  },

  /* Admin */
  "camunda/app/admin/camunda-admin-ui": {
    import: './ui/admin/client/scripts/camunda-admin-ui.js',
    dependOn: 'camunda/lib/deps'
  },
  "camunda/app/admin/adminPlugins": {
    import: './ui/admin/plugins/adminPlugins.js',
    dependOn: 'camunda/lib/deps'
  },

  /* Welcome */
  "camunda/app/welcome/camunda-welcome-ui": {
    import: './ui/welcome/client/scripts/camunda-welcome-ui.js',
    dependOn: 'camunda/lib/deps'
  },

  /* Shared */
  "camunda/lib/jquery": {
    import: './ui/common/lib/jquery.js'
  },
  "camunda/lib/ngDefine": {
    import: './ui/common/lib/ngDefine.js',
    dependOn: 'camunda/lib/deps'
  },
  "camunda/lib/requirejs": {
    import: './ui/common/lib/requirejs.js',
    dependOn: 'camunda/lib/deps'
  },
  "camunda/lib/deps": [
    "angular",
    "moment",
    "camunda-bpm-sdk-js/lib/angularjs/index",
    "camunda-bpm-sdk-js",
    "q",
    "angular-animate",
    "angular-cookies",
    "angular-data-depend",
    "angular-loader",
    "angular-mocks",
    "angular-moment",
    "angular-resource",
    "angular-route",
    "angular-sanitize",
    "angular-touch",
    "angular-translate",
    "bpmn-js",
    "bpmn-js/lib/NavigatedViewer",
    "core-js",
    "@bpmn-io/form-js",
    "@bpmn-io/form-js-editor",
    "@bpmn-io/form-js-viewer",
    "@bpmn-io/dmn-migrate",
    "dmn-moddle",
    "clipboard",
    "events",
    "dom4",
    "cmmn-js",
    "dmn-js",
    "bootstrap",
    "lodash",
    "dmn-js-shared/lib/base/Manager",
    "dmn-js-drd/lib/NavigatedViewer",
    "dmn-js-decision-table/lib/Viewer",
    "dmn-js-literal-expression/lib/Viewer",
    "dmn-js-shared/lib/util/ModelUtil",
    "dmn-js-shared/lib/util/DiUtil",
    "dmn-js/lib/Modeler",
    "angular-ui-bootstrap",
    "jquery-ui",
    "jquery-ui/ui/widgets/mouse",
    "jquery-ui/ui/data",
    "jquery-ui/ui/plugin",
    "jquery-ui/ui/safe-active-element",
    "jquery-ui/ui/safe-blur",
    "jquery-ui/ui/scroll-parent",
    "jquery-ui/ui/version",
    "jquery-ui/ui/widget",
    "jquery-ui/ui/widgets/draggable"
  ]
};

let optimization = {
  runtimeChunk: 'single',
};

let rules = [
  {
    test: /\.html$/,
    use: [{
      loader: 'ejs-loader',
      options: {
        esModule: false
      }
    }, {
      loader: 'extract-loader'
    },
      {
        loader: 'html-loader',
        options: {
          minimize: false
        }
      }
    ]
  },
  {
    test: /\.(woff(2)?|ttf|eot|svg)(\?v=\d+\.\d+\.\d+)?$/,
    include: [],
    use: [
      {
        loader: 'file-loader',
        options: {
          name: '[name].[ext]',
          outputPath: 'fonts/'
        }
      }
    ]
  },
  {
    test: /\.(png|jpe?g)/i,
    use: [
      {
        loader: 'url-loader',
        options: {
          name: './assets/images/[name].[ext]',
          limit: 10000
        }
      },
      {
        loader: 'img-loader'
      }
    ]
  },
  {
    test: /\.less$/i,
    use: [
      // compiles Less to CSS
      "style-loader",
      "css-loader",
      "less-loader",
    ],
  },
  {
    test: /\.js$/,
    exclude: /node_modules/,
    use: jsLoaders
  }
];

module.exports = (_env, argv = {}) => {
  const isDevMode = argv.mode === 'development';
  const isTestMode = argv.mode === 'test';

  if (isDevMode) {
    jsLoaders = ['ng-hot-reload-loader', ...jsLoaders];
    entry = {
      'dev-server': 'webpack-dev-server/client?http://localhost:8081',
      'hot-only-dev-server': 'webpack/hot/only-dev-server',
      ...entry
    };
    rules = [
      ...rules,
      {
        test: /ui\/cockpit\/client\/scripts\/index\.html$/,
        loader: 'string-replace-loader',
        options: {
          multiple: [
            {search: /\$APP_ROOT/g, replace: '/camunda'},
            {search: /\$BASE/g, replace: '/camunda/app/cockpit/'},
            {
              search: /\$PLUGIN_DEPENDENCIES/g, replace: `
                [{
                  ngModuleName: 'cockpit.plugin.cockpitPlugins',
                  requirePackageName: 'cockpit-plugin-cockpitPlugins'
                }]`
            },
            {
              search: /\$PLUGIN_PACKAGES/g, replace: `
                [{
                  name: 'cockpit-plugin-cockpitPlugins',
                  location: '/camunda/app/cockpit/',
                  main: 'plugin.js'
                }]`
            }
          ]
        }
      },
      {
        test: /ui\/tasklist\/client\/index\.html$/,
        loader: 'string-replace-loader',
        options: {
          multiple: [
            {search: /\$APP_ROOT/g, replace: '/camunda'},
            {search: /\$BASE/g, replace: '/camunda/app/tasklist/'},
            {
              search: /\$PLUGIN_DEPENDENCIES/g, replace: `
                [{
                  ngModuleName: 'tasklist.plugin.tasklistPlugins',
                  requirePackageName: 'tasklist-plugin-tasklistPlugins'
                }]`
            },
            {
              search: /\$PLUGIN_PACKAGES/g, replace: `
                [{
                  name: 'tasklist-plugin-tasklistPlugins',
                  location: '/camunda/app/tasklist/',
                  main: 'tasklistPlugins.js'
                }]`
            }
          ]
        }
      },
      {
        test: /ui\/admin\/client\/scripts\/index\.html$/,
        loader: 'string-replace-loader',
        options: {
          multiple: [
            {search: /\$APP_ROOT/g, replace: '/camunda'},
            {search: /\$BASE/g, replace: '/camunda/app/admin/'},
            {
              search: /\$PLUGIN_DEPENDENCIES/g, replace: `
                [{
                  ngModuleName: 'admin.plugin.adminPlugins',
                  requirePackageName: 'admin-plugin-adminPlugins'
                }]`
            },
            {
              search: /\$PLUGIN_PACKAGES/g, replace: `
                [{
                  name: 'admin-plugin-adminPlugins',
                  location: '/camunda/app/admin/',
                  main: 'adminPlugins.js'
                }]`
            }
          ]
        }
      },
      {
        test: /ui\/welcome\/client\/scripts\/index\.html$/,
        loader: 'string-replace-loader',
        options: {
          multiple: [
            {search: /\$APP_ROOT/g, replace: '/camunda'},
            {search: /\$BASE/g, replace: '/camunda/app/welcome/'},
            {search: /\$PLUGIN_DEPENDENCIES/g, replace: '[]'},
            {search: /\$PLUGIN_PACKAGES/g, replace: '[]'}
          ]
        }
      }
    ]
  } else {
    optimization = {
      minimize: true,
      minimizer: [
        new TerserPlugin({
          terserOptions: {
            format: {
              comments: /license/i,
            },
          },
          extractComments: false,
        }),
      ],
      ...optimization
    };
  }

  console.log('[Webpack Config] NODE_ENV:', process.env.NODE_ENV);
  console.log('[Webpack Config] argv.mode:', argv.mode);

  return {
    entry,
    stats: {
      errorDetails: true
    },
    devtool: isDevMode ? 'source-map' : false,
    output: {
      library: '[name]',
      libraryTarget: 'umd',
      filename: '[name].js',
    },
    devServer: {
      static: {
        directory: path.resolve(__dirname, './public'),
        publicPath: '/camunda/app',
      },
      hot: true,
      //hotOnly: true,
      https: false,
      proxy: {
        '/camunda/api': {
          target: 'http://localhost:8080',
          logLevel: 'debug'
        }
      },
      open: ['/camunda/app/cockpit/']
    },
    resolve: {
      fallback: {
        fs: false
      },
      extensions: ['.js'],
      alias: {
        '@': path.resolve('src'),
        'camunda-bpm-sdk-js': path.resolve('./camunda-bpm-sdk-js'),
        'cam-common': path.resolve('./ui/common/scripts/module'),
      }
    },
    module: {
      rules
    },
    plugins: [
      // clean the "dist" folder before generating a build
      new CleanWebpackPlugin(),
      new HtmlWebPackPlugin({
        template: 'ui/cockpit/client/scripts/index.html',
        filename: 'camunda/app/cockpit/index.html',
        chunks: ['camunda/lib/jquery', 'camunda/lib/requirejs', 'camunda/lib/deps'],
        favicon: "./ui/common/images/favicon.ico"
      }),
      new HtmlWebPackPlugin({
        template: 'ui/tasklist/client/index.html',
        filename: 'camunda/app/tasklist/index.html',
        chunks: ['camunda/lib/jquery', 'camunda/lib/requirejs', 'camunda/lib/deps'],
        favicon: "./ui/common/images/favicon.ico"
      }),
      new HtmlWebPackPlugin({
        template: 'ui/admin/client/scripts/index.html',
        filename: 'camunda/app/admin/index.html',
        chunks: ['camunda/lib/jquery', 'camunda/lib/requirejs', 'camunda/lib/deps'],
        favicon: "./ui/common/images/favicon.ico"
      }),
      new HtmlWebPackPlugin({
        template: 'ui/welcome/client/scripts/index.html',
        filename: 'camunda/app/welcome/index.html',
        chunks: ['camunda/lib/jquery', 'camunda/lib/requirejs', 'camunda/lib/deps'],
        favicon: "./ui/common/images/favicon.ico"
      }),
      new MiniCssExtractPlugin({
        // both options are optional, similar to the same options in webpackOptions.output
        filename: isDevMode ? '[name].css' : '[name].[hash].css',
        chunkFilename: isDevMode ? '[id].css' : '[id].[hash].css'
      }),
      new webpack.HotModuleReplacementPlugin(),
      new webpack.DefinePlugin({
        // define custom global variables
        TEST_MODE: isTestMode,
        DEV_MODE: isDevMode,
        'process.env.CAMUNDA_VERSION': "'7.18.0-SNAPSHOT'",
      }),
      new webpack.ProvidePlugin({
        DEV_MODE: isDevMode,
      }),
      //new BundleAnalyzerPlugin()
    ],
    optimization
  }
};

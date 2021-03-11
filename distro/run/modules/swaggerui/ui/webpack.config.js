const writeThirdPartyNotice = require('./tasks/license').writeThirdPartyNotice;
const path = require('path');
const CopyPlugin = require('copy-webpack-plugin');
const TerserPlugin = require("terser-webpack-plugin");
const LicenseCheckerWebpackPlugin = require("license-checker-webpack-plugin");


const config = {
    entry: './src/index.js',
    output: {
        path: path.resolve(__dirname, '..', 'target', 'classes', 'swaggerui'),
        filename: 'bundle.js',
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: [
                    'style-loader',
                    'css-loader'
                ]
            }
        ]
    },
    plugins: [
        new CopyPlugin({
            patterns: [{from: 'src/index.html'}, {from: 'src/favicon.ico'}],
        }),
        new LicenseCheckerWebpackPlugin({
            outputFilename: path.join( "..", "..", "THIRD-PARTY-NOTICE.json"),
            outputWriter: writeThirdPartyNotice
        })
    ],
    optimization: {
        minimize: true,
        minimizer: [
            new TerserPlugin({
                extractComments: false,
            }),
        ],
    },
};

module.exports = config;
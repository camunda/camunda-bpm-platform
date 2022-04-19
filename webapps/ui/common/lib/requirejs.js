import {requirejs, define, require as rrequire} from "exports-loader?exports=requirejs,define,require!requirejs/require";

window.__define = define;
window.__require = rrequire;
window.__requirejs = requirejs;

window.define = undefined;
window.require = undefined;
window.bust = '$GRUNT_CACHE_BUST';

const base = document.getElementsByTagName('base')[0];
const appRoot = base.getAttribute("app-root");
const appName = base.getAttribute("app-name");

requirejs.config({
  baseUrl: `${appRoot}/app/${appName}`,
  urlArgs: 'bust=$GRUNT_CACHE_BUST'
});

window.jQuery = window.$ = require('jquery');

requirejs([`camunda-${appName}-bootstrap`], () => {});

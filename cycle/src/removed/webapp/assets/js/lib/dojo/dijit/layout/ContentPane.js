//>>built
define("dijit/layout/ContentPane",["dojo/_base/kernel","dojo/_base/lang","../_Widget","../_Container","./_ContentPaneResizeMixin","dojo/string","dojo/html","dojo/i18n!../nls/loading","dojo/_base/array","dojo/_base/declare","dojo/_base/Deferred","dojo/dom","dojo/dom-attr","dojo/_base/xhr","dojo/i18n","dojo/when"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,_c,_d,_e,_f,_10){
return _a("dijit.layout.ContentPane",[_3,_4,_5],{href:"",content:"",extractContent:false,parseOnLoad:true,parserScope:_1._scopeName,preventCache:false,preload:false,refreshOnShow:false,loadingMessage:"<span class='dijitContentPaneLoading'><span class='dijitInline dijitIconLoading'></span>${loadingState}</span>",errorMessage:"<span class='dijitContentPaneError'><span class='dijitInline dijitIconError'></span>${errorState}</span>",isLoaded:false,baseClass:"dijitContentPane",ioArgs:{},onLoadDeferred:null,_setTitleAttr:null,stopParser:true,template:false,create:function(_11,_12){
if((!_11||!_11.template)&&_12&&!("href" in _11)&&!("content" in _11)){
_12=_c.byId(_12);
var df=_12.ownerDocument.createDocumentFragment();
while(_12.firstChild){
df.appendChild(_12.firstChild);
}
_11=_2.delegate(_11,{content:df});
}
this.inherited(arguments,[_11,_12]);
},postMixInProperties:function(){
this.inherited(arguments);
var _13=_f.getLocalization("dijit","loading",this.lang);
this.loadingMessage=_6.substitute(this.loadingMessage,_13);
this.errorMessage=_6.substitute(this.errorMessage,_13);
},buildRendering:function(){
this.inherited(arguments);
if(!this.containerNode){
this.containerNode=this.domNode;
}
this.domNode.title="";
if(!_d.get(this.domNode,"role")){
this.domNode.setAttribute("role","group");
}
},startup:function(){
this.inherited(arguments);
if(this._contentSetter){
_9.forEach(this._contentSetter.parseResults,function(obj){
if(!obj._started&&!obj._destroyed&&_2.isFunction(obj.startup)){
obj.startup();
obj._started=true;
}
},this);
}
},_startChildren:function(){
_9.forEach(this.getChildren(),function(obj){
if(!obj._started&&!obj._destroyed&&_2.isFunction(obj.startup)){
obj.startup();
obj._started=true;
}
});
if(this._contentSetter){
_9.forEach(this._contentSetter.parseResults,function(obj){
if(!obj._started&&!obj._destroyed&&_2.isFunction(obj.startup)){
obj.startup();
obj._started=true;
}
},this);
}
},setHref:function(_14){
_1.deprecated("dijit.layout.ContentPane.setHref() is deprecated. Use set('href', ...) instead.","","2.0");
return this.set("href",_14);
},_setHrefAttr:function(_15){
this.cancel();
this.onLoadDeferred=new _b(_2.hitch(this,"cancel"));
this.onLoadDeferred.then(_2.hitch(this,"onLoad"));
this._set("href",_15);
if(this.preload||(this._created&&this._isShown())){
this._load();
}else{
this._hrefChanged=true;
}
return this.onLoadDeferred;
},setContent:function(_16){
_1.deprecated("dijit.layout.ContentPane.setContent() is deprecated.  Use set('content', ...) instead.","","2.0");
this.set("content",_16);
},_setContentAttr:function(_17){
this._set("href","");
this.cancel();
this.onLoadDeferred=new _b(_2.hitch(this,"cancel"));
if(this._created){
this.onLoadDeferred.then(_2.hitch(this,"onLoad"));
}
this._setContent(_17||"");
this._isDownloaded=false;
return this.onLoadDeferred;
},_getContentAttr:function(){
return this.containerNode.innerHTML;
},cancel:function(){
if(this._xhrDfd&&(this._xhrDfd.fired==-1)){
this._xhrDfd.cancel();
}
delete this._xhrDfd;
this.onLoadDeferred=null;
},destroy:function(){
this.cancel();
this.inherited(arguments);
},destroyRecursive:function(_18){
if(this._beingDestroyed){
return;
}
this.inherited(arguments);
},_onShow:function(){
this.inherited(arguments);
if(this.href){
if(!this._xhrDfd&&(!this.isLoaded||this._hrefChanged||this.refreshOnShow)){
return this.refresh();
}
}
},refresh:function(){
this.cancel();
this.onLoadDeferred=new _b(_2.hitch(this,"cancel"));
this.onLoadDeferred.then(_2.hitch(this,"onLoad"));
this._load();
return this.onLoadDeferred;
},_load:function(){
this._setContent(this.onDownloadStart(),true);
var _19=this;
var _1a={preventCache:(this.preventCache||this.refreshOnShow),url:this.href,handleAs:"text"};
if(_2.isObject(this.ioArgs)){
_2.mixin(_1a,this.ioArgs);
}
var _1b=(this._xhrDfd=(this.ioMethod||_e.get)(_1a)),_1c;
_1b.then(function(_1d){
_1c=_1d;
try{
_19._isDownloaded=true;
return _19._setContent(_1d,false);
}
catch(err){
_19._onError("Content",err);
}
},function(err){
if(!_1b.canceled){
_19._onError("Download",err);
}
delete _19._xhrDfd;
return err;
}).then(function(){
_19.onDownloadEnd();
delete _19._xhrDfd;
return _1c;
});
delete this._hrefChanged;
},_onLoadHandler:function(_1e){
this._set("isLoaded",true);
try{
this.onLoadDeferred.resolve(_1e);
}
catch(e){
console.error("Error "+this.widgetId+" running custom onLoad code: "+e.message);
}
},_onUnloadHandler:function(){
this._set("isLoaded",false);
try{
this.onUnload();
}
catch(e){
console.error("Error "+this.widgetId+" running custom onUnload code: "+e.message);
}
},destroyDescendants:function(_1f){
if(this.isLoaded){
this._onUnloadHandler();
}
var _20=this._contentSetter;
_9.forEach(this.getChildren(),function(_21){
if(_21.destroyRecursive){
_21.destroyRecursive(_1f);
}else{
if(_21.destroy){
_21.destroy(_1f);
}
}
_21._destroyed=true;
});
if(_20){
_9.forEach(_20.parseResults,function(_22){
if(!_22._destroyed){
if(_22.destroyRecursive){
_22.destroyRecursive(_1f);
}else{
if(_22.destroy){
_22.destroy(_1f);
}
}
_22._destroyed=true;
}
});
delete _20.parseResults;
}
if(!_1f){
_7._emptyNode(this.containerNode);
}
delete this._singleChild;
},_setContent:function(_23,_24){
this.destroyDescendants();
var _25=this._contentSetter;
if(!(_25&&_25 instanceof _7._ContentSetter)){
_25=this._contentSetter=new _7._ContentSetter({node:this.containerNode,_onError:_2.hitch(this,this._onError),onContentError:_2.hitch(this,function(e){
var _26=this.onContentError(e);
try{
this.containerNode.innerHTML=_26;
}
catch(e){
console.error("Fatal "+this.id+" could not change content due to "+e.message,e);
}
})});
}
var _27=_2.mixin({cleanContent:this.cleanContent,extractContent:this.extractContent,parseContent:!_23.domNode&&this.parseOnLoad,parserScope:this.parserScope,startup:false,dir:this.dir,lang:this.lang,textDir:this.textDir},this._contentSetterParams||{});
var p=_25.set((_2.isObject(_23)&&_23.domNode)?_23.domNode:_23,_27);
var _28=this;
return _10(p&&p.then?p:_25.parseDeferred,function(){
delete _28._contentSetterParams;
if(!_24){
if(_28._started){
_28._startChildren();
_28._scheduleLayout();
}
_28._onLoadHandler(_23);
}
});
},_onError:function(_29,err,_2a){
this.onLoadDeferred.reject(err);
var _2b=this["on"+_29+"Error"].call(this,err);
if(_2a){
console.error(_2a,err);
}else{
if(_2b){
this._setContent(_2b,true);
}
}
},onLoad:function(){
},onUnload:function(){
},onDownloadStart:function(){
return this.loadingMessage;
},onContentError:function(){
},onDownloadError:function(){
return this.errorMessage;
},onDownloadEnd:function(){
}});
});

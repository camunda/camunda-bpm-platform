//>>built
define("dojox/mvc/EditStoreRefController",["dojo/_base/declare","dojo/_base/lang","dojo/when","./getPlainValue","./EditModelRefController","./StoreRefController"],function(_1,_2,_3,_4,_5,_6){
return _1("dojox.mvc.EditStoreRefController",[_6,_5],{getPlainValueOptions:null,_removals:[],_resultsWatchHandle:null,_refSourceModelProp:"sourceModel",queryStore:function(_7,_8){
if(!(this.store||{}).query){
return;
}
if(this._resultsWatchHandle){
this._resultsWatchHandle.unwatch();
}
this._removals=[];
var _9=this;
return _3(this.inherited(arguments),function(_a){
if(_9._beingDestroyed){
return;
}
if(_2.isArray(_a)){
_9._resultsWatchHandle=_a.watchElements(function(_b,_c,_d){
[].push.apply(_9._removals,_c);
});
}
return _a;
});
},getStore:function(id,_e){
if(this._resultsWatchHandle){
this._resultsWatchHandle.unwatch();
}
return this.inherited(arguments);
},commit:function(){
if(this._removals){
for(var i=0;i<this._removals.length;i++){
this.store.remove(this.store.getIdentity(this._removals[i]));
}
this._removals=[];
}
var _f=_4(this.get(this._refEditModelProp),this.getPlainValueOptions);
if(_2.isArray(_f)){
for(var i=0;i<_f.length;i++){
this.store.put(_f[i]);
}
}else{
this.store.put(_f);
}
this.inherited(arguments);
},reset:function(){
this.inherited(arguments);
this._removals=[];
},destroy:function(){
if(this._resultsWatchHandle){
this._resultsWatchHandle.unwatch();
}
this.inherited(arguments);
}});
});

//>>built
define("dijit/_WidgetBase",["require","dojo/_base/array","dojo/aspect","dojo/_base/config","dojo/_base/connect","dojo/_base/declare","dojo/dom","dojo/dom-attr","dojo/dom-class","dojo/dom-construct","dojo/dom-geometry","dojo/dom-style","dojo/has","dojo/_base/kernel","dojo/_base/lang","dojo/on","dojo/ready","dojo/Stateful","dojo/topic","dojo/_base/window","./Destroyable","./registry"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,_b,_c,_d,_e,_f,on,_10,_11,_12,win,_13,_14){
_d.add("dijit-legacy-requires",!_e.isAsync);
if(_d("dijit-legacy-requires")){
_10(0,function(){
var _15=["dijit/_base/manager"];
_1(_15);
});
}
var _16={};
function _17(obj){
var ret={};
for(var _18 in obj){
ret[_18.toLowerCase()]=true;
}
return ret;
};
function _19(_1a){
return function(val){
_8[val?"set":"remove"](this.domNode,_1a,val);
this._set(_1a,val);
};
};
return _6("dijit._WidgetBase",[_11,_13],{id:"",_setIdAttr:"domNode",lang:"",_setLangAttr:_19("lang"),dir:"",_setDirAttr:_19("dir"),textDir:"","class":"",_setClassAttr:{node:"domNode",type:"class"},style:"",title:"",tooltip:"",baseClass:"",srcNodeRef:null,domNode:null,containerNode:null,ownerDocument:null,_setOwnerDocumentAttr:function(val){
this._set("ownerDocument",val);
},attributeMap:{},_blankGif:_4.blankGif||_1.toUrl("dojo/resources/blank.gif"),postscript:function(_1b,_1c){
this.create(_1b,_1c);
},create:function(_1d,_1e){
this.srcNodeRef=_7.byId(_1e);
this._connects=[];
this._supportingWidgets=[];
if(this.srcNodeRef&&(typeof this.srcNodeRef.id=="string")){
this.id=this.srcNodeRef.id;
}
if(_1d){
this.params=_1d;
_f.mixin(this,_1d);
}
this.postMixInProperties();
if(!this.id){
this.id=_14.getUniqueId(this.declaredClass.replace(/\./g,"_"));
if(this.params){
delete this.params.id;
}
}
this.ownerDocument=this.ownerDocument||(this.srcNodeRef?this.srcNodeRef.ownerDocument:win.doc);
this.ownerDocumentBody=win.body(this.ownerDocument);
_14.add(this);
this.buildRendering();
var _1f;
if(this.domNode){
this._applyAttributes();
var _20=this.srcNodeRef;
if(_20&&_20.parentNode&&this.domNode!==_20){
_20.parentNode.replaceChild(this.domNode,_20);
_1f=true;
}
this.domNode.setAttribute("widgetId",this.id);
}
this.postCreate();
if(_1f){
delete this.srcNodeRef;
}
this._created=true;
},_applyAttributes:function(){
var _21=this.constructor,_22=_21._setterAttrs;
if(!_22){
_22=(_21._setterAttrs=[]);
for(var _23 in this.attributeMap){
_22.push(_23);
}
var _24=_21.prototype;
for(var _25 in _24){
if(_25 in this.attributeMap){
continue;
}
var _26="_set"+_25.replace(/^[a-z]|-[a-zA-Z]/g,function(c){
return c.charAt(c.length-1).toUpperCase();
})+"Attr";
if(_26 in _24){
_22.push(_25);
}
}
}
_2.forEach(_22,function(_27){
if(this.params&&_27 in this.params){
}else{
if(this[_27]){
this.set(_27,this[_27]);
}
}
},this);
for(var _28 in this.params){
this.set(_28,this.params[_28]);
}
},postMixInProperties:function(){
},buildRendering:function(){
if(!this.domNode){
this.domNode=this.srcNodeRef||this.ownerDocument.createElement("div");
}
if(this.baseClass){
var _29=this.baseClass.split(" ");
if(!this.isLeftToRight()){
_29=_29.concat(_2.map(_29,function(_2a){
return _2a+"Rtl";
}));
}
_9.add(this.domNode,_29);
}
},postCreate:function(){
},startup:function(){
if(this._started){
return;
}
this._started=true;
_2.forEach(this.getChildren(),function(obj){
if(!obj._started&&!obj._destroyed&&_f.isFunction(obj.startup)){
obj.startup();
obj._started=true;
}
});
},destroyRecursive:function(_2b){
this._beingDestroyed=true;
this.destroyDescendants(_2b);
this.destroy(_2b);
},destroy:function(_2c){
this._beingDestroyed=true;
this.uninitialize();
function _2d(w){
if(w.destroyRecursive){
w.destroyRecursive(_2c);
}else{
if(w.destroy){
w.destroy(_2c);
}
}
};
_2.forEach(this._connects,_f.hitch(this,"disconnect"));
_2.forEach(this._supportingWidgets,_2d);
if(this.domNode){
_2.forEach(_14.findWidgets(this.domNode,this.containerNode),_2d);
}
this.destroyRendering(_2c);
_14.remove(this.id);
this._destroyed=true;
},destroyRendering:function(_2e){
if(this.bgIframe){
this.bgIframe.destroy(_2e);
delete this.bgIframe;
}
if(this.domNode){
if(_2e){
_8.remove(this.domNode,"widgetId");
}else{
_a.destroy(this.domNode);
}
delete this.domNode;
}
if(this.srcNodeRef){
if(!_2e){
_a.destroy(this.srcNodeRef);
}
delete this.srcNodeRef;
}
},destroyDescendants:function(_2f){
_2.forEach(this.getChildren(),function(_30){
if(_30.destroyRecursive){
_30.destroyRecursive(_2f);
}
});
},uninitialize:function(){
return false;
},_setStyleAttr:function(_31){
var _32=this.domNode;
if(_f.isObject(_31)){
_c.set(_32,_31);
}else{
if(_32.style.cssText){
_32.style.cssText+="; "+_31;
}else{
_32.style.cssText=_31;
}
}
this._set("style",_31);
},_attrToDom:function(_33,_34,_35){
_35=arguments.length>=3?_35:this.attributeMap[_33];
_2.forEach(_f.isArray(_35)?_35:[_35],function(_36){
var _37=this[_36.node||_36||"domNode"];
var _38=_36.type||"attribute";
switch(_38){
case "attribute":
if(_f.isFunction(_34)){
_34=_f.hitch(this,_34);
}
var _39=_36.attribute?_36.attribute:(/^on[A-Z][a-zA-Z]*$/.test(_33)?_33.toLowerCase():_33);
if(_37.tagName){
_8.set(_37,_39,_34);
}else{
_37.set(_39,_34);
}
break;
case "innerText":
_37.innerHTML="";
_37.appendChild(this.ownerDocument.createTextNode(_34));
break;
case "innerHTML":
_37.innerHTML=_34;
break;
case "class":
_9.replace(_37,_34,this[_33]);
break;
}
},this);
},get:function(_3a){
var _3b=this._getAttrNames(_3a);
return this[_3b.g]?this[_3b.g]():this[_3a];
},set:function(_3c,_3d){
if(typeof _3c==="object"){
for(var x in _3c){
this.set(x,_3c[x]);
}
return this;
}
var _3e=this._getAttrNames(_3c),_3f=this[_3e.s];
if(_f.isFunction(_3f)){
var _40=_3f.apply(this,Array.prototype.slice.call(arguments,1));
}else{
var _41=this.focusNode&&!_f.isFunction(this.focusNode)?"focusNode":"domNode",tag=this[_41].tagName,_42=_16[tag]||(_16[tag]=_17(this[_41])),map=_3c in this.attributeMap?this.attributeMap[_3c]:_3e.s in this?this[_3e.s]:((_3e.l in _42&&typeof _3d!="function")||/^aria-|^data-|^role$/.test(_3c))?_41:null;
if(map!=null){
this._attrToDom(_3c,_3d,map);
}
this._set(_3c,_3d);
}
return _40||this;
},_attrPairNames:{},_getAttrNames:function(_43){
var apn=this._attrPairNames;
if(apn[_43]){
return apn[_43];
}
var uc=_43.replace(/^[a-z]|-[a-zA-Z]/g,function(c){
return c.charAt(c.length-1).toUpperCase();
});
return (apn[_43]={n:_43+"Node",s:"_set"+uc+"Attr",g:"_get"+uc+"Attr",l:uc.toLowerCase()});
},_set:function(_44,_45){
var _46=this[_44];
this[_44]=_45;
if(this._created&&_45!==_46){
if(this._watchCallbacks){
this._watchCallbacks(_44,_46,_45);
}
this.emit("attrmodified-"+_44,{detail:{prevValue:_46,newValue:_45}});
}
},emit:function(_47,_48,_49){
_48=_48||{};
if(_48.bubbles===undefined){
_48.bubbles=true;
}
if(_48.cancelable===undefined){
_48.cancelable=true;
}
if(!_48.detail){
_48.detail={};
}
_48.detail.widget=this;
var ret,_4a=this["on"+_47];
if(_4a){
ret=_4a.apply(this,_49?_49:[_48]);
}
if(this._started&&!this._beingDestroyed){
on.emit(this.domNode,_47.toLowerCase(),_48);
}
return ret;
},on:function(_4b,_4c){
var _4d=this._onMap(_4b);
if(_4d){
return _3.after(this,_4d,_4c,true);
}
return this.own(on(this.domNode,_4b,_4c))[0];
},_onMap:function(_4e){
var _4f=this.constructor,map=_4f._onMap;
if(!map){
map=(_4f._onMap={});
for(var _50 in _4f.prototype){
if(/^on/.test(_50)){
map[_50.replace(/^on/,"").toLowerCase()]=_50;
}
}
}
return map[typeof _4e=="string"&&_4e.toLowerCase()];
},toString:function(){
return "[Widget "+this.declaredClass+", "+(this.id||"NO ID")+"]";
},getChildren:function(){
return this.containerNode?_14.findWidgets(this.containerNode):[];
},getParent:function(){
return _14.getEnclosingWidget(this.domNode.parentNode);
},connect:function(obj,_51,_52){
return this.own(_5.connect(obj,_51,this,_52))[0];
},disconnect:function(_53){
_53.remove();
},subscribe:function(t,_54){
return this.own(_12.subscribe(t,_f.hitch(this,_54)))[0];
},unsubscribe:function(_55){
_55.remove();
},isLeftToRight:function(){
return this.dir?(this.dir=="ltr"):_b.isBodyLtr(this.ownerDocument);
},isFocusable:function(){
return this.focus&&(_c.get(this.domNode,"display")!="none");
},placeAt:function(_56,_57){
var _58=!_56.tagName&&_14.byId(_56);
if(_58&&_58.addChild&&(!_57||typeof _57==="number")){
_58.addChild(this,_57);
}else{
var ref=_58?(_58.containerNode&&!/after|before|replace/.test(_57||"")?_58.containerNode:_58.domNode):_7.byId(_56,this.ownerDocument);
_a.place(this.domNode,ref,_57);
if(!this._started&&(this.getParent()||{})._started){
this.startup();
}
}
return this;
},getTextDir:function(_59,_5a){
return _5a;
},applyTextDir:function(){
},defer:function(fcn,_5b){
var _5c=setTimeout(_f.hitch(this,function(){
_5c=null;
if(!this._destroyed){
_f.hitch(this,fcn)();
}
}),_5b||0);
return {remove:function(){
if(_5c){
clearTimeout(_5c);
_5c=null;
}
return null;
}};
}});
});

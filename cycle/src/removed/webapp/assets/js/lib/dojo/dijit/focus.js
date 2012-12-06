//>>built
define("dijit/focus",["dojo/aspect","dojo/_base/declare","dojo/dom","dojo/dom-attr","dojo/dom-construct","dojo/Evented","dojo/_base/lang","dojo/on","dojo/ready","dojo/sniff","dojo/Stateful","dojo/_base/unload","dojo/_base/window","dojo/window","./a11y","./registry","./main"],function(_1,_2,_3,_4,_5,_6,_7,on,_8,_9,_a,_b,_c,_d,_e,_f,_10){
var _11=_2([_a,_6],{curNode:null,activeStack:[],constructor:function(){
var _12=_7.hitch(this,function(_13){
if(_3.isDescendant(this.curNode,_13)){
this.set("curNode",null);
}
if(_3.isDescendant(this.prevNode,_13)){
this.set("prevNode",null);
}
});
_1.before(_5,"empty",_12);
_1.before(_5,"destroy",_12);
},registerIframe:function(_14){
return this.registerWin(_14.contentWindow,_14);
},registerWin:function(_15,_16){
var _17=this;
var _18=function(evt){
_17._justMouseDowned=true;
setTimeout(function(){
_17._justMouseDowned=false;
},0);
if(_9("ie")&&evt&&evt.srcElement&&evt.srcElement.parentNode==null){
return;
}
_17._onTouchNode(_16||evt.target||evt.srcElement,"mouse");
};
var doc=_9("ie")?_15.document.documentElement:_15.document;
if(doc){
if(_9("ie")){
_15.document.body.attachEvent("onmousedown",_18);
var _19=function(evt){
var tag=evt.srcElement.tagName.toLowerCase();
if(tag=="#document"||tag=="body"){
return;
}
if(_e.isTabNavigable(evt.srcElement)){
_17._onFocusNode(_16||evt.srcElement);
}else{
_17._onTouchNode(_16||evt.srcElement);
}
};
doc.attachEvent("onfocusin",_19);
var _1a=function(evt){
_17._onBlurNode(_16||evt.srcElement);
};
doc.attachEvent("onfocusout",_1a);
return {remove:function(){
_15.document.detachEvent("onmousedown",_18);
doc.detachEvent("onfocusin",_19);
doc.detachEvent("onfocusout",_1a);
doc=null;
}};
}else{
doc.body.addEventListener("mousedown",_18,true);
doc.body.addEventListener("touchstart",_18,true);
var _1b=function(evt){
_17._onFocusNode(_16||evt.target);
};
doc.addEventListener("focus",_1b,true);
var _1c=function(evt){
_17._onBlurNode(_16||evt.target);
};
doc.addEventListener("blur",_1c,true);
return {remove:function(){
doc.body.removeEventListener("mousedown",_18,true);
doc.body.removeEventListener("touchstart",_18,true);
doc.removeEventListener("focus",_1b,true);
doc.removeEventListener("blur",_1c,true);
doc=null;
}};
}
}
},_onBlurNode:function(_1d){
if(this._clearFocusTimer){
clearTimeout(this._clearFocusTimer);
}
this._clearFocusTimer=setTimeout(_7.hitch(this,function(){
this.set("prevNode",this.curNode);
this.set("curNode",null);
}),0);
if(this._justMouseDowned){
return;
}
if(this._clearActiveWidgetsTimer){
clearTimeout(this._clearActiveWidgetsTimer);
}
this._clearActiveWidgetsTimer=setTimeout(_7.hitch(this,function(){
delete this._clearActiveWidgetsTimer;
this._setStack([]);
}),0);
},_onTouchNode:function(_1e,by){
if(this._clearActiveWidgetsTimer){
clearTimeout(this._clearActiveWidgetsTimer);
delete this._clearActiveWidgetsTimer;
}
var _1f=[];
try{
while(_1e){
var _20=_4.get(_1e,"dijitPopupParent");
if(_20){
_1e=_f.byId(_20).domNode;
}else{
if(_1e.tagName&&_1e.tagName.toLowerCase()=="body"){
if(_1e===_c.body()){
break;
}
_1e=_d.get(_1e.ownerDocument).frameElement;
}else{
var id=_1e.getAttribute&&_1e.getAttribute("widgetId"),_21=id&&_f.byId(id);
if(_21&&!(by=="mouse"&&_21.get("disabled"))){
_1f.unshift(id);
}
_1e=_1e.parentNode;
}
}
}
}
catch(e){
}
this._setStack(_1f,by);
},_onFocusNode:function(_22){
if(!_22){
return;
}
if(_22.nodeType==9){
return;
}
if(this._clearFocusTimer){
clearTimeout(this._clearFocusTimer);
delete this._clearFocusTimer;
}
this._onTouchNode(_22);
if(_22==this.curNode){
return;
}
this.set("prevNode",this.curNode);
this.set("curNode",_22);
},_setStack:function(_23,by){
var _24=this.activeStack;
this.set("activeStack",_23);
for(var _25=0;_25<Math.min(_24.length,_23.length);_25++){
if(_24[_25]!=_23[_25]){
break;
}
}
var _26;
for(var i=_24.length-1;i>=_25;i--){
_26=_f.byId(_24[i]);
if(_26){
_26._hasBeenBlurred=true;
_26.set("focused",false);
if(_26._focusManager==this){
_26._onBlur(by);
}
this.emit("widget-blur",_26,by);
}
}
for(i=_25;i<_23.length;i++){
_26=_f.byId(_23[i]);
if(_26){
_26.set("focused",true);
if(_26._focusManager==this){
_26._onFocus(by);
}
this.emit("widget-focus",_26,by);
}
}
},focus:function(_27){
if(_27){
try{
_27.focus();
}
catch(e){
}
}
}});
var _28=new _11();
_8(function(){
var _29=_28.registerWin(_d.get(_c.doc));
if(_9("ie")){
_b.addOnWindowUnload(function(){
if(_29){
_29.remove();
_29=null;
}
});
}
});
_10.focus=function(_2a){
_28.focus(_2a);
};
for(var _2b in _28){
if(!/^_/.test(_2b)){
_10.focus[_2b]=typeof _28[_2b]=="function"?_7.hitch(_28,_2b):_28[_2b];
}
}
_28.watch(function(_2c,_2d,_2e){
_10.focus[_2c]=_2e;
});
return _28;
});

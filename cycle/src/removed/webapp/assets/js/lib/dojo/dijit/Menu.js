//>>built
define("dijit/Menu",["require","dojo/_base/array","dojo/_base/declare","dojo/_base/event","dojo/dom","dojo/dom-attr","dojo/dom-geometry","dojo/dom-style","dojo/keys","dojo/_base/lang","dojo/on","dojo/sniff","dojo/_base/window","dojo/window","./popup","./DropDownMenu","dojo/ready"],function(_1,_2,_3,_4,_5,_6,_7,_8,_9,_a,on,_b,_c,_d,pm,_e,_f){
if(_b("dijit-legacy-requires")){
_f(0,function(){
var _10=["dijit/MenuItem","dijit/PopupMenuItem","dijit/CheckedMenuItem","dijit/MenuSeparator"];
_1(_10);
});
}
return _3("dijit.Menu",_e,{constructor:function(){
this._bindings=[];
},targetNodeIds:[],selector:"",contextMenuForWindow:false,leftClickToOpen:false,refocus:true,postCreate:function(){
if(this.contextMenuForWindow){
this.bindDomNode(this.ownerDocumentBody);
}else{
_2.forEach(this.targetNodeIds,this.bindDomNode,this);
}
this.inherited(arguments);
},_iframeContentWindow:function(_11){
return _d.get(this._iframeContentDocument(_11))||this._iframeContentDocument(_11)["__parent__"]||(_11.name&&_c.doc.frames[_11.name])||null;
},_iframeContentDocument:function(_12){
return _12.contentDocument||(_12.contentWindow&&_12.contentWindow.document)||(_12.name&&_c.doc.frames[_12.name]&&_c.doc.frames[_12.name].document)||null;
},bindDomNode:function(_13){
_13=_5.byId(_13,this.ownerDocument);
var cn;
if(_13.tagName.toLowerCase()=="iframe"){
var _14=_13,_15=this._iframeContentWindow(_14);
cn=_c.body(_15.document);
}else{
cn=(_13==_c.body(this.ownerDocument)?this.ownerDocument.documentElement:_13);
}
var _16={node:_13,iframe:_14};
_6.set(_13,"_dijitMenu"+this.id,this._bindings.push(_16));
var _17=_a.hitch(this,function(cn){
var _18=this.selector,_19=_18?function(_1a){
return on.selector(_18,_1a);
}:function(_1b){
return _1b;
},_1c=this;
return [on(cn,_19(this.leftClickToOpen?"click":"contextmenu"),function(evt){
_4.stop(evt);
_1c._scheduleOpen(this,_14,{x:evt.pageX,y:evt.pageY});
}),on(cn,_19("keydown"),function(evt){
if(evt.shiftKey&&evt.keyCode==_9.F10){
_4.stop(evt);
_1c._scheduleOpen(this,_14);
}
})];
});
_16.connects=cn?_17(cn):[];
if(_14){
_16.onloadHandler=_a.hitch(this,function(){
var _1d=this._iframeContentWindow(_14);
cn=_c.body(_1d.document);
_16.connects=_17(cn);
});
if(_14.addEventListener){
_14.addEventListener("load",_16.onloadHandler,false);
}else{
_14.attachEvent("onload",_16.onloadHandler);
}
}
},unBindDomNode:function(_1e){
var _1f;
try{
_1f=_5.byId(_1e,this.ownerDocument);
}
catch(e){
return;
}
var _20="_dijitMenu"+this.id;
if(_1f&&_6.has(_1f,_20)){
var bid=_6.get(_1f,_20)-1,b=this._bindings[bid],h;
while((h=b.connects.pop())){
h.remove();
}
var _21=b.iframe;
if(_21){
if(_21.removeEventListener){
_21.removeEventListener("load",b.onloadHandler,false);
}else{
_21.detachEvent("onload",b.onloadHandler);
}
}
_6.remove(_1f,_20);
delete this._bindings[bid];
}
},_scheduleOpen:function(_22,_23,_24){
if(!this._openTimer){
this._openTimer=this.defer(function(){
delete this._openTimer;
this._openMyself({target:_22,iframe:_23,coords:_24});
},1);
}
},_openMyself:function(_25){
var _26=_25.target,_27=_25.iframe,_28=_25.coords;
this.currentTarget=_26;
if(_28){
if(_27){
var ifc=_7.position(_27,true),_29=this._iframeContentWindow(_27),_2a=_7.docScroll(_29.document);
var cs=_8.getComputedStyle(_27),tp=_8.toPixelValue,_2b=(_b("ie")&&_b("quirks")?0:tp(_27,cs.paddingLeft))+(_b("ie")&&_b("quirks")?tp(_27,cs.borderLeftWidth):0),top=(_b("ie")&&_b("quirks")?0:tp(_27,cs.paddingTop))+(_b("ie")&&_b("quirks")?tp(_27,cs.borderTopWidth):0);
_28.x+=ifc.x+_2b-_2a.x;
_28.y+=ifc.y+top-_2a.y;
}
}else{
_28=_7.position(_26,true);
_28.x+=10;
_28.y+=10;
}
var _2c=this;
var _2d=this._focusManager.get("prevNode");
var _2e=this._focusManager.get("curNode");
var _2f=!_2e||(_5.isDescendant(_2e,this.domNode))?_2d:_2e;
function _30(){
if(_2c.refocus&&_2f){
_2f.focus();
}
pm.close(_2c);
};
pm.open({popup:this,x:_28.x,y:_28.y,onExecute:_30,onCancel:_30,orient:this.isLeftToRight()?"L":"R"});
this.focus();
this._onBlur=function(){
this.inherited("_onBlur",arguments);
pm.close(this);
};
},destroy:function(){
_2.forEach(this._bindings,function(b){
if(b){
this.unBindDomNode(b.node);
}
},this);
this.inherited(arguments);
}});
});

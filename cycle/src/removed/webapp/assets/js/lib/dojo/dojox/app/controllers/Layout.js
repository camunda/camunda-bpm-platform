//>>built
define("dojox/app/controllers/Layout",["dojo/_base/lang","dojo/_base/declare","dojo/sniff","dojo/on","dojo/_base/window","dojo/_base/array","dojo/query","dojo/dom-style","dojo/dom-attr","dojo/dom-geometry","dijit/registry","../Controller","../layout/utils"],function(_1,_2,_3,on,_4,_5,_6,_7,_8,_9,_a,_b,_c){
return _2("dojox.app.controllers.Layout",_b,{constructor:function(_d,_e){
this.events={"layout":this.layout,"select":this.select};
this.inherited(arguments);
this.bind(_4.global,_3("ios")?"orientationchange":"resize",_1.hitch(this,this.onResize));
},onResize:function(){
this._doResize(this.app);
},layout:function(_f){
var _10=_f.view;
var _11=_f.changeSize||null;
var _12=_f.resultSize||null;
this._doResize(_10,_11,_12);
},_doLayout:function(_13){
if(!_13){
console.warn("layout empty view.");
return;
}
var _14,_15;
if(_13.selectedChild&&_13.selectedChild.isFullScreen){
console.warn("fullscreen sceen layout");
}else{
_15=_6("> [data-app-region], > [region]",_13.domNode).map(function(_16){
var w=_a.getEnclosingWidget(_16);
if(w){
w.region=_8.get(_16,"data-app-region")||_8.get(_16,"region");
return w;
}
return {domNode:_16,region:_8.get(_16,"data-app-region")||_8.get(_16,"region")};
});
if(_13.selectedChild){
_15=_5.filter(_15,function(c){
if((c.region=="center")&&_13.selectedChild&&(_13.selectedChild.domNode!==c.domNode)){
_7.set(c.domNode,"zIndex",25);
_7.set(c.domNode,"display","none");
return false;
}else{
if(c.region!="center"){
_7.set(c.domNode,"display","");
_7.set(c.domNode,"zIndex",100);
}
}
return c.domNode&&c.region;
},_13);
}
}
if(_13._contentBox){
_c.layoutChildren(_13.domNode,_13._contentBox,_15);
}
},_doResize:function(_17,_18,_19){
var _1a=_17.domNode;
if(_18){
_9.setMarginBox(_1a,_18);
if(_18.t){
_1a.style.top=_18.t+"px";
}
if(_18.l){
_1a.style.left=_18.l+"px";
}
}
var mb=_19||{};
_1.mixin(mb,_18||{});
if(!("h" in mb)||!("w" in mb)){
mb=_1.mixin(_9.getMarginBox(_1a),mb);
}
var cs=_7.getComputedStyle(_1a);
var me=_9.getMarginExtents(_1a,cs);
var be=_9.getBorderExtents(_1a,cs);
var bb=(_17._borderBox={w:mb.w-(me.w+be.w),h:mb.h-(me.h+be.h)});
var pe=_9.getPadExtents(_1a,cs);
_17._contentBox={l:_7.toPixelValue(_1a,cs.paddingLeft),t:_7.toPixelValue(_1a,cs.paddingTop),w:bb.w-pe.w,h:bb.h-pe.h};
this._doLayout(_17);
if(_17.selectedChild){
this._doResize(_17.selectedChild);
}
},select:function(_1b){
var _1c=_1b.parent||this.app;
var _1d=_1b.view;
if(!_1d){
return;
}
if(_1d!==_1c.selectedChild){
if(_1c.selectedChild){
_7.set(_1c.selectedChild.domNode,"zIndex",25);
}
_7.set(_1d.domNode,"display","");
_7.set(_1d.domNode,"zIndex",50);
_1c.selectedChild=_1d;
}
this._doResize(_1c);
}});
});

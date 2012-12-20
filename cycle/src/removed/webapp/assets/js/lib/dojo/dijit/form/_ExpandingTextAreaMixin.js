//>>built
define("dijit/form/_ExpandingTextAreaMixin",["dojo/_base/declare","dojo/dom-construct","dojo/has","dojo/_base/lang","dojo/_base/window","../Viewport"],function(_1,_2,_3,_4,_5,_6){
_3.add("textarea-needs-help-shrinking",function(){
var _7=_5.body(),te=_2.create("textarea",{rows:"5",cols:"20",value:" ",style:{zoom:1,overflow:"hidden",visibility:"hidden",position:"absolute",border:"0px solid black",padding:"0px"}},_7,"last");
var _8=te.scrollHeight>=te.clientHeight;
_7.removeChild(te);
return _8;
});
return _1("dijit.form._ExpandingTextAreaMixin",null,{_setValueAttr:function(){
this.inherited(arguments);
this.resize();
},postCreate:function(){
this.inherited(arguments);
var _9=this.textbox;
this.connect(_9,"onscroll","_resizeLater");
this.connect(_9,"onresize","_resizeLater");
this.connect(_9,"onfocus","_resizeLater");
this.own(_6.on("resize",_4.hitch(this,"_resizeLater")));
_9.style.overflowY="hidden";
this._estimateHeight();
this._resizeLater();
},_onInput:function(e){
this.inherited(arguments);
this.resize();
},_estimateHeight:function(){
var _a=this.textbox;
_a.style.height="auto";
_a.rows=(_a.value.match(/\n/g)||[]).length+2;
},_resizeLater:function(){
this.defer("resize");
},resize:function(){
var _b=this.textbox;
function _c(){
var _d=false;
if(_b.value===""){
_b.value=" ";
_d=true;
}
var sh=_b.scrollHeight;
if(_d){
_b.value="";
}
return sh;
};
if(_b.style.overflowY=="hidden"){
_b.scrollTop=0;
}
if(this.busyResizing){
return;
}
this.busyResizing=true;
if(_c()||_b.offsetHeight){
var _e=_b.style.height;
if(!(/px/.test(_e))){
_e=_c();
_b.rows=1;
_b.style.height=_e+"px";
}
var _f=Math.max(Math.max(_b.offsetHeight,parseInt(_e))-_b.clientHeight,0)+_c();
var _10=_f+"px";
if(_10!=_b.style.height){
_b.rows=1;
_b.style.height=_10;
}
if(_3("textarea-needs-help-shrinking")){
var _11=_c(),_12=_11,_13=_b.style.minHeight,_14=4,_15;
_b.style.minHeight=_10;
_b.style.height="auto";
while(_f>0){
_b.style.minHeight=Math.max(_f-_14,4)+"px";
_15=_c();
var _16=_12-_15;
_f-=_16;
if(_16<_14){
break;
}
_12=_15;
_14<<=1;
}
_b.style.height=_f+"px";
_b.style.minHeight=_13;
}
_b.style.overflowY=_c()>_b.clientHeight?"auto":"hidden";
}else{
this._estimateHeight();
}
this.busyResizing=false;
}});
});

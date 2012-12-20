//>>built
define("dijit/Viewport",["dojo/Evented","dojo/on","dojo/ready","dojo/sniff","dojo/_base/window","dojo/window"],function(_1,on,_2,_3,_4,_5){
var _6=new _1();
_2(200,function(){
var _7=_5.getBox();
_6._rlh=on(_4.global,"resize",function(){
var _8=_5.getBox();
if(_7.h==_8.h&&_7.w==_8.w){
return;
}
_7=_8;
_6.emit("resize");
});
if(_3("ie")==8){
var _9=screen.deviceXDPI;
setInterval(function(){
if(screen.deviceXDPI!=_9){
_9=screen.deviceXDPI;
_6.emit("resize");
}
},500);
}
});
return _6;
});

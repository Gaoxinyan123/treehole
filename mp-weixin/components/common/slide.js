(global["webpackJsonp"]=global["webpackJsonp"]||[]).push([["components/common/slide"],{"1ac9":function(t,n,o){"use strict";var a=o("1c5f"),i=o.n(a);i.a},"1c5f":function(t,n,o){},9484:function(t,n,o){"use strict";var a;o.r(n);var i,r=function(){var t=this,n=t.$createElement,o=(t._self._c,t.__map(t.list,(function(n,o){var a=t.__get_orig(n),i=n[t.vm.img]?t.$fullImgUrl(n[t.vm.img]):null;return{$orig:a,m0:i}})));t.$mp.data=Object.assign({},{$root:{l0:o}})},e=[],c={name:"SwiperImg",props:{show_title:{type:String,default:""},list:{type:Array,default:function(){return[]}},vm:{type:Object,default:function(){return{img:"img",title:"title"}}}},data:function(){return{background:["color1","color2","color3"],indicatorDots:!0,indicatorColor:"rgba(0, 0, 0, .3)",indicatorActiveColor:"#fff",autoplay:!0,interval:2e3,duration:500}},methods:{changeIndicatorDots:function(t){this.indicatorDots=!this.indicatorDots},changeAutoplay:function(t){this.autoplay=!this.autoplay},intervalChange:function(t){this.interval=t.target.value},durationChange:function(t){this.duration=t.target.value}}},l=c,u=(o("1ac9"),o("f0c5")),s=Object(u["a"])(l,r,e,!1,null,"3f9b5b42",null,!1,a,i);n["default"]=s.exports}}]);
;(global["webpackJsonp"] = global["webpackJsonp"] || []).push([
    'components/common/slide-create-component',
    {
        'components/common/slide-create-component':(function(module, exports, __webpack_require__){
            __webpack_require__('543d')['createComponent'](__webpack_require__("9484"))
        })
    },
    [['components/common/slide-create-component']]
]);

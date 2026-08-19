import { $t as openBlock, at as createElementBlock, mt as defineComponent, nt as createBaseVNode, zt as mergeProps } from "./dist.js";
//#endregion
//#region resources/js/components/AppLogoIcon.vue
var AppLogoIcon_default = /* @__PURE__ */ defineComponent({
	inheritAttrs: false,
	__name: "AppLogoIcon",
	props: { className: { type: [
		Boolean,
		null,
		String,
		Object,
		Array
	] } },
	setup(__props) {
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock("svg", mergeProps({
				xmlns: "http://www.w3.org/2000/svg",
				viewBox: "0 0 25 14",
				class: __props.className
			}, _ctx.$attrs), [..._cache[0] || (_cache[0] = [createBaseVNode("path", {
				d: "M7.05 0H0L6.81 6.81L0 13.62H7.05L13.86 6.81L7.05 0Z",
				fill: "currentColor"
			}, null, -1), createBaseVNode("path", {
				d: "M17.7 0H10.65L17.46 6.81L10.65 13.62H17.7L24.51 6.81L17.7 0Z",
				fill: "currentColor"
			}, null, -1)])], 16);
		};
	}
});
//#endregion
export { AppLogoIcon_default as t };

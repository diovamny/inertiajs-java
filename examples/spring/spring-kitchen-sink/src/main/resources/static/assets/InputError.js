import { $t as openBlock, L as vShow, Pn as withDirectives, at as createElementBlock, mt as defineComponent, nt as createBaseVNode, yr as toDisplayString } from "./dist.js";
//#region resources/js/components/InputError.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "text-sm text-red-600 dark:text-red-500" };
//#endregion
//#region resources/js/components/InputError.vue
var InputError_default = /* @__PURE__ */ defineComponent({
	__name: "InputError",
	props: { message: {} },
	setup(__props) {
		return (_ctx, _cache) => {
			return withDirectives((openBlock(), createElementBlock("div", null, [createBaseVNode("p", _hoisted_1, toDisplayString(__props.message), 1)], 512)), [[vShow, __props.message]]);
		};
	}
});
//#endregion
export { InputError_default as t };

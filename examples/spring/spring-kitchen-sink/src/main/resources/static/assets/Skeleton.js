import { c as cn } from "./createLucideIcon.js";
import { $t as openBlock, at as createElementBlock, gr as normalizeClass, mt as defineComponent, pr as unref } from "./dist.js";
//#endregion
//#region resources/js/components/ui/skeleton/Skeleton.vue
var Skeleton_default = /* @__PURE__ */ defineComponent({
	__name: "Skeleton",
	props: { class: { type: [
		Boolean,
		null,
		String,
		Object,
		Array
	] } },
	setup(__props) {
		const props = __props;
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock("div", {
				"data-slot": "skeleton",
				class: normalizeClass(unref(cn)("animate-pulse rounded-md bg-primary/10", props.class))
			}, null, 2);
		};
	}
});
//#endregion
export { Skeleton_default as t };

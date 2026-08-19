import { c as cn } from "./createLucideIcon.js";
import { $t as openBlock, at as createElementBlock, gr as normalizeClass, mt as defineComponent, on as renderSlot, pr as unref } from "./dist.js";
//#endregion
//#region resources/js/components/ui/card/Card.vue
var Card_default = /* @__PURE__ */ defineComponent({
	__name: "Card",
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
				"data-slot": "card",
				class: normalizeClass(unref(cn)("bg-card text-card-foreground flex flex-col gap-6 rounded-2xl border py-6", props.class))
			}, [renderSlot(_ctx.$slots, "default")], 2);
		};
	}
});
//#endregion
//#region resources/js/components/ui/card/CardContent.vue
var CardContent_default = /* @__PURE__ */ defineComponent({
	__name: "CardContent",
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
				"data-slot": "card-content",
				class: normalizeClass(unref(cn)("px-6", props.class))
			}, [renderSlot(_ctx.$slots, "default")], 2);
		};
	}
});
//#endregion
//#region resources/js/components/ui/card/CardHeader.vue
var CardHeader_default = /* @__PURE__ */ defineComponent({
	__name: "CardHeader",
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
				"data-slot": "card-header",
				class: normalizeClass(unref(cn)("@container/card-header grid auto-rows-min grid-rows-[auto_auto] items-start gap-1.5 px-6 has-data-[slot=card-action]:grid-cols-[1fr_auto] [.border-b]:pb-6", props.class))
			}, [renderSlot(_ctx.$slots, "default")], 2);
		};
	}
});
//#endregion
//#region resources/js/components/ui/card/CardTitle.vue
var CardTitle_default = /* @__PURE__ */ defineComponent({
	__name: "CardTitle",
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
			return openBlock(), createElementBlock("h3", {
				"data-slot": "card-title",
				class: normalizeClass(unref(cn)("leading-none font-semibold", props.class))
			}, [renderSlot(_ctx.$slots, "default")], 2);
		};
	}
});
//#endregion
export { Card_default as i, CardHeader_default as n, CardContent_default as r, CardTitle_default as t };

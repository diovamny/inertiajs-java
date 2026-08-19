import { C as useForwardExpose, c as cn, o as reactiveOmit, v as Primitive } from "./createLucideIcon.js";
import { $t as openBlock, Mn as withCtx, mt as defineComponent, on as renderSlot, pr as unref, rt as createBlock, zt as mergeProps } from "./dist.js";
//#region node_modules/reka-ui/dist/Label/Label.js
var Label_default$1 = /* @__PURE__ */ defineComponent({
	__name: "Label",
	props: {
		for: {
			type: String,
			required: false
		},
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false,
			default: "label"
		}
	},
	setup(__props) {
		const props = __props;
		useForwardExpose();
		return (_ctx, _cache) => {
			return openBlock(), createBlock(unref(Primitive), mergeProps(props, { onMousedown: _cache[0] || (_cache[0] = (event) => {
				if (!event.defaultPrevented && event.detail > 1) event.preventDefault();
			}) }), {
				default: withCtx(() => [renderSlot(_ctx.$slots, "default")]),
				_: 3
			}, 16);
		};
	}
});
//#endregion
//#region resources/js/components/ui/label/Label.vue
var Label_default = /* @__PURE__ */ defineComponent({
	__name: "Label",
	props: {
		for: {},
		asChild: { type: Boolean },
		as: {},
		class: { type: [
			Boolean,
			null,
			String,
			Object,
			Array
		] }
	},
	setup(__props) {
		const props = __props;
		const delegatedProps = reactiveOmit(props, "class");
		return (_ctx, _cache) => {
			return openBlock(), createBlock(unref(Label_default$1), mergeProps({ "data-slot": "label" }, unref(delegatedProps), { class: unref(cn)("flex items-center gap-2 text-sm leading-none font-medium select-none group-data-[disabled=true]:pointer-events-none group-data-[disabled=true]:opacity-50 peer-disabled:cursor-not-allowed peer-disabled:opacity-50", props.class) }), {
				default: withCtx(() => [renderSlot(_ctx.$slots, "default")]),
				_: 3
			}, 16, ["class"]);
		};
	}
});
//#endregion
export { Label_default as t };

import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, R as withKeys, at as createElementBlock, c as setLayoutProps, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, rr as ref, s as resetLayoutProps } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Label_default } from "./Label.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Input_default } from "./Input.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Layouts/LayoutProps.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "space-y-2" };
var _hoisted_5 = { class: "flex gap-2" };
var _hoisted_6 = { class: "flex gap-2" };
var _hoisted_7 = { class: "space-y-3" };
var _hoisted_8 = { class: "space-y-2" };
//#endregion
//#region resources/js/pages/Features/Layouts/LayoutProps.vue
var LayoutProps_default = /* @__PURE__ */ defineComponent({
	layout: [AppLayout_default, { breadcrumbs: [{ title: "Layouts & Head" }, { title: "Layout Props" }] }],
	__name: "LayoutProps",
	setup(__props) {
		const subtitle = ref("");
		function apply() {
			if (subtitle.value) setLayoutProps({ subtitle: subtitle.value });
		}
		function reset() {
			subtitle.value = "";
			resetLayoutProps();
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Layout Props" }), createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
				title: "Layout Props",
				docs: "the-basics/layouts#layout-props",
				controller: "app/Http/Controllers/Feature/LayoutController.php#L30"
			}, {
				default: withCtx(() => [..._cache[3] || (_cache[3] = [
					createTextVNode(" Pass data from pages to layouts with ", -1),
					createBaseVNode("code", { class: "text-xs" }, "setLayoutProps()", -1),
					createTextVNode(" and regular component props. ", -1)
				])]),
				_: 1
			}), createBaseVNode("div", _hoisted_2, [createVNode(FeatureCard_default, {
				title: "setLayoutProps()",
				description: "Type a subtitle and click Apply. A banner appears below the breadcrumbs."
			}, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_3, [
					createBaseVNode("div", _hoisted_4, [createVNode(unref(Label_default), { for: "subtitle" }, {
						default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode("Subtitle", -1)])]),
						_: 1
					}), createBaseVNode("div", _hoisted_5, [createVNode(unref(Input_default), {
						id: "subtitle",
						modelValue: subtitle.value,
						"onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => subtitle.value = $event),
						placeholder: "Enter a subtitle...",
						onKeyup: withKeys(apply, ["enter"])
					}, null, 8, ["modelValue"]), createVNode(unref(Button_default), { onClick: apply }, {
						default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode("Apply", -1)])]),
						_: 1
					})])]),
					createBaseVNode("div", _hoisted_6, [
						createVNode(unref(Button_default), {
							variant: "outline",
							size: "sm",
							onClick: _cache[1] || (_cache[1] = ($event) => {
								subtitle.value = "Welcome back!";
								apply();
							})
						}, {
							default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" \"Welcome back!\" ", -1)])]),
							_: 1
						}),
						createVNode(unref(Button_default), {
							variant: "outline",
							size: "sm",
							onClick: _cache[2] || (_cache[2] = ($event) => {
								subtitle.value = "Maintenance scheduled tonight";
								apply();
							})
						}, {
							default: withCtx(() => [..._cache[7] || (_cache[7] = [createTextVNode(" \"Maintenance...\" ", -1)])]),
							_: 1
						}),
						createVNode(unref(Button_default), {
							variant: "outline",
							size: "sm",
							onClick: reset
						}, {
							default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode(" Reset ", -1)])]),
							_: 1
						})
					]),
					_cache[9] || (_cache[9] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
						createTextVNode(" The layout declares a "),
						createBaseVNode("code", null, "subtitle"),
						createTextVNode(" prop with a default value. Pages override it with "),
						createBaseVNode("code", null, "setLayoutProps()"),
						createTextVNode(". Dynamic props auto-reset on navigation. ")
					], -1))
				])]),
				_: 1
			}), createVNode(FeatureCard_default, {
				"info-card": "",
				title: "API Reference",
				description: "The complete layout props API."
			}, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_7, [createBaseVNode("div", _hoisted_8, [
					createVNode(CodeBlock_default, {
						title: "In Layout:",
						code: "defineProps({\n  subtitle: {\n    type: String,\n    default: '',\n  },\n})"
					}),
					createVNode(CodeBlock_default, {
						title: "In Page:",
						code: "setLayoutProps({\n  subtitle: 'Custom subtitle',\n})"
					}),
					createVNode(CodeBlock_default, {
						title: "Reset:",
						code: "resetLayoutProps()"
					})
				])])]),
				_: 1
			})])])], 64);
		};
	}
});
//#endregion
export { LayoutProps_default as default };

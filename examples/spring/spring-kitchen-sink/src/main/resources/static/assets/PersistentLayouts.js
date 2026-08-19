import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, rr as ref, yr as toDisplayString } from "./dist.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as PersistentDemoLayout_default } from "./PersistentDemoLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Layouts/PersistentLayouts.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_2 = { class: "space-y-4" };
var _hoisted_3 = { class: "flex items-center gap-4" };
var _hoisted_4 = { class: "space-y-3" };
//#endregion
//#region resources/js/pages/Features/Layouts/PersistentLayouts.vue
var PersistentLayouts_default = /* @__PURE__ */ defineComponent({
	layout: PersistentDemoLayout_default,
	__name: "PersistentLayouts",
	setup(__props) {
		const pageCounter = ref(0);
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Persistent Layouts" }), createBaseVNode("div", _hoisted_1, [createVNode(FeatureCard_default, {
				title: "Page 1 State",
				description: "This state belongs to the page component. It resets every time you navigate."
			}, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_2, [createBaseVNode("div", _hoisted_3, [
					_cache[4] || (_cache[4] = createBaseVNode("span", { class: "text-sm font-medium" }, "Page counter:", -1)),
					createVNode(unref(Button_default), {
						variant: "outline",
						size: "sm",
						class: "size-7 p-0",
						onClick: _cache[0] || (_cache[0] = ($event) => pageCounter.value--)
					}, {
						default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode(" - ", -1)])]),
						_: 1
					}),
					createVNode(unref(Badge_default), {
						variant: "secondary",
						class: "font-mono text-base"
					}, {
						default: withCtx(() => [createTextVNode(toDisplayString(pageCounter.value), 1)]),
						_: 1
					}),
					createVNode(unref(Button_default), {
						variant: "outline",
						size: "sm",
						class: "size-7 p-0",
						onClick: _cache[1] || (_cache[1] = ($event) => pageCounter.value++)
					}, {
						default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" + ", -1)])]),
						_: 1
					})
				]), _cache[5] || (_cache[5] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Increment this counter, switch to Page 2, then come back. This resets to 0 because the page component remounts. The stopwatch and counter above persist because they live in the layout. ", -1))])]),
				_: 1
			}), createVNode(FeatureCard_default, {
				"info-card": "",
				title: "How It Works",
				description: "Pages declare their layout with defineOptions instead of wrapping content in the template."
			}, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_4, [
					createVNode(CodeBlock_default, {
						title: "Persistent (this page):",
						code: "defineOptions({\n  layout: PersistentDemoLayout,\n})\n\n// No layout wrapper in template"
					}),
					createVNode(CodeBlock_default, { title: "Standard (non-persistent):" }, {
						default: withCtx(() => [..._cache[6] || (_cache[6] = [createBaseVNode("textarea", null, "<AppLayout :breadcrumbs=\"...\">\n  <!-- page content -->\n</AppLayout>\n                    ", -1)])]),
						_: 1
					}),
					_cache[7] || (_cache[7] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " With persistent layouts, Inertia keeps the layout instance mounted and only swaps the page slot content. Layout state, intervals, and event listeners all survive navigation. ", -1))
				])]),
				_: 1
			})])], 64);
		};
	}
});
//#endregion
export { PersistentLayouts_default as default };

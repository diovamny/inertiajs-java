import { $t as openBlock, G as Fragment, Mn as withCtx, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Layouts/NestedLayouts.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-3" };
var _hoisted_4 = { class: "grid gap-4 sm:grid-cols-2" };
//#endregion
//#region resources/js/pages/Features/Layouts/NestedLayouts.vue
var NestedLayouts_default = /* @__PURE__ */ defineComponent({
	__name: "NestedLayouts",
	setup(__props) {
		const breadcrumbs = [{ title: "Layouts & Head" }, { title: "Nested Layouts" }];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Nested Layouts" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Nested Layouts",
					docs: "the-basics/layouts#nested-layouts",
					controller: "app/Http/Controllers/Feature/LayoutController.php#L20"
				}, {
					default: withCtx(() => [..._cache[0] || (_cache[0] = [
						createTextVNode(" Multi-level layout nesting using ", -1),
						createBaseVNode("code", { class: "text-xs" }, "defineOptions({ layout: [Outer, Inner] })", -1),
						createTextVNode(". ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						title: "How Nesting Works",
						description: "Pass an array of layouts to nest them from outermost to innermost."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createVNode(CodeBlock_default, null, {
							default: withCtx(() => [..._cache[1] || (_cache[1] = [createBaseVNode("textarea", null, "                                import AppLayout from \"./AppLayout.vue\"\n                                import SectionLayout from \"./SectionLayout.vue\"\n\n                                defineOptions({\n                                  layout: [AppLayout, SectionLayout],\n                                })\n                            ", -1)])]),
							_: 1
						}), _cache[2] || (_cache[2] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
							createTextVNode(" This renders as: "),
							createBaseVNode("code", null, "AppLayout > SectionLayout > Page Content"),
							createTextVNode(". Each layout uses "),
							createBaseVNode("code", null, "<slot />"),
							createTextVNode(" for its child content. ")
						], -1))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Nesting Visualization",
						description: "How nested layouts wrap around page content."
					}, {
						default: withCtx(() => [..._cache[3] || (_cache[3] = [createBaseVNode("div", { class: "rounded-md border-2 border-blue-300 p-3 dark:border-blue-700" }, [createBaseVNode("p", { class: "mb-2 text-xs font-semibold text-blue-600 dark:text-blue-400" }, " App Layout (outermost) "), createBaseVNode("div", { class: "rounded-md border-2 border-green-300 p-3 dark:border-green-700" }, [createBaseVNode("p", { class: "mb-2 text-xs font-semibold text-green-600 dark:text-green-400" }, " Section Layout (middle) "), createBaseVNode("div", { class: "rounded-md border-2 border-orange-300 p-3 dark:border-orange-700" }, [createBaseVNode("p", { class: "text-xs font-semibold text-orange-600 dark:text-orange-400" }, " Page Content (innermost) "), createBaseVNode("p", { class: "mt-1 text-xs text-muted-foreground" }, " This is where your page component renders. ")])])], -1)])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "This App's Layout Structure",
						description: "How this demo app uses layouts."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_4, [createBaseVNode("div", null, [createVNode(CodeBlock_default, { title: "Standard pages:" }, {
							default: withCtx(() => [..._cache[4] || (_cache[4] = [createBaseVNode("textarea", null, "                                    <AppLayout :breadcrumbs=\"...\">\n                                      <!-- page content -->\n                                    </AppLayout>\n                                ", -1)])]),
							_: 1
						}), _cache[5] || (_cache[5] = createBaseVNode("p", { class: "mt-2 text-xs text-muted-foreground" }, " Single layout wrapping page content directly in the template. ", -1))]), createBaseVNode("div", null, [createVNode(CodeBlock_default, { title: "Persistent nested:" }, {
							default: withCtx(() => [..._cache[6] || (_cache[6] = [createBaseVNode("textarea", null, "                                    defineOptions({\n                                      layout: [AppLayout, SectionLayout],\n                                    })\n\n                                    // Template has no layout wrapper\n                                    <template>\n                                      <!-- content only -->\n                                    </template>\n                                ", -1)])]),
							_: 1
						}), _cache[7] || (_cache[7] = createBaseVNode("p", { class: "mt-2 text-xs text-muted-foreground" }, " The template has no layout wrapper. Inertia manages both layouts externally. ", -1))])])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { NestedLayouts_default as default };

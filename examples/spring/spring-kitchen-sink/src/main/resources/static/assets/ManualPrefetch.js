import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Prefetching/ManualPrefetch.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
//#endregion
//#region resources/js/pages/Features/Prefetching/ManualPrefetch.vue
var ManualPrefetch_default = /* @__PURE__ */ defineComponent({
	__name: "ManualPrefetch",
	setup(__props) {
		const breadcrumbs = [{ title: "Prefetching" }, { title: "Manual Prefetch" }];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Manual Prefetch" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Manual Prefetch",
					docs: "data-props/prefetching#programmatic-prefetching",
					controller: "app/Http/Controllers/Feature/PrefetchingController.php#L20"
				}, {
					default: withCtx(() => [..._cache[1] || (_cache[1] = [
						createTextVNode(" Programmatic prefetching with ", -1),
						createBaseVNode("code", { class: "text-xs" }, "router.prefetch()", -1),
						createTextVNode(". ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [createVNode(FeatureCard_default, {
					title: "router.prefetch()",
					description: "Programmatically prefetch any URL with custom options."
				}, {
					default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createVNode(unref(Button_default), {
						variant: "outline",
						onClick: _cache[0] || (_cache[0] = ($event) => unref(router).prefetch("/contacts"))
					}, {
						default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode(" Prefetch Contacts ", -1)])]),
						_: 1
					}), _cache[3] || (_cache[3] = createBaseVNode("div", { class: "rounded-lg border border-black/5 bg-neutral-50/80 p-3 font-mono text-xs dark:border-white/5 dark:bg-neutral-900/80" }, [createBaseVNode("p", null, " Open DevTools Network tab, click the button, then navigate to Contacts in the sidebar. The page will load instantly from the prefetch cache. ")], -1))])]),
					_: 1
				})])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { ManualPrefetch_default as default };

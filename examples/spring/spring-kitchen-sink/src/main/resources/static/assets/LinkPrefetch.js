import { $t as openBlock, G as Fragment, Mn as withCtx, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Prefetching/LinkPrefetch.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-3" };
var _hoisted_4 = { class: "space-y-3" };
var _hoisted_5 = { class: "space-y-3" };
var _hoisted_6 = { class: "space-y-3" };
//#endregion
//#region resources/js/pages/Features/Prefetching/LinkPrefetch.vue
var LinkPrefetch_default = /* @__PURE__ */ defineComponent({
	__name: "LinkPrefetch",
	setup(__props) {
		const breadcrumbs = [{ title: "Prefetching" }, { title: "Link Prefetch" }];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Link Prefetch" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Link Prefetch",
					docs: "data-props/prefetching#link-prefetching",
					controller: "app/Http/Controllers/Feature/PrefetchingController.php#L10"
				}, {
					default: withCtx(() => [..._cache[0] || (_cache[0] = [createTextVNode(" Automatic prefetching on mount, hover, and click. Open DevTools Network tab to observe prefetch requests. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, { title: "Hover Prefetch (default)" }, {
						description: withCtx(() => [..._cache[1] || (_cache[1] = [createBaseVNode("code", { class: "text-xs" }, "prefetch", -1), createTextVNode(". Prefetches after 75ms hover delay. ", -1)])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createVNode(unref(link_default), {
							href: "/contacts",
							prefetch: "",
							class: "inline-flex items-center rounded-md border border-black/10 bg-background px-4 py-2 text-sm font-medium hover:bg-accent dark:border-white/10"
						}, {
							default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode(" Contacts ", -1)])]),
							_: 1
						}), _cache[3] || (_cache[3] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Hover over the link and watch the Network tab. Data loads before you click. ", -1))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "Click Prefetch" }, {
						description: withCtx(() => [..._cache[4] || (_cache[4] = [createBaseVNode("code", { class: "text-xs" }, "prefetch=\"click\"", -1), createTextVNode(". Prefetches on mousedown, loads by the time click fires. ", -1)])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_4, [createVNode(unref(link_default), {
							href: "/organizations",
							prefetch: "click",
							class: "inline-flex items-center rounded-md border border-black/10 bg-background px-4 py-2 text-sm font-medium hover:bg-accent dark:border-white/10"
						}, {
							default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" Organizations ", -1)])]),
							_: 1
						}), _cache[6] || (_cache[6] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Data starts loading on mousedown. The page loads almost instantly. ", -1))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "Mount Prefetch" }, {
						description: withCtx(() => [..._cache[7] || (_cache[7] = [createBaseVNode("code", { class: "text-xs" }, "prefetch=\"mount\"", -1), createTextVNode(". Prefetches immediately when the component mounts. ", -1)])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_5, [createVNode(unref(link_default), {
							href: "/contacts/create",
							prefetch: "mount",
							class: "inline-flex items-center rounded-md border border-black/10 bg-background px-4 py-2 text-sm font-medium hover:bg-accent dark:border-white/10"
						}, {
							default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode(" Create Contact ", -1)])]),
							_: 1
						}), _cache[9] || (_cache[9] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " This link's page data was prefetched as soon as this page loaded. ", -1))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "Cache Duration" }, {
						description: withCtx(() => [..._cache[10] || (_cache[10] = [createBaseVNode("code", { class: "text-xs" }, "cacheFor=\"10s\"", -1), createTextVNode(". Controls how long prefetched data stays cached. ", -1)])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_6, [createVNode(unref(link_default), {
							href: "/dashboard",
							prefetch: "",
							cacheFor: "10s",
							class: "inline-flex items-center rounded-md border border-black/10 bg-background px-4 py-2 text-sm font-medium hover:bg-accent dark:border-white/10"
						}, {
							default: withCtx(() => [..._cache[11] || (_cache[11] = [createTextVNode(" Dashboard (10s cache) ", -1)])]),
							_: 1
						}), _cache[12] || (_cache[12] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Hover to prefetch, then hover again after 10 seconds to see a new request in the Network tab. Default cache duration is 30 seconds. ", -1))])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { LinkPrefetch_default as default };

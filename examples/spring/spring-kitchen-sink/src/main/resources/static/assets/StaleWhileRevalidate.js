import { $t as openBlock, G as Fragment, Mn as withCtx, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Prefetching/StaleWhileRevalidate.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-3" };
//#endregion
//#region resources/js/pages/Features/Prefetching/StaleWhileRevalidate.vue
var StaleWhileRevalidate_default = /* @__PURE__ */ defineComponent({
	__name: "StaleWhileRevalidate",
	setup(__props) {
		const breadcrumbs = [{ title: "Prefetching" }, { title: "Stale While Revalidate" }];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Stale While Revalidate" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Stale While Revalidate",
					docs: "data-props/prefetching#stale-while-revalidate",
					controller: "app/Http/Controllers/Feature/PrefetchingController.php#L15"
				}, {
					default: withCtx(() => [..._cache[0] || (_cache[0] = [createTextVNode(" SWR caching strategy. Serve stale data instantly while revalidating in the background. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [createVNode(FeatureCard_default, { title: "SWR Link" }, {
					description: withCtx(() => [..._cache[1] || (_cache[1] = [createBaseVNode("code", { class: "text-xs" }, ":cacheFor=\"['10s', '20s']\"", -1), createTextVNode(". Prefetched on mount. 10s fresh, then stale for up to 20s. ", -1)])]),
					default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createVNode(unref(link_default), {
						href: "/contacts",
						prefetch: "mount",
						cacheFor: ["10s", "20s"],
						class: "inline-flex items-center rounded-md border border-black/10 bg-background px-4 py-2 text-sm font-medium hover:bg-accent dark:border-white/10"
					}, {
						default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode(" Contacts (10s/20s SWR) ", -1)])]),
						_: 1
					}), _cache[3] || (_cache[3] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Prefetched on mount. Within 10s, clicking navigates instantly from cache. Between 10-20s, stale data is served immediately and the page updates with fresh data. After 20s, the cache is gone. ", -1))])]),
					_: 1
				}), createVNode(FeatureCard_default, { title: "How SWR Works" }, {
					description: withCtx(() => [..._cache[4] || (_cache[4] = [
						createTextVNode(" Pass a tuple to ", -1),
						createBaseVNode("code", { class: "text-xs" }, "cacheFor", -1),
						createTextVNode(": ", -1),
						createBaseVNode("code", { class: "text-xs" }, "[freshPeriod, stalePeriod]", -1),
						createTextVNode(". ", -1)
					])]),
					default: withCtx(() => [_cache[5] || (_cache[5] = createBaseVNode("div", { class: "space-y-3" }, [createBaseVNode("div", { class: "rounded-md bg-muted p-4 text-sm" }, [createBaseVNode("ol", { class: "list-inside list-decimal space-y-2" }, [
						createBaseVNode("li", null, [createBaseVNode("strong", null, "Fresh period"), createTextVNode(": cached data is returned immediately, no request made. ")]),
						createBaseVNode("li", null, [createBaseVNode("strong", null, "Stale period"), createTextVNode(": stale cached data is served instantly for immediate navigation. The page updates with fresh data from the server response. ")]),
						createBaseVNode("li", null, [createBaseVNode("strong", null, "Expired"), createTextVNode(": cache is gone, a full server request happens. ")])
					])])], -1))]),
					_: 1
				})])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { StaleWhileRevalidate_default as default };

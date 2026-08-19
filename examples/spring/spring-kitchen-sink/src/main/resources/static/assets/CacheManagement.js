import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Prefetching/CacheManagement.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "space-y-2" };
var _hoisted_5 = { class: "flex flex-wrap gap-2" };
var _hoisted_6 = { class: "space-y-4" };
var _hoisted_7 = { class: "space-y-2" };
var _hoisted_8 = { class: "flex flex-wrap gap-2" };
var _hoisted_9 = { class: "space-y-2" };
//#endregion
//#region resources/js/pages/Features/Prefetching/CacheManagement.vue
var CacheManagement_default = /* @__PURE__ */ defineComponent({
	__name: "CacheManagement",
	setup(__props) {
		const breadcrumbs = [{ title: "Prefetching" }, { title: "Cache Management" }];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Cache Management" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Cache Management",
					docs: "data-props/prefetching#cache-invalidation",
					controller: "app/Http/Controllers/Feature/PrefetchingController.php#L25"
				}, {
					default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode(" Tag-based cache system with targeted invalidation. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [createVNode(FeatureCard_default, { title: "Cache Tags" }, {
					description: withCtx(() => [..._cache[5] || (_cache[5] = [
						createTextVNode(" Tag prefetched data with ", -1),
						createBaseVNode("code", { class: "text-xs" }, "cache-tags", -1),
						createTextVNode(" for targeted invalidation. ", -1)
					])]),
					default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [
						_cache[12] || (_cache[12] = createBaseVNode("h4", { class: "text-sm font-semibold" }, "Tagged Links", -1)),
						createBaseVNode("div", _hoisted_5, [
							createVNode(unref(link_default), {
								href: "/contacts",
								prefetch: "",
								"cache-tags": "crm",
								class: "inline-flex items-center gap-1.5 rounded-md border border-black/10 bg-background px-3 py-1.5 text-sm hover:bg-accent dark:border-white/10"
							}, {
								default: withCtx(() => [_cache[7] || (_cache[7] = createTextVNode(" Contacts ", -1)), createVNode(unref(Badge_default), {
									variant: "outline",
									class: "text-xs"
								}, {
									default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode("crm", -1)])]),
									_: 1
								})]),
								_: 1
							}),
							createVNode(unref(link_default), {
								href: "/organizations",
								prefetch: "",
								"cache-tags": ["crm", "orgs"],
								class: "inline-flex items-center gap-1.5 rounded-md border border-black/10 bg-background px-3 py-1.5 text-sm hover:bg-accent dark:border-white/10"
							}, {
								default: withCtx(() => [_cache[9] || (_cache[9] = createTextVNode(" Organizations ", -1)), createVNode(unref(Badge_default), {
									variant: "outline",
									class: "text-xs"
								}, {
									default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode("crm, orgs", -1)])]),
									_: 1
								})]),
								_: 1
							}),
							createVNode(unref(link_default), {
								href: "/dashboard",
								prefetch: "",
								"cache-tags": "dashboard",
								class: "inline-flex items-center gap-1.5 rounded-md border border-black/10 bg-background px-3 py-1.5 text-sm hover:bg-accent dark:border-white/10"
							}, {
								default: withCtx(() => [_cache[11] || (_cache[11] = createTextVNode(" Dashboard ", -1)), createVNode(unref(Badge_default), {
									variant: "outline",
									class: "text-xs"
								}, {
									default: withCtx(() => [..._cache[10] || (_cache[10] = [createTextVNode("dashboard", -1)])]),
									_: 1
								})]),
								_: 1
							})
						]),
						_cache[13] || (_cache[13] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Hover to prefetch, then use the invalidation buttons to flush by tag. ", -1))
					])])]),
					_: 1
				}), createVNode(FeatureCard_default, {
					title: "Invalidation",
					description: "Flush cached data by tag or flush all."
				}, {
					default: withCtx(() => [createBaseVNode("div", _hoisted_6, [
						createBaseVNode("div", _hoisted_7, [_cache[17] || (_cache[17] = createBaseVNode("h4", { class: "text-sm font-semibold" }, "Flush by Tag", -1)), createBaseVNode("div", _hoisted_8, [
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[0] || (_cache[0] = ($event) => unref(router).flushByCacheTags("crm"))
							}, {
								default: withCtx(() => [..._cache[14] || (_cache[14] = [createTextVNode(" Flush \"crm\" ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[1] || (_cache[1] = ($event) => unref(router).flushByCacheTags("orgs"))
							}, {
								default: withCtx(() => [..._cache[15] || (_cache[15] = [createTextVNode(" Flush \"orgs\" ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[2] || (_cache[2] = ($event) => unref(router).flushByCacheTags("dashboard"))
							}, {
								default: withCtx(() => [..._cache[16] || (_cache[16] = [createTextVNode(" Flush \"dashboard\" ", -1)])]),
								_: 1
							})
						])]),
						createBaseVNode("div", _hoisted_9, [_cache[19] || (_cache[19] = createBaseVNode("h4", { class: "text-sm font-semibold" }, "Flush All", -1)), createVNode(unref(Button_default), {
							variant: "destructive",
							size: "sm",
							onClick: _cache[3] || (_cache[3] = ($event) => unref(router).flushAll())
						}, {
							default: withCtx(() => [..._cache[18] || (_cache[18] = [createTextVNode(" Flush All Cache ", -1)])]),
							_: 1
						})]),
						_cache[20] || (_cache[20] = createBaseVNode("div", { class: "rounded-lg border border-black/5 bg-neutral-50/80 p-3 font-mono text-xs dark:border-white/5 dark:bg-neutral-900/80" }, [createBaseVNode("p", null, [
							createBaseVNode("strong", null, "Tip:"),
							createTextVNode(" Forms can auto-invalidate cache on submission with "),
							createBaseVNode("code", null, ":invalidate-cache-tags=\"['tag']\""),
							createTextVNode(". ")
						])], -1))
					])]),
					_: 1
				})])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { CacheManagement_default as default };

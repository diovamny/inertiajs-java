import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, gr as normalizeClass, i as head_default, it as createCommentVNode, mt as defineComponent, n as deferred_default, nt as createBaseVNode, pr as unref, rt as createBlock, vr as normalizeStyle, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/DataLoading/DeferredProps.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-3" };
var _hoisted_3 = { class: "space-y-2" };
var _hoisted_4 = { class: "flex items-center justify-between" };
var _hoisted_5 = { class: "flex items-center justify-between" };
var _hoisted_6 = { class: "space-y-2" };
var _hoisted_7 = { class: "space-y-1.5" };
var _hoisted_8 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_9 = { class: "flex items-center gap-2" };
var _hoisted_10 = { class: "text-sm" };
var _hoisted_11 = { class: "text-sm" };
var _hoisted_12 = { class: "rounded border border-emerald-500/30 bg-emerald-500/5 p-3 text-sm" };
var _hoisted_13 = { class: "text-xs" };
//#endregion
//#region resources/js/pages/Features/DataLoading/DeferredProps.vue
var DeferredProps_default = /* @__PURE__ */ defineComponent({
	__name: "DeferredProps",
	props: {
		quickStat: {},
		slowStats: {},
		heavyData: {},
		flakyReport: {}
	},
	setup(__props) {
		const breadcrumbs = [{ title: "Data Loading" }, { title: "Deferred Props" }];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Deferred Props" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [
					createVNode(FeatureHeader_default, {
						title: "Deferred Props",
						docs: "data-props/deferred-props",
						controller: "app/Http/Controllers/Feature/DataLoadingController.php#L16"
					}, {
						default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode(" Lazy-loaded props with skeleton fallbacks. Expensive data loads after the initial page render. ", -1)])]),
						_: 1
					}),
					createBaseVNode("div", _hoisted_2, [
						createVNode(FeatureCard_default, {
							title: "Instant Prop",
							description: "Loaded immediately with the page response."
						}, {
							default: withCtx(() => [createVNode(unref(Badge_default), null, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.quickStat), 1)]),
								_: 1
							})]),
							_: 1
						}),
						createVNode(FeatureCard_default, { title: "Deferred Stats" }, {
							description: withCtx(() => [..._cache[3] || (_cache[3] = [createBaseVNode("code", { class: "text-xs" }, "Inertia::defer()", -1), createTextVNode(". Default group, ~800ms delay. ", -1)])]),
							default: withCtx(() => [createVNode(unref(deferred_default), { data: "slowStats" }, {
								fallback: withCtx(() => [..._cache[4] || (_cache[4] = [createBaseVNode("div", { class: "space-y-3" }, [createBaseVNode("div", { class: "h-4 w-3/4 animate-pulse rounded bg-muted" }), createBaseVNode("div", { class: "h-4 w-1/2 animate-pulse rounded bg-muted" })], -1)])]),
								default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [_cache[5] || (_cache[5] = createBaseVNode("span", { class: "text-sm" }, "Total Contacts", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
									default: withCtx(() => [createTextVNode(toDisplayString(__props.slowStats?.totalContacts), 1)]),
									_: 1
								})]), createBaseVNode("div", _hoisted_5, [_cache[6] || (_cache[6] = createBaseVNode("span", { class: "text-sm" }, "Total Favorites", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
									default: withCtx(() => [createTextVNode(toDisplayString(__props.slowStats?.totalFavorites), 1)]),
									_: 1
								})])])]),
								_: 1
							})]),
							_: 1
						}),
						createVNode(FeatureCard_default, { title: "Heavy Data" }, {
							description: withCtx(() => [..._cache[7] || (_cache[7] = [createBaseVNode("code", { class: "text-xs" }, "Inertia::defer(fn, 'heavy')", -1), createTextVNode(". Named group, ~1.5s delay. ", -1)])]),
							default: withCtx(() => [createVNode(unref(deferred_default), { data: "heavyData" }, {
								fallback: withCtx(() => [createBaseVNode("div", _hoisted_6, [(openBlock(), createElementBlock(Fragment, null, renderList(5, (i) => {
									return createBaseVNode("div", {
										key: i,
										class: "h-4 animate-pulse rounded bg-muted",
										style: normalizeStyle({ width: `${60 + Math.random() * 40}%` })
									}, null, 4);
								}), 64))])]),
								default: withCtx(() => [createBaseVNode("div", _hoisted_7, [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.heavyData, (contact) => {
									return openBlock(), createElementBlock("div", {
										key: contact.id,
										class: "flex items-center justify-between rounded bg-muted/50 px-2 py-1 text-sm"
									}, [createBaseVNode("span", null, toDisplayString(contact.name), 1), createVNode(unref(Badge_default), {
										variant: "outline",
										class: "text-xs"
									}, {
										default: withCtx(() => [createTextVNode("#" + toDisplayString(contact.id), 1)]),
										_: 2
									}, 1024)]);
								}), 128))])]),
								_: 1
							})]),
							_: 1
						})
					]),
					createBaseVNode("div", _hoisted_8, [createVNode(FeatureCard_default, { title: "Reloading Slot" }, {
						description: withCtx(() => [..._cache[8] || (_cache[8] = [
							createTextVNode(" The ", -1),
							createBaseVNode("code", { class: "text-xs" }, "reloading", -1),
							createTextVNode(" slot prop lets you show stale data with a visual indicator while refreshing. ", -1)
						])]),
						"header-action": withCtx(() => [createVNode(unref(Button_default), {
							variant: "outline",
							onClick: _cache[0] || (_cache[0] = ($event) => unref(router).reload({ only: ["slowStats"] }))
						}, {
							default: withCtx(() => [..._cache[9] || (_cache[9] = [createTextVNode(" Reload Stats ", -1)])]),
							_: 1
						})]),
						default: withCtx(() => [createVNode(unref(deferred_default), { data: "slowStats" }, {
							fallback: withCtx(() => [..._cache[10] || (_cache[10] = [createBaseVNode("div", { class: "space-y-3" }, [createBaseVNode("div", { class: "h-4 w-3/4 animate-pulse rounded bg-muted" }), createBaseVNode("div", { class: "h-4 w-1/2 animate-pulse rounded bg-muted" })], -1)])]),
							default: withCtx(({ reloading }) => [createBaseVNode("div", { class: normalizeClass([{ "opacity-50 transition-opacity": reloading }, "space-y-2"]) }, [createBaseVNode("div", _hoisted_9, [createBaseVNode("span", _hoisted_10, [_cache[11] || (_cache[11] = createTextVNode("Total Contacts: ", -1)), createBaseVNode("strong", null, toDisplayString(__props.slowStats?.totalContacts), 1)]), reloading ? (openBlock(), createBlock(unref(Badge_default), {
								key: 0,
								variant: "secondary",
								class: "text-xs"
							}, {
								default: withCtx(() => [..._cache[12] || (_cache[12] = [createTextVNode("Refreshing...", -1)])]),
								_: 1
							})) : createCommentVNode("", true)]), createBaseVNode("div", _hoisted_11, [_cache[13] || (_cache[13] = createTextVNode(" Total Favorites: ", -1)), createBaseVNode("strong", null, toDisplayString(__props.slowStats?.totalFavorites), 1)])], 2)]),
							_: 1
						})]),
						_: 1
					}), createVNode(FeatureCard_default, {
						title: "Rescued Deferred Prop",
						badge: "v3.1"
					}, {
						description: withCtx(() => [..._cache[14] || (_cache[14] = [
							createBaseVNode("code", { class: "text-xs" }, "Inertia::defer(fn, rescue: true)", -1),
							createTextVNode(". When the closure throws, the prop is marked rescued and the ", -1),
							createBaseVNode("code", { class: "text-xs" }, "rescue", -1),
							createTextVNode(" slot renders instead of the fallback. Retry sends an ", -1),
							createBaseVNode("code", { class: "text-xs" }, "X-Force-Success", -1),
							createTextVNode(" header so the server returns data instead of throwing. ", -1)
						])]),
						default: withCtx(() => [createVNode(unref(deferred_default), { data: "flakyReport" }, {
							fallback: withCtx(() => [..._cache[15] || (_cache[15] = [createBaseVNode("div", { class: "space-y-3" }, [createBaseVNode("div", { class: "h-4 w-2/3 animate-pulse rounded bg-muted" }), createBaseVNode("div", { class: "h-4 w-1/3 animate-pulse rounded bg-muted" })], -1)])]),
							rescue: withCtx(({ reloading }) => [createBaseVNode("div", { class: normalizeClass([{ "opacity-50 transition-opacity": reloading }, "space-y-3 rounded border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive"]) }, [_cache[16] || (_cache[16] = createBaseVNode("div", null, [createBaseVNode("div", { class: "font-medium" }, " Failed to load report "), createBaseVNode("div", { class: "text-xs opacity-80" }, " Server threw during deferred resolution. ")], -1)), createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								disabled: reloading,
								onClick: _cache[1] || (_cache[1] = ($event) => unref(router).reload({
									only: ["flakyReport"],
									headers: { "X-Force-Success": "1" }
								}))
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(reloading ? "Retrying..." : "Retry"), 1)]),
								_: 2
							}, 1032, ["disabled"])], 2)]),
							default: withCtx(() => [createBaseVNode("div", _hoisted_12, [_cache[18] || (_cache[18] = createBaseVNode("div", { class: "font-medium text-emerald-700 dark:text-emerald-400" }, " Report loaded ", -1)), createBaseVNode("div", _hoisted_13, [_cache[17] || (_cache[17] = createTextVNode(" Value: ", -1)), createBaseVNode("strong", null, toDisplayString(__props.flakyReport?.value), 1)])])]),
							_: 1
						})]),
						_: 1
					})])
				])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { DeferredProps_default as default };

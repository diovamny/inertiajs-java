import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/DataLoading/OnceProps.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "flex flex-wrap gap-2" };
var _hoisted_5 = { class: "rounded-lg border border-black/5 bg-neutral-50/80 p-3 font-mono text-xs dark:border-white/5 dark:bg-neutral-900/80" };
var _hoisted_6 = { class: "list-inside list-disc space-y-1" };
var _hoisted_7 = { class: "space-y-3" };
var _hoisted_8 = { class: "flex items-center justify-between" };
var _hoisted_9 = { class: "flex items-center justify-between" };
var _hoisted_10 = { class: "space-y-3" };
var _hoisted_11 = { class: "flex items-center justify-between" };
var _hoisted_12 = { class: "flex items-center justify-between" };
var _hoisted_13 = { class: "space-y-3" };
var _hoisted_14 = { class: "flex items-center justify-between" };
var _hoisted_15 = { class: "flex items-center justify-between" };
var _hoisted_16 = { class: "space-y-3" };
var _hoisted_17 = { class: "flex items-center justify-between" };
var _hoisted_18 = { class: "flex items-center justify-between" };
var _hoisted_19 = { class: "space-y-3" };
var _hoisted_20 = { class: "flex items-center justify-between" };
var _hoisted_21 = { class: "flex items-center justify-between" };
var _hoisted_22 = { class: "space-y-3 text-xs" };
//#endregion
//#region resources/js/pages/Features/DataLoading/OnceProps.vue
var OnceProps_default = /* @__PURE__ */ defineComponent({
	__name: "OnceProps",
	props: {
		page: {},
		staticData: {},
		freshData: {},
		expiringData: {},
		aliasedData: {},
		dynamicData: {}
	},
	setup(__props) {
		const props = __props;
		const breadcrumbs = [{ title: "Data Loading" }, { title: "Once Props" }];
		const otherPage = () => props.page === 1 ? 2 : 1;
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Once Props" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Once Props",
					docs: "data-props/once-props",
					controller: "app/Http/Controllers/Feature/DataLoadingController.php#L191"
				}, {
					default: withCtx(() => [..._cache[1] || (_cache[1] = [createTextVNode(" Props that resolve once and are remembered by the client. Navigate between pages to see the once prop persist while dynamic props change. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "Navigate Between Pages"
					}, {
						"header-action": withCtx(() => [createVNode(unref(Badge_default), { variant: "secondary" }, {
							default: withCtx(() => [createTextVNode(" Page " + toDisplayString(__props.page), 1)]),
							_: 1
						})]),
						description: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode(" Both pages return the same once props. Navigate between them to see once props remembered while dynamic props update. The server skips the callback entirely on subsequent visits. ", -1)])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [
							createVNode(unref(Button_default), {
								variant: __props.page === 1 ? "default" : "outline",
								"as-child": ""
							}, {
								default: withCtx(() => [createVNode(unref(link_default), { href: "/features/data-loading/once-props/1" }, {
									default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" Page 1 ", -1)])]),
									_: 1
								})]),
								_: 1
							}, 8, ["variant"]),
							createVNode(unref(Button_default), {
								variant: __props.page === 2 ? "default" : "outline",
								"as-child": ""
							}, {
								default: withCtx(() => [createVNode(unref(link_default), { href: "/features/data-loading/once-props/2" }, {
									default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode(" Page 2 ", -1)])]),
									_: 1
								})]),
								_: 1
							}, 8, ["variant"]),
							createVNode(unref(Button_default), {
								variant: "outline",
								onClick: _cache[0] || (_cache[0] = ($event) => unref(router).reload({ only: ["staticData", "expiringData"] }))
							}, {
								default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" Force Refresh Once Prop ", -1)])]),
								_: 1
							})
						]), createBaseVNode("div", _hoisted_5, [createBaseVNode("ul", _hoisted_6, [
							createBaseVNode("li", null, [
								createTextVNode(" Navigate to Page " + toDisplayString(otherPage()) + " and back. The ", 1),
								_cache[6] || (_cache[6] = createBaseVNode("strong", null, "once prop", -1)),
								_cache[7] || (_cache[7] = createTextVNode(" keeps its original timestamp, the ", -1)),
								_cache[8] || (_cache[8] = createBaseVNode("strong", null, "dynamic prop", -1)),
								_cache[9] || (_cache[9] = createTextVNode(" updates. ", -1))
							]),
							_cache[10] || (_cache[10] = createBaseVNode("li", null, [
								createBaseVNode("strong", null, "Force Refresh"),
								createTextVNode(" uses "),
								createBaseVNode("code", null, "router.reload({ only: ['staticData', 'expiringData'] })"),
								createTextVNode(" to explicitly re-resolve once props. ")
							], -1)),
							_cache[11] || (_cache[11] = createBaseVNode("li", null, [
								createBaseVNode("strong", null, ".fresh()"),
								createTextVNode(" re-evaluates on every visit. "),
								createBaseVNode("strong", null, ".until(5s)"),
								createTextVNode(" remembers for 5 seconds. ")
							], -1))
						])])])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "Once Prop" }, {
						description: withCtx(() => [..._cache[12] || (_cache[12] = [createBaseVNode("code", { class: "text-xs" }, "Inertia::once(fn () => ...)", -1), createTextVNode(". Resolved on first load, remembered across navigations. ", -1)])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_7, [createBaseVNode("div", _hoisted_8, [_cache[13] || (_cache[13] = createBaseVNode("span", { class: "text-sm font-medium" }, "Generated At", -1)), createVNode(unref(Badge_default), {
							variant: "outline",
							class: "font-mono text-xs"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.staticData?.generatedAt), 1)]),
							_: 1
						})]), createBaseVNode("div", _hoisted_9, [_cache[14] || (_cache[14] = createBaseVNode("span", { class: "text-sm font-medium" }, "Random ID", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.staticData?.randomId), 1)]),
							_: 1
						})])])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "Dynamic Prop",
						description: "Regular prop, re-evaluated on every visit."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_10, [createBaseVNode("div", _hoisted_11, [_cache[15] || (_cache[15] = createBaseVNode("span", { class: "text-sm font-medium" }, "Timestamp", -1)), createVNode(unref(Badge_default), {
							variant: "outline",
							class: "font-mono text-xs"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.dynamicData.timestamp), 1)]),
							_: 1
						})]), createBaseVNode("div", _hoisted_12, [_cache[16] || (_cache[16] = createBaseVNode("span", { class: "text-sm font-medium" }, "Random Number", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.dynamicData.randomNumber), 1)]),
							_: 1
						})])])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: ".fresh()" }, {
						description: withCtx(() => [..._cache[17] || (_cache[17] = [createBaseVNode("code", { class: "text-xs" }, "Inertia::once(...)->fresh()", -1), createTextVNode(". Forces re-evaluation on every visit. ", -1)])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_13, [
							createBaseVNode("div", _hoisted_14, [_cache[18] || (_cache[18] = createBaseVNode("span", { class: "text-sm font-medium" }, "Generated At", -1)), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "font-mono text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.freshData?.generatedAt ?? "N/A"), 1)]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_15, [_cache[19] || (_cache[19] = createBaseVNode("span", { class: "text-sm font-medium" }, "Value", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.freshData?.value ?? "N/A"), 1)]),
								_: 1
							})]),
							_cache[20] || (_cache[20] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
								createTextVNode(" Changes on every visit because "),
								createBaseVNode("code", null, ".fresh()"),
								createTextVNode(" forces re-resolution. ")
							], -1))
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: ".until(5s)" }, {
						description: withCtx(() => [..._cache[21] || (_cache[21] = [createBaseVNode("code", { class: "text-xs" }, "Inertia::once(...)->until(now()->addSeconds(5))", -1), createTextVNode(". Remembered for 5 seconds, then re-evaluated on the next visit. ", -1)])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_16, [
							createBaseVNode("div", _hoisted_17, [_cache[22] || (_cache[22] = createBaseVNode("span", { class: "text-sm font-medium" }, "Generated At", -1)), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "font-mono text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.expiringData?.generatedAt ?? "N/A"), 1)]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_18, [_cache[23] || (_cache[23] = createBaseVNode("span", { class: "text-sm font-medium" }, "Value", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.expiringData?.value ?? "N/A"), 1)]),
								_: 1
							})]),
							_cache[24] || (_cache[24] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Cached for 5 seconds. After expiry, the next visit re-evaluates the callback. ", -1))
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: ".as('key')" }, {
						description: withCtx(() => [..._cache[25] || (_cache[25] = [createBaseVNode("code", { class: "text-xs" }, "Inertia::once(...)->as('shared-once-key')", -1), createTextVNode(". Custom storage key for cross-page sharing. ", -1)])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_19, [
							createBaseVNode("div", _hoisted_20, [_cache[26] || (_cache[26] = createBaseVNode("span", { class: "text-sm font-medium" }, "Generated At", -1)), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "font-mono text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.aliasedData?.generatedAt ?? "N/A"), 1)]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_21, [_cache[27] || (_cache[27] = createBaseVNode("span", { class: "text-sm font-medium" }, "Value", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.aliasedData?.value ?? "N/A"), 1)]),
								_: 1
							})]),
							_cache[28] || (_cache[28] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Multiple pages can share cached data under the same custom key, even if their prop names differ. ", -1))
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Fluent API"
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_22, [createVNode(CodeBlock_default, { code: "\n                            // Basic once prop\n                            Inertia::once(fn () => ...);\n\n                            // Always re-evaluate\n                            Inertia::once(fn () => ...)->fresh();\n\n                            // Expire after 5s\n                            Inertia::once(fn () => ...)->until(now()->addSeconds(5));\n\n                            // Custom cache key\n                            Inertia::once(fn () => ...)->as('key');\n\n                            // Shared once prop\n                            Inertia::shareOnce('key', value);\n                        " })])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { OnceProps_default as default };

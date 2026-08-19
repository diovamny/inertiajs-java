import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/DataLoading/OptionalProps.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-3" };
var _hoisted_3 = { class: "space-y-3" };
var _hoisted_4 = { class: "flex items-center justify-between" };
var _hoisted_5 = { class: "flex items-center justify-between" };
var _hoisted_6 = { class: "text-xs text-muted-foreground" };
var _hoisted_7 = { class: "space-y-3" };
var _hoisted_8 = { class: "flex items-center justify-between" };
var _hoisted_9 = { class: "flex items-center justify-between" };
var _hoisted_10 = { class: "space-y-1" };
var _hoisted_11 = {
	key: 1,
	class: "text-xs text-muted-foreground"
};
var _hoisted_12 = { class: "space-y-3" };
var _hoisted_13 = { class: "flex items-center justify-between" };
var _hoisted_14 = { class: "flex items-center justify-between" };
var _hoisted_15 = { class: "flex items-center justify-between" };
var _hoisted_16 = {
	key: 1,
	class: "text-xs text-muted-foreground"
};
var _hoisted_17 = { class: "space-y-4" };
var _hoisted_18 = { class: "flex flex-wrap gap-2" };
//#endregion
//#region resources/js/pages/Features/DataLoading/OptionalProps.vue
var OptionalProps_default = /* @__PURE__ */ defineComponent({
	__name: "OptionalProps",
	props: {
		regularData: {},
		optionalData: {},
		deferredData: {}
	},
	setup(__props) {
		const breadcrumbs = [{ title: "Data Loading" }, { title: "Optional Props" }];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Optional Props" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [
					createVNode(FeatureHeader_default, {
						title: "Optional Props",
						docs: "data-props/partial-reloads#lazy-data-evaluation",
						controller: "app/Http/Controllers/Feature/DataLoadingController.php#L162"
					}, {
						default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" Compare regular, optional, and deferred prop loading behaviors. ", -1)])]),
						_: 1
					}),
					createBaseVNode("div", _hoisted_2, [
						createVNode(FeatureCard_default, {
							title: "Regular Prop",
							description: "Always included in the response. Available immediately."
						}, {
							default: withCtx(() => [createBaseVNode("div", _hoisted_3, [
								createBaseVNode("div", _hoisted_4, [_cache[5] || (_cache[5] = createBaseVNode("span", { class: "text-sm font-medium" }, "Status", -1)), createVNode(unref(Badge_default), { variant: "default" }, {
									default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode("Loaded", -1)])]),
									_: 1
								})]),
								createBaseVNode("div", _hoisted_5, [_cache[6] || (_cache[6] = createBaseVNode("span", { class: "text-sm font-medium" }, "Timestamp", -1)), createVNode(unref(Badge_default), {
									variant: "outline",
									class: "font-mono text-xs"
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(__props.regularData.timestamp), 1)]),
									_: 1
								})]),
								createBaseVNode("p", _hoisted_6, toDisplayString(__props.regularData.message), 1)
							])]),
							_: 1
						}),
						createVNode(FeatureCard_default, { title: "Optional Prop" }, {
							description: withCtx(() => [..._cache[7] || (_cache[7] = [createBaseVNode("code", { class: "text-xs" }, "Inertia::optional()", -1), createTextVNode(". Only loaded when explicitly requested via partial reload. ", -1)])]),
							default: withCtx(() => [createBaseVNode("div", _hoisted_7, [createBaseVNode("div", _hoisted_8, [_cache[8] || (_cache[8] = createBaseVNode("span", { class: "text-sm font-medium" }, "Status", -1)), createVNode(unref(Badge_default), { variant: __props.optionalData ? "default" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.optionalData ? "Loaded" : "Not loaded"), 1)]),
								_: 1
							}, 8, ["variant"])]), __props.optionalData ? (openBlock(), createElementBlock(Fragment, { key: 0 }, [createBaseVNode("div", _hoisted_9, [_cache[9] || (_cache[9] = createBaseVNode("span", { class: "text-sm font-medium" }, "Generated At", -1)), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "font-mono text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.optionalData.generatedAt), 1)]),
								_: 1
							})]), createBaseVNode("div", _hoisted_10, [_cache[10] || (_cache[10] = createBaseVNode("span", { class: "text-sm font-medium" }, "Contacts", -1)), (openBlock(true), createElementBlock(Fragment, null, renderList(__props.optionalData.contacts, (contact) => {
								return openBlock(), createElementBlock("div", {
									key: contact.id,
									class: "rounded bg-muted px-2 py-1 text-xs"
								}, toDisplayString(contact.name), 1);
							}), 128))])], 64)) : (openBlock(), createElementBlock("p", _hoisted_11, " Click \"Load Optional Data\" to fetch this prop. "))])]),
							_: 1
						}),
						createVNode(FeatureCard_default, { title: "Deferred Prop" }, {
							description: withCtx(() => [..._cache[11] || (_cache[11] = [createBaseVNode("code", { class: "text-xs" }, "Inertia::defer()", -1), createTextVNode(". Automatically loaded after the initial page render. ", -1)])]),
							default: withCtx(() => [createBaseVNode("div", _hoisted_12, [createBaseVNode("div", _hoisted_13, [_cache[12] || (_cache[12] = createBaseVNode("span", { class: "text-sm font-medium" }, "Status", -1)), createVNode(unref(Badge_default), { variant: __props.deferredData ? "default" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.deferredData ? "Loaded" : "Loading..."), 1)]),
								_: 1
							}, 8, ["variant"])]), __props.deferredData ? (openBlock(), createElementBlock(Fragment, { key: 0 }, [createBaseVNode("div", _hoisted_14, [_cache[13] || (_cache[13] = createBaseVNode("span", { class: "text-sm font-medium" }, "Generated At", -1)), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "font-mono text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.deferredData.generatedAt), 1)]),
								_: 1
							})]), createBaseVNode("div", _hoisted_15, [_cache[14] || (_cache[14] = createBaseVNode("span", { class: "text-sm font-medium" }, "Total Contacts", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.deferredData.totalContacts), 1)]),
								_: 1
							})])], 64)) : (openBlock(), createElementBlock("p", _hoisted_16, " Automatically loads after the initial page render... "))])]),
							_: 1
						})
					]),
					createVNode(FeatureCard_default, {
						title: "Controls",
						description: "Trigger different reload strategies to see how each prop type behaves."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_17, [createBaseVNode("div", _hoisted_18, [
							createVNode(unref(Button_default), { onClick: _cache[0] || (_cache[0] = ($event) => unref(router).reload({ only: ["optionalData"] })) }, {
								default: withCtx(() => [..._cache[15] || (_cache[15] = [createTextVNode(" Load Optional Data ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								onClick: _cache[1] || (_cache[1] = ($event) => unref(router).reload({ only: ["regularData"] }))
							}, {
								default: withCtx(() => [..._cache[16] || (_cache[16] = [createTextVNode(" Reload Regular Only ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								onClick: _cache[2] || (_cache[2] = ($event) => unref(router).reload())
							}, {
								default: withCtx(() => [..._cache[17] || (_cache[17] = [createTextVNode(" Reload All ", -1)])]),
								_: 1
							})
						]), _cache[18] || (_cache[18] = createBaseVNode("div", { class: "rounded-lg border border-black/5 bg-neutral-50/80 p-3 font-mono text-xs dark:border-white/5 dark:bg-neutral-900/80" }, [createBaseVNode("p", null, [createBaseVNode("strong", null, "Key Differences:")]), createBaseVNode("ul", { class: "mt-1 list-inside list-disc space-y-1" }, [
							createBaseVNode("li", null, [createBaseVNode("strong", null, "Regular"), createTextVNode(": always resolved on full visits. Excluded from partial reloads unless explicitly requested. ")]),
							createBaseVNode("li", null, [
								createBaseVNode("strong", null, "Optional"),
								createTextVNode(": never resolved unless explicitly requested with "),
								createBaseVNode("code", null, "only: ['optionalData']"),
								createTextVNode(". ")
							]),
							createBaseVNode("li", null, [createBaseVNode("strong", null, "Deferred"), createTextVNode(": not included on initial load, but automatically fetched client-side after render. ")])
						])], -1))])]),
						_: 1
					})
				])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { OptionalProps_default as default };

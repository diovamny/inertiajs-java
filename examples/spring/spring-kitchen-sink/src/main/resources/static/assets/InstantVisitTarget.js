import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Navigation/InstantVisitTarget.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-3" };
var _hoisted_4 = { class: "flex items-center justify-between" };
var _hoisted_5 = { class: "flex items-center justify-between" };
var _hoisted_6 = {
	key: 0,
	class: "space-y-2"
};
var _hoisted_7 = { class: "text-sm" };
var _hoisted_8 = {
	key: 1,
	class: "space-y-2"
};
//#endregion
//#region resources/js/pages/Features/Navigation/InstantVisitTarget.vue
var InstantVisitTarget_default = /* @__PURE__ */ defineComponent({
	__name: "InstantVisitTarget",
	props: {
		greeting: {},
		serverTimestamp: {},
		items: {}
	},
	setup(__props) {
		const breadcrumbs = [
			{ title: "Navigation" },
			{ title: "Instant Visits" },
			{ title: "Target Page" }
		];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Instant Visit Target" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Instant Visit Target",
					docs: "the-basics/manual-visits#client-side-visits",
					controller: "app/Http/Controllers/Feature/NavigationController.php#L87"
				}, {
					default: withCtx(() => [..._cache[1] || (_cache[1] = [createTextVNode(" This page has an artificial server delay. With instant visits, the component renders immediately with placeholder props. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Server Data",
						description: "Props received from the server response."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [_cache[2] || (_cache[2] = createBaseVNode("span", { class: "text-sm font-medium" }, "Greeting", -1)), createVNode(unref(Badge_default), { variant: "outline" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.greeting ?? "Loading..."), 1)]),
							_: 1
						})]), createBaseVNode("div", _hoisted_5, [_cache[3] || (_cache[3] = createBaseVNode("span", { class: "text-sm font-medium" }, "Timestamp", -1)), createVNode(unref(Badge_default), {
							variant: "outline",
							class: "font-mono text-xs"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.serverTimestamp ?? "Waiting for server..."), 1)]),
							_: 1
						})])])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Items List",
						description: "Items loaded from the server."
					}, {
						default: withCtx(() => [__props.items?.length ? (openBlock(), createElementBlock("div", _hoisted_6, [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.items, (item) => {
							return openBlock(), createElementBlock("div", {
								key: item.id,
								class: "flex items-center justify-between rounded-lg bg-muted/30 px-3 py-2"
							}, [createBaseVNode("span", _hoisted_7, toDisplayString(item.name), 1), createVNode(unref(Badge_default), {
								variant: "secondary",
								class: "text-xs"
							}, {
								default: withCtx(() => [createTextVNode("ID: " + toDisplayString(item.id), 1)]),
								_: 2
							}, 1024)]);
						}), 128))])) : (openBlock(), createElementBlock("div", _hoisted_8, [(openBlock(), createElementBlock(Fragment, null, renderList(3, (i) => {
							return createBaseVNode("div", {
								key: i,
								class: "h-10 animate-pulse rounded bg-muted"
							});
						}), 64)), _cache[4] || (_cache[4] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Waiting for server data... ", -1))]))]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						class: "lg:col-span-2",
						title: "Back to Source"
					}, {
						default: withCtx(() => [createVNode(unref(Button_default), { onClick: _cache[0] || (_cache[0] = ($event) => unref(router).visit("/features/navigation/instant-visits")) }, {
							default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" Back to Instant Visits ", -1)])]),
							_: 1
						})]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { InstantVisitTarget_default as default };

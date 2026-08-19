import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Navigation/HistoryManagement.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "flex flex-wrap items-center gap-4" };
var _hoisted_4 = { class: "flex items-center gap-2" };
var _hoisted_5 = { class: "flex items-center gap-2" };
var _hoisted_6 = { class: "flex items-center gap-2" };
var _hoisted_7 = { class: "space-y-4" };
var _hoisted_8 = { class: "space-y-2" };
var _hoisted_9 = { class: "space-y-2" };
var _hoisted_10 = { class: "space-y-4" };
var _hoisted_11 = { class: "space-y-2" };
var _hoisted_12 = { class: "space-y-2" };
//#endregion
//#region resources/js/pages/Features/Navigation/HistoryManagement.vue
var HistoryManagement_default = /* @__PURE__ */ defineComponent({
	__name: "HistoryManagement",
	props: {
		visit: {},
		timestamp: {}
	},
	setup(__props) {
		const props = __props;
		const breadcrumbs = [{ title: "Navigation" }, { title: "History Management" }];
		function visitPush() {
			const next = props.visit + 1;
			router.get(`/features/navigation/history-management?visit=${next}`);
		}
		function visitReplace() {
			const next = props.visit + 1;
			router.get(`/features/navigation/history-management?visit=${next}`, {}, { replace: true });
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "History Management" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "History Management",
					docs: "the-basics/manual-visits#browser-history",
					controller: "app/Http/Controllers/Feature/NavigationController.php#L45"
				}, {
					default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode(" Control how visits affect the browser history stack. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "Current State"
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [
							createBaseVNode("div", _hoisted_4, [_cache[5] || (_cache[5] = createBaseVNode("span", { class: "text-sm text-muted-foreground" }, "Visit #", -1)), createVNode(unref(Badge_default), {
								variant: "secondary",
								class: "min-w-8 justify-center text-lg tabular-nums"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.visit), 1)]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_5, [_cache[6] || (_cache[6] = createBaseVNode("span", { class: "text-sm text-muted-foreground" }, "URL:", -1)), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "font-mono text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(_ctx.$page.url), 1)]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_6, [_cache[7] || (_cache[7] = createBaseVNode("span", { class: "text-sm text-muted-foreground" }, "Timestamp:", -1)), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.timestamp), 1)]),
								_: 1
							})])
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "replace: true vs push",
						description: "Push adds a new history entry. Replace overwrites the current one. Click each a few times, then use the browser Back button to see the difference."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_7, [createBaseVNode("div", _hoisted_8, [createVNode(unref(Button_default), {
							variant: "outline",
							class: "w-full",
							onClick: _cache[0] || (_cache[0] = ($event) => visitPush())
						}, {
							default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode(" Push (default) ", -1)])]),
							_: 1
						}), _cache[9] || (_cache[9] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Each click adds a history entry. Back returns to the previous visit. ", -1))]), createBaseVNode("div", _hoisted_9, [createVNode(unref(Button_default), {
							class: "w-full",
							onClick: _cache[1] || (_cache[1] = ($event) => visitReplace())
						}, {
							default: withCtx(() => [..._cache[10] || (_cache[10] = [createTextVNode(" Replace ", -1)])]),
							_: 1
						}), _cache[11] || (_cache[11] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Each click overwrites the current entry. Back skips replaced visits entirely. ", -1))])])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "preserveUrl: true",
						description: "Makes a server request but keeps the browser URL unchanged. First use the Push button a few times to increase the visit number, then try these."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_10, [createBaseVNode("div", _hoisted_11, [createVNode(unref(Button_default), {
							class: "w-full",
							onClick: _cache[2] || (_cache[2] = ($event) => unref(router).post("/features/navigation/history-management", {}, {
								preserveUrl: true,
								preserveScroll: true
							}))
						}, {
							default: withCtx(() => [..._cache[12] || (_cache[12] = [createTextVNode(" POST (preserveUrl) ", -1)])]),
							_: 1
						}), _cache[13] || (_cache[13] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " The timestamp updates but the URL stays the same. ", -1))]), createBaseVNode("div", _hoisted_12, [createVNode(unref(Button_default), {
							variant: "outline",
							class: "w-full",
							onClick: _cache[3] || (_cache[3] = ($event) => unref(router).post("/features/navigation/history-management", {}, { preserveScroll: true }))
						}, {
							default: withCtx(() => [..._cache[14] || (_cache[14] = [createTextVNode(" POST (default) ", -1)])]),
							_: 1
						}), _cache[15] || (_cache[15] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " For comparison. The URL may update. ", -1))])])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { HistoryManagement_default as default };

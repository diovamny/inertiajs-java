import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Navigation/ManualVisits.vue?vue&type=script&setup=true&lang.ts
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
//#region resources/js/pages/Features/Navigation/ManualVisits.vue
var ManualVisits_default = /* @__PURE__ */ defineComponent({
	__name: "ManualVisits",
	props: {
		timestamp: {},
		counter: {}
	},
	setup(__props) {
		const breadcrumbs = [{ title: "Navigation" }, { title: "Client-Side Visits" }];
		let pushCount = 0;
		function pushUrl() {
			pushCount++;
			router.push({ url: `/features/navigation/manual-visits?pushed=${pushCount}` });
		}
		function replaceUrl() {
			router.replace({ url: `/features/navigation/manual-visits?replaced=${Date.now()}` });
		}
		function pushWithProps() {
			router.push({ props: (currentProps) => ({
				...currentProps,
				counter: currentProps.counter + 1
			}) });
		}
		function pushWithPropCallback() {
			router.push({
				url: `/features/navigation/manual-visits?updated=${Date.now()}`,
				props: (currentProps) => ({
					...currentProps,
					counter: currentProps.counter * 10,
					timestamp: "Client-side override at " + (/* @__PURE__ */ new Date()).toLocaleTimeString()
				})
			});
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Client-Side Visits" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Client-Side Visits",
					docs: "the-basics/manual-visits",
					controller: "app/Http/Controllers/Feature/NavigationController.php#L103"
				}, {
					default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode(" Client-side navigation using router.push() and router.replace(). Update the URL and props without a server request. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "Current State"
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [
							createBaseVNode("div", _hoisted_4, [_cache[5] || (_cache[5] = createBaseVNode("span", { class: "text-sm text-muted-foreground" }, "URL:", -1)), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "font-mono text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(_ctx.$page.url), 1)]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_5, [_cache[6] || (_cache[6] = createBaseVNode("span", { class: "text-sm text-muted-foreground" }, "Timestamp:", -1)), createVNode(unref(Badge_default), { variant: "outline" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.timestamp), 1)]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_6, [_cache[7] || (_cache[7] = createBaseVNode("span", { class: "text-sm text-muted-foreground" }, "Counter:", -1)), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "min-w-8 justify-center tabular-nums"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(__props.counter), 1)]),
								_: 1
							})])
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "URL Replacement",
						description: "Change the browser URL without making a server request."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_7, [createBaseVNode("div", _hoisted_8, [
							_cache[9] || (_cache[9] = createBaseVNode("h4", { class: "text-sm font-semibold" }, "router.push()", -1)),
							createVNode(unref(Button_default), {
								variant: "outline",
								class: "w-full",
								onClick: _cache[0] || (_cache[0] = ($event) => pushUrl())
							}, {
								default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode(" Push new URL ", -1)])]),
								_: 1
							}),
							_cache[10] || (_cache[10] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Adds a new history entry. Press Back to return to the previous URL. ", -1))
						]), createBaseVNode("div", _hoisted_9, [
							_cache[12] || (_cache[12] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " router.replace() ", -1)),
							createVNode(unref(Button_default), {
								variant: "outline",
								class: "w-full",
								onClick: _cache[1] || (_cache[1] = ($event) => replaceUrl())
							}, {
								default: withCtx(() => [..._cache[11] || (_cache[11] = [createTextVNode(" Replace current URL ", -1)])]),
								_: 1
							}),
							_cache[13] || (_cache[13] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Overwrites the current history entry. Back skips this URL. ", -1))
						])])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "Prop Replacement",
						description: "Update page props client-side. Use the callback form to spread existing props and override specific ones."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_10, [createBaseVNode("div", _hoisted_11, [
							_cache[15] || (_cache[15] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " Increment a single prop ", -1)),
							createVNode(unref(Button_default), {
								variant: "outline",
								class: "w-full",
								onClick: _cache[2] || (_cache[2] = ($event) => pushWithProps())
							}, {
								default: withCtx(() => [..._cache[14] || (_cache[14] = [createTextVNode(" Increment counter ", -1)])]),
								_: 1
							}),
							_cache[16] || (_cache[16] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Spreads current props and overrides counter. Other props stay unchanged. ", -1))
						]), createBaseVNode("div", _hoisted_12, [
							_cache[18] || (_cache[18] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " Props callback ", -1)),
							createVNode(unref(Button_default), {
								variant: "outline",
								class: "w-full",
								onClick: _cache[3] || (_cache[3] = ($event) => pushWithPropCallback())
							}, {
								default: withCtx(() => [..._cache[17] || (_cache[17] = [createTextVNode(" Transform props + URL ", -1)])]),
								_: 1
							}),
							_cache[19] || (_cache[19] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Receives current props as an argument. Multiplies the counter by 10 and overrides the timestamp. ", -1))
						])])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { ManualVisits_default as default };

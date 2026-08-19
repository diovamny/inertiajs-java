import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, rr as ref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Navigation/Links.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "flex items-center gap-2" };
var _hoisted_4 = { class: "space-y-4" };
var _hoisted_5 = { class: "space-y-2" };
var _hoisted_6 = { class: "space-y-2" };
var _hoisted_7 = { class: "space-y-2" };
var _hoisted_8 = { class: "space-y-2" };
var _hoisted_9 = { class: "space-y-2" };
var _hoisted_10 = { class: "space-y-2" };
var _hoisted_11 = { class: "space-y-4" };
var _hoisted_12 = { class: "flex flex-wrap gap-2" };
var _hoisted_13 = { class: "space-y-2" };
var _hoisted_14 = { class: "mt-4 rounded-md border border-black/10 p-3 dark:border-white/10" };
var _hoisted_15 = { class: "mb-2 flex items-center justify-between" };
var _hoisted_16 = {
	key: 0,
	class: "space-y-1"
};
var _hoisted_17 = {
	key: 1,
	class: "text-xs text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Features/Navigation/Links.vue
var Links_default = /* @__PURE__ */ defineComponent({
	__name: "Links",
	props: { timestamp: {} },
	setup(__props) {
		const breadcrumbs = [{ title: "Navigation" }, { title: "Links & Methods" }];
		const eventLog = ref([]);
		function log(message) {
			eventLog.value.unshift(`${(/* @__PURE__ */ new Date()).toLocaleTimeString()} - ${message}`);
			if (eventLog.value.length > 10) eventLog.value.pop();
		}
		function manualVisit(method) {
			const options = {
				preserveScroll: true,
				onSuccess: () => log(`router.${method}() completed`)
			};
			({
				get: () => router.get("/features/navigation/links", { demo: true }, options),
				post: () => router.post("/features/navigation/links", { demo: true }, options),
				put: () => router.put("/features/navigation/links", { demo: true }, options),
				patch: () => router.patch("/features/navigation/links", { demo: true }, options),
				delete: () => router.delete("/features/navigation/links", options)
			})[method]();
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Links & Methods" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Links & Methods",
					docs: "the-basics/links",
					controller: "app/Http/Controllers/Feature/NavigationController.php#L13"
				}, {
					default: withCtx(() => [..._cache[7] || (_cache[7] = [createTextVNode(" The <Link> component and programmatic router visits. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "Server Response"
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [_cache[8] || (_cache[8] = createBaseVNode("span", { class: "text-sm text-muted-foreground" }, "Server timestamp:", -1)), createVNode(unref(Badge_default), { variant: "outline" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.timestamp), 1)]),
							_: 1
						})])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "<Link> Component",
						description: "Client-side navigation that avoids full page reloads."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_4, [
							createBaseVNode("div", _hoisted_5, [_cache[10] || (_cache[10] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " GET Link (default) ", -1)), createVNode(unref(link_default), {
								href: "/features/navigation/links",
								class: "text-sm text-primary underline underline-offset-2"
							}, {
								default: withCtx(() => [..._cache[9] || (_cache[9] = [createTextVNode(" Reload this page via <Link> ", -1)])]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_6, [_cache[12] || (_cache[12] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " POST as button ", -1)), createVNode(unref(link_default), {
								href: "/features/navigation/links",
								method: "post",
								as: "button",
								"preserve-scroll": "",
								class: "inline-flex items-center rounded-md bg-primary px-3 py-1.5 text-sm text-primary-foreground hover:bg-primary/90"
							}, {
								default: withCtx(() => [..._cache[11] || (_cache[11] = [createTextVNode(" POST Request ", -1)])]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_7, [_cache[14] || (_cache[14] = createBaseVNode("h4", { class: "text-sm font-semibold" }, "PUT as button", -1)), createVNode(unref(link_default), {
								href: "/features/navigation/links",
								method: "put",
								as: "button",
								"preserve-scroll": "",
								class: "inline-flex items-center rounded-md bg-primary px-3 py-1.5 text-sm text-primary-foreground hover:bg-primary/90"
							}, {
								default: withCtx(() => [..._cache[13] || (_cache[13] = [createTextVNode(" PUT Request ", -1)])]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_8, [_cache[16] || (_cache[16] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " DELETE as button ", -1)), createVNode(unref(link_default), {
								href: "/features/navigation/links",
								method: "delete",
								as: "button",
								"preserve-scroll": "",
								class: "inline-flex items-center rounded-md bg-destructive px-3 py-1.5 text-sm text-destructive-foreground hover:bg-destructive/90"
							}, {
								default: withCtx(() => [..._cache[15] || (_cache[15] = [createTextVNode(" DELETE Request ", -1)])]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_9, [_cache[18] || (_cache[18] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " Link with data ", -1)), createVNode(unref(link_default), {
								href: "/features/navigation/links",
								method: "post",
								as: "button",
								data: { name: "Inertia" },
								"preserve-scroll": "",
								class: "inline-flex items-center rounded-md border border-black/10 bg-background px-3 py-1.5 text-sm hover:bg-accent dark:border-white/10"
							}, {
								default: withCtx(() => [..._cache[17] || (_cache[17] = [createTextVNode(" POST with payload ", -1)])]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_10, [_cache[20] || (_cache[20] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " Link with custom headers ", -1)), createVNode(unref(link_default), {
								href: "/features/navigation/links",
								headers: { "X-Custom-Header": "demo" },
								class: "text-sm text-primary underline underline-offset-2"
							}, {
								default: withCtx(() => [..._cache[19] || (_cache[19] = [createTextVNode(" GET with custom header ", -1)])]),
								_: 1
							})])
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "Programmatic Visits",
						description: "Navigate using router.get(), router.post(), etc."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_11, [
							createBaseVNode("div", _hoisted_12, [
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[0] || (_cache[0] = ($event) => manualVisit("get"))
								}, {
									default: withCtx(() => [..._cache[21] || (_cache[21] = [createTextVNode("router.get()", -1)])]),
									_: 1
								}),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[1] || (_cache[1] = ($event) => manualVisit("post"))
								}, {
									default: withCtx(() => [..._cache[22] || (_cache[22] = [createTextVNode("router.post()", -1)])]),
									_: 1
								}),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[2] || (_cache[2] = ($event) => manualVisit("put"))
								}, {
									default: withCtx(() => [..._cache[23] || (_cache[23] = [createTextVNode("router.put()", -1)])]),
									_: 1
								}),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[3] || (_cache[3] = ($event) => manualVisit("patch"))
								}, {
									default: withCtx(() => [..._cache[24] || (_cache[24] = [createTextVNode("router.patch()", -1)])]),
									_: 1
								}),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[4] || (_cache[4] = ($event) => manualVisit("delete"))
								}, {
									default: withCtx(() => [..._cache[25] || (_cache[25] = [createTextVNode("router.delete()", -1)])]),
									_: 1
								})
							]),
							createBaseVNode("div", _hoisted_13, [_cache[27] || (_cache[27] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " router.reload() ", -1)), createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[5] || (_cache[5] = ($event) => unref(router).reload({ onSuccess: () => log("router.reload() completed") }))
							}, {
								default: withCtx(() => [..._cache[26] || (_cache[26] = [createTextVNode(" Reload current page ", -1)])]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_14, [createBaseVNode("div", _hoisted_15, [_cache[29] || (_cache[29] = createBaseVNode("h4", { class: "text-sm font-semibold" }, "Event Log", -1)), createVNode(unref(Button_default), {
								variant: "ghost",
								size: "sm",
								onClick: _cache[6] || (_cache[6] = ($event) => eventLog.value = [])
							}, {
								default: withCtx(() => [..._cache[28] || (_cache[28] = [createTextVNode("Clear", -1)])]),
								_: 1
							})]), eventLog.value.length ? (openBlock(), createElementBlock("div", _hoisted_16, [(openBlock(true), createElementBlock(Fragment, null, renderList(eventLog.value, (entry, i) => {
								return openBlock(), createElementBlock("div", {
									key: i,
									class: "rounded bg-muted px-2 py-1 font-mono text-xs"
								}, toDisplayString(entry), 1);
							}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_17, " Click a button to see events. "))])
						])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { Links_default as default };

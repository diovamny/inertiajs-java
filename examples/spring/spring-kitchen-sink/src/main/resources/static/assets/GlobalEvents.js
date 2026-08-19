import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, Zt as onUnmounted, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, qt as onMounted, rr as ref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Events/GlobalEvents.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "flex flex-wrap gap-2" };
var _hoisted_5 = { class: "grid grid-cols-2 gap-2" };
var _hoisted_6 = {
	key: 0,
	class: "max-h-64 space-y-1 overflow-y-auto"
};
var _hoisted_7 = {
	key: 1,
	class: "text-xs text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Features/Events/GlobalEvents.vue
var GlobalEvents_default = /* @__PURE__ */ defineComponent({
	__name: "GlobalEvents",
	setup(__props) {
		const breadcrumbs = [{ title: "Events & Lifecycle" }, { title: "Global Events" }];
		const eventLog = ref([]);
		const removers = [];
		function log(event, detail) {
			eventLog.value.unshift(`${(/* @__PURE__ */ new Date()).toLocaleTimeString()} - ${event}${detail ? ": " + detail : ""}`);
			if (eventLog.value.length > 20) eventLog.value.pop();
		}
		onMounted(() => {
			removers.push(router.on("before", (event) => {
				log("before", event.detail.visit.url.toString());
			}), router.on("start", (event) => {
				log("start", event.detail.visit.url.toString());
			}), router.on("progress", (event) => {
				log("progress", `${event.detail.progress?.percentage}%`);
			}), router.on("success", (event) => {
				log("success", event.detail.page.url);
			}), router.on("error", (event) => {
				log("error", JSON.stringify(event.detail.errors));
			}), router.on("finish", () => {
				log("finish");
			}), router.on("navigate", (event) => {
				log("navigate", event.detail.page.url);
			}), router.on("flash", (event) => {
				log("flash", JSON.stringify(event.detail.flash));
			}));
		});
		onUnmounted(() => {
			removers.forEach((remove) => remove());
		});
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Global Events" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Global Events",
					docs: "advanced/events#registering-listeners",
					controller: "app/Http/Controllers/Feature/EventController.php#L12"
				}, {
					default: withCtx(() => [..._cache[3] || (_cache[3] = [
						createTextVNode(" Router event system with ", -1),
						createBaseVNode("code", { class: "text-xs" }, "router.on()", -1),
						createTextVNode(". Fires on every Inertia request. ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						title: "Trigger Events",
						description: "Perform actions and watch the event log update in real time."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [
							createBaseVNode("div", _hoisted_4, [
								createVNode(unref(Button_default), {
									size: "sm",
									onClick: _cache[0] || (_cache[0] = ($event) => unref(router).reload())
								}, {
									default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode(" Reload Page ", -1)])]),
									_: 1
								}),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[1] || (_cache[1] = ($event) => unref(router).post("/features/events/global-events/action", {}, { preserveScroll: true }))
								}, {
									default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" POST Action ", -1)])]),
									_: 1
								}),
								createVNode(unref(link_default), {
									href: "/features/events/visit-callbacks",
									class: "inline-flex items-center rounded-md border border-black/10 bg-background px-3 py-1.5 text-sm hover:bg-accent dark:border-white/10"
								}, {
									default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" Navigate Away ", -1)])]),
									_: 1
								})
							]),
							_cache[7] || (_cache[7] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Each action triggers a sequence of events: before → start → success/error → finish → navigate. The progress event only fires during file uploads. ", -1)),
							createVNode(CodeBlock_default, {
								title: "Cleanup:",
								code: "\n                                const remove = router.on('before', (e) => {\n                                  console.log(e.detail.visit.url)\n                                })\n\n                                // Later: deregister the listener\n                                remove()\n                            "
							})
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Available Events",
						description: "All events fired by the Inertia router."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_5, [
							createVNode(unref(Badge_default), {
								variant: "outline",
								class: "justify-center"
							}, {
								default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode("before", -1)])]),
								_: 1
							}),
							createVNode(unref(Badge_default), {
								variant: "outline",
								class: "justify-center"
							}, {
								default: withCtx(() => [..._cache[9] || (_cache[9] = [createTextVNode("start", -1)])]),
								_: 1
							}),
							createVNode(unref(Badge_default), {
								variant: "outline",
								class: "justify-center"
							}, {
								default: withCtx(() => [..._cache[10] || (_cache[10] = [createTextVNode("progress", -1)])]),
								_: 1
							}),
							createVNode(unref(Badge_default), {
								variant: "outline",
								class: "justify-center"
							}, {
								default: withCtx(() => [..._cache[11] || (_cache[11] = [createTextVNode("success", -1)])]),
								_: 1
							}),
							createVNode(unref(Badge_default), {
								variant: "outline",
								class: "justify-center"
							}, {
								default: withCtx(() => [..._cache[12] || (_cache[12] = [createTextVNode("error", -1)])]),
								_: 1
							}),
							createVNode(unref(Badge_default), {
								variant: "outline",
								class: "justify-center"
							}, {
								default: withCtx(() => [..._cache[13] || (_cache[13] = [createTextVNode("finish", -1)])]),
								_: 1
							}),
							createVNode(unref(Badge_default), {
								variant: "outline",
								class: "justify-center"
							}, {
								default: withCtx(() => [..._cache[14] || (_cache[14] = [createTextVNode("navigate", -1)])]),
								_: 1
							}),
							createVNode(unref(Badge_default), {
								variant: "outline",
								class: "justify-center"
							}, {
								default: withCtx(() => [..._cache[15] || (_cache[15] = [createTextVNode("flash", -1)])]),
								_: 1
							}),
							createVNode(unref(Badge_default), {
								variant: "outline",
								class: "justify-center"
							}, {
								default: withCtx(() => [..._cache[16] || (_cache[16] = [createTextVNode("httpException", -1)])]),
								_: 1
							}),
							createVNode(unref(Badge_default), {
								variant: "outline",
								class: "justify-center"
							}, {
								default: withCtx(() => [..._cache[17] || (_cache[17] = [createTextVNode("networkError", -1)])]),
								_: 1
							}),
							createVNode(unref(Badge_default), {
								variant: "outline",
								class: "justify-center"
							}, {
								default: withCtx(() => [..._cache[18] || (_cache[18] = [createTextVNode("prefetching", -1)])]),
								_: 1
							}),
							createVNode(unref(Badge_default), {
								variant: "outline",
								class: "justify-center"
							}, {
								default: withCtx(() => [..._cache[19] || (_cache[19] = [createTextVNode("prefetched", -1)])]),
								_: 1
							})
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "Event Log"
					}, {
						description: withCtx(() => [..._cache[20] || (_cache[20] = [
							createTextVNode("Events captured by ", -1),
							createBaseVNode("code", { class: "text-xs" }, "router.on()", -1),
							createTextVNode(" listeners registered on this page.", -1)
						])]),
						"header-action": withCtx(() => [createVNode(unref(Button_default), {
							variant: "ghost",
							size: "sm",
							onClick: _cache[2] || (_cache[2] = ($event) => eventLog.value = [])
						}, {
							default: withCtx(() => [..._cache[21] || (_cache[21] = [createTextVNode("Clear", -1)])]),
							_: 1
						})]),
						default: withCtx(() => [eventLog.value.length ? (openBlock(), createElementBlock("div", _hoisted_6, [(openBlock(true), createElementBlock(Fragment, null, renderList(eventLog.value, (entry, i) => {
							return openBlock(), createElementBlock("div", {
								key: i,
								class: "rounded bg-muted px-2 py-1 font-mono text-xs"
							}, toDisplayString(entry), 1);
						}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_7, " Trigger an action to see events. "))]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { GlobalEvents_default as default };

import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, Zt as onUnmounted, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, rr as ref, rt as createBlock, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Events/OnceEvents.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "flex flex-wrap gap-2" };
var _hoisted_5 = { class: "flex flex-wrap gap-2" };
var _hoisted_6 = { class: "space-y-4" };
var _hoisted_7 = { class: "flex flex-wrap gap-2" };
var _hoisted_8 = {
	key: 0,
	class: "max-h-64 space-y-1 overflow-y-auto"
};
//#endregion
//#region resources/js/pages/Features/Events/OnceEvents.vue
var OnceEvents_default = /* @__PURE__ */ defineComponent({
	__name: "OnceEvents",
	setup(__props) {
		const breadcrumbs = [{ title: "Events & Lifecycle" }, { title: "Inertia.once" }];
		const eventLog = ref([]);
		const pendingRemovers = [];
		function log(message) {
			eventLog.value.unshift(`${(/* @__PURE__ */ new Date()).toLocaleTimeString()} - ${message}`);
			if (eventLog.value.length > 20) eventLog.value.pop();
		}
		function registerOnceBefore() {
			log("Registered router.once(\"before\") confirm listener");
			router.once("before", (event) => {
				const url = event.detail.visit.url.toString();
				log(`once(before) fired for ${url}`);
				return confirm(`Allow visit to ${url}?`);
			});
		}
		function registerOnceSuccess() {
			log("Registered router.once(\"success\") listener");
			router.once("success", () => {
				log("once(success) fired");
			});
		}
		function compareWithOn() {
			log("Registered router.on(\"before\") confirm listener");
			const remove = router.on("before", (event) => {
				const url = event.detail.visit.url.toString();
				log(`on(before) fired for ${url}`);
				return confirm(`Allow visit to ${url}?`);
			});
			pendingRemovers.push(remove);
		}
		function clearOnListeners() {
			pendingRemovers.forEach((r) => r());
			pendingRemovers.length = 0;
			log("Cleared all router.on listeners");
		}
		function fireAction() {
			router.post("/features/events/once-events/action", {}, { preserveScroll: true });
		}
		onUnmounted(() => {
			pendingRemovers.forEach((r) => r());
		});
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Inertia.once" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Inertia.once",
					docs: "advanced/events#registering-listeners",
					controller: "app/Http/Controllers/Feature/EventController.php#L22"
				}, {
					default: withCtx(() => [..._cache[2] || (_cache[2] = [
						createTextVNode(" Listen to a router event exactly once with ", -1),
						createBaseVNode("code", { class: "text-xs" }, "router.once()", -1),
						createTextVNode(". Listener auto-removes after the first invocation. ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						title: "Register once listeners",
						badge: "v3.2",
						description: "Register a listener, then trigger a visit. once(before) returns confirm() to gate the visit, then auto-removes."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [
							createBaseVNode("div", _hoisted_4, [createVNode(unref(Button_default), {
								size: "sm",
								"data-test": "register-once-before",
								onClick: registerOnceBefore
							}, {
								default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" once('before') ", -1)])]),
								_: 1
							}), createVNode(unref(Button_default), {
								size: "sm",
								"data-test": "register-once-success",
								onClick: registerOnceSuccess
							}, {
								default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode(" once('success') ", -1)])]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_5, [createVNode(unref(Button_default), {
								size: "sm",
								variant: "secondary",
								onClick: _cache[0] || (_cache[0] = ($event) => unref(router).reload())
							}, {
								default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" Reload page ", -1)])]),
								_: 1
							}), createVNode(unref(Button_default), {
								size: "sm",
								variant: "secondary",
								onClick: fireAction
							}, {
								default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" POST action ", -1)])]),
								_: 1
							})]),
							createVNode(CodeBlock_default, {
								title: "Inertia.once",
								code: "\n                                router.once('before', (event) => {\n                                  return confirm('Allow visit to ' + event.detail.visit.url + '?')\n                                })\n\n                                // Equivalent to the self-removing pattern:\n                                const remove = router.on('before', (event) => {\n                                  remove()\n                                  return confirm('...')\n                                })\n                            "
							})
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "Compare with router.on",
						description: "A plain router.on listener keeps firing until removed. Register it, then trigger multiple visits to contrast with once()."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_6, [createBaseVNode("div", _hoisted_7, [createVNode(unref(Button_default), {
							size: "sm",
							onClick: compareWithOn
						}, {
							default: withCtx(() => [..._cache[7] || (_cache[7] = [createTextVNode(" Register on('before') ", -1)])]),
							_: 1
						}), createVNode(unref(Button_default), {
							size: "sm",
							variant: "destructive",
							onClick: clearOnListeners
						}, {
							default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode(" Clear on listeners ", -1)])]),
							_: 1
						})]), _cache[9] || (_cache[9] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
							createTextVNode(" Fire several reloads after registering. "),
							createBaseVNode("code", null, "on(before)"),
							createTextVNode(" prompts every visit until cleared. "),
							createBaseVNode("code", null, "once(before)"),
							createTextVNode(" prompts the next visit, then auto-removes. ")
						], -1))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "Event Log"
					}, {
						"header-action": withCtx(() => [createVNode(unref(Button_default), {
							variant: "ghost",
							size: "sm",
							onClick: _cache[1] || (_cache[1] = ($event) => eventLog.value = [])
						}, {
							default: withCtx(() => [..._cache[10] || (_cache[10] = [createTextVNode(" Clear ", -1)])]),
							_: 1
						})]),
						default: withCtx(() => [eventLog.value.length ? (openBlock(), createElementBlock("div", _hoisted_8, [(openBlock(true), createElementBlock(Fragment, null, renderList(eventLog.value, (entry, i) => {
							return openBlock(), createElementBlock("div", {
								key: i,
								class: "rounded bg-muted px-2 py-1 font-mono text-xs"
							}, toDisplayString(entry), 1);
						}), 128))])) : (openBlock(), createBlock(unref(Badge_default), {
							key: 1,
							variant: "outline"
						}, {
							default: withCtx(() => [..._cache[11] || (_cache[11] = [createTextVNode("No events yet", -1)])]),
							_: 1
						}))]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { OnceEvents_default as default };

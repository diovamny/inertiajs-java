import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, Zt as onUnmounted, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, pr as unref, qt as onMounted, rr as ref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Events/LocationEvent.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = {
	key: 0,
	class: "flex flex-wrap items-center justify-between gap-3 rounded-lg border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-900 dark:border-amber-500/30 dark:bg-amber-500/10 dark:text-amber-200"
};
var _hoisted_3 = { class: "flex gap-2" };
var _hoisted_4 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_5 = { class: "space-y-4" };
var _hoisted_6 = {
	key: 0,
	class: "max-h-64 space-y-1 overflow-y-auto"
};
var _hoisted_7 = {
	key: 1,
	class: "text-xs text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Features/Events/LocationEvent.vue
var LocationEvent_default = /* @__PURE__ */ defineComponent({
	__name: "LocationEvent",
	setup(__props) {
		const breadcrumbs = [{ title: "Events & Lifecycle" }, { title: "Location Event" }];
		const updateAvailable = ref(false);
		const eventLog = ref([]);
		let remove = null;
		function log(message) {
			eventLog.value.unshift(`${(/* @__PURE__ */ new Date()).toLocaleTimeString()} - ${message}`);
			if (eventLog.value.length > 20) eventLog.value.pop();
		}
		onMounted(() => {
			remove = router.on("location", (event) => {
				log(`location event (versionChange: ${event.detail.versionChange})`);
				if (event.detail.versionChange) {
					event.preventDefault();
					updateAvailable.value = true;
				}
			});
		});
		onUnmounted(() => remove?.());
		function deploy() {
			router.get("/features/events/location-event/deploy");
		}
		function reload() {
			window.location.reload();
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Location Event" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [
					updateAvailable.value ? (openBlock(), createElementBlock("div", _hoisted_2, [_cache[4] || (_cache[4] = createBaseVNode("span", null, " A new version of this app is available. Reload to get the latest assets. ", -1)), createBaseVNode("div", _hoisted_3, [createVNode(unref(Button_default), {
						size: "sm",
						onClick: reload
					}, {
						default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode("Reload now", -1)])]),
						_: 1
					}), createVNode(unref(Button_default), {
						variant: "ghost",
						size: "sm",
						onClick: _cache[0] || (_cache[0] = ($event) => updateAvailable.value = false)
					}, {
						default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" Dismiss ", -1)])]),
						_: 1
					})])])) : createCommentVNode("", true),
					createVNode(FeatureHeader_default, {
						title: "Location Event",
						docs: "advanced/events#the-location-event",
						controller: "app/Http/Controllers/Feature/EventController.php#L54"
					}, {
						default: withCtx(() => [..._cache[5] || (_cache[5] = [
							createTextVNode(" The cancelable ", -1),
							createBaseVNode("code", { class: "text-xs" }, "location", -1),
							createTextVNode(" event fires before Inertia forces a full-page visit. Cancel it to intercept asset version changes with your own UI instead of a hard reload. ", -1)
						])]),
						_: 1
					}),
					createBaseVNode("div", _hoisted_4, [
						createVNode(FeatureCard_default, {
							title: "Simulate a Deployment",
							description: "The server reports a different asset version, triggering a 409 location visit."
						}, {
							default: withCtx(() => [createBaseVNode("div", _hoisted_5, [
								createVNode(unref(Button_default), {
									size: "sm",
									onClick: deploy
								}, {
									default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" Deploy new version ", -1)])]),
									_: 1
								}),
								_cache[7] || (_cache[7] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
									createTextVNode(" The asset version usually changes on its own after a deploy, since Inertia derives it from your compiled assets. Here we set it by hand with "),
									createBaseVNode("code", { class: "text-xs" }, "Inertia::version('new-asset-version-hash')"),
									createTextVNode(" to fake a deploy. The stale version triggers a reload, which our listener cancels in favor of the banner above. ")
								], -1)),
								createVNode(CodeBlock_default, {
									title: "Server (EventController):",
									code: "\n                                public function deployNewVersion(): RedirectResponse\n                                {\n                                    Inertia::version('new-asset-version-hash');\n\n                                    return back();\n                                }\n                            "
								})
							])]),
							_: 1
						}),
						createVNode(FeatureCard_default, {
							"info-card": "",
							title: "Intercepting the Event",
							description: "Listen for location and preventDefault on a version change."
						}, {
							default: withCtx(() => [createVNode(CodeBlock_default, { code: "\n                            router.on('location', (event) => {\n                              if (event.detail.versionChange) {\n                                event.preventDefault()\n                                showUpdateBanner()\n                              }\n                            })\n                        " }), _cache[8] || (_cache[8] = createBaseVNode("p", { class: "mt-4 text-xs text-muted-foreground" }, [
								createBaseVNode("code", { class: "text-xs" }, "versionChange"),
								createTextVNode(" is "),
								createBaseVNode("code", { class: "text-xs" }, "true"),
								createTextVNode(" for asset version mismatches and "),
								createBaseVNode("code", { class: "text-xs" }, "false"),
								createTextVNode(" for explicit "),
								createBaseVNode("code", { class: "text-xs" }, "Inertia::location()"),
								createTextVNode(" redirects. Background requests ("),
								createBaseVNode("code", { class: "text-xs" }, "router.reload()"),
								createTextVNode(", polling) no longer force a reload on version change; new assets are picked up on the next user-initiated visit. ")
							], -1))]),
							_: 1
						}),
						createVNode(FeatureCard_default, {
							"info-card": "",
							class: "lg:col-span-2",
							title: "Event Log"
						}, {
							description: withCtx(() => [..._cache[9] || (_cache[9] = [createBaseVNode("code", { class: "text-xs" }, "location", -1), createTextVNode(" events captured on this page. ", -1)])]),
							"header-action": withCtx(() => [createVNode(unref(Button_default), {
								variant: "ghost",
								size: "sm",
								onClick: _cache[1] || (_cache[1] = ($event) => eventLog.value = [])
							}, {
								default: withCtx(() => [..._cache[10] || (_cache[10] = [createTextVNode(" Clear ", -1)])]),
								_: 1
							})]),
							default: withCtx(() => [eventLog.value.length ? (openBlock(), createElementBlock("div", _hoisted_6, [(openBlock(true), createElementBlock(Fragment, null, renderList(eventLog.value, (entry, i) => {
								return openBlock(), createElementBlock("div", {
									key: i,
									class: "rounded bg-muted px-2 py-1 font-mono text-xs"
								}, toDisplayString(entry), 1);
							}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_7, " Deploy a new version to see the event fire. "))]),
							_: 1
						})
					])
				])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { LocationEvent_default as default };

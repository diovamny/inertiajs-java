import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, Zt as onUnmounted, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, rr as ref, yr as toDisplayString } from "./dist.js";
import { n as addToast, t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Errors/NetworkErrors.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "flex items-center gap-3" };
var _hoisted_5 = {
	key: 0,
	class: "max-h-48 space-y-1 overflow-y-auto"
};
var _hoisted_6 = {
	key: 1,
	class: "text-xs text-muted-foreground"
};
var _hoisted_7 = { class: "grid gap-3 sm:grid-cols-2" };
//#endregion
//#region resources/js/pages/Features/Errors/NetworkErrors.vue
var NetworkErrors_default = /* @__PURE__ */ defineComponent({
	__name: "NetworkErrors",
	setup(__props) {
		const breadcrumbs = [{ title: "Error Handling" }, { title: "Network Errors" }];
		const eventLog = ref([]);
		const interceptEnabled = ref(false);
		let removeListener = null;
		function log(message) {
			eventLog.value.unshift(`${(/* @__PURE__ */ new Date()).toLocaleTimeString()} - ${message}`);
			if (eventLog.value.length > 15) eventLog.value.pop();
		}
		function toggleIntercept() {
			if (interceptEnabled.value) {
				removeListener?.();
				removeListener = null;
				interceptEnabled.value = false;
				log("Global networkError listener removed");
			} else {
				removeListener = router.on("networkError", (event) => {
					event.preventDefault();
					log(`Intercepted network error, default behavior prevented`);
					addToast("Network error intercepted. Default behavior was prevented.", "warning");
				});
				interceptEnabled.value = true;
				log("Global networkError listener registered");
			}
		}
		onUnmounted(() => {
			removeListener?.();
		});
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Network Errors" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Network Errors",
					docs: "advanced/events#network-error",
					controller: "app/Http/Controllers/Feature/NetworkErrorController.php#L35"
				}, {
					default: withCtx(() => [..._cache[1] || (_cache[1] = [
						createTextVNode(" Handle network failures with ", -1),
						createBaseVNode("code", { class: "text-xs" }, "onNetworkError", -1),
						createTextVNode(" callback and ", -1),
						createBaseVNode("code", { class: "text-xs" }, "router.on('networkError')", -1),
						createTextVNode(". ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						title: "When Network Errors Occur",
						description: "Network errors fire when a request fails due to connectivity issues or when page component resolution fails."
					}, {
						default: withCtx(() => [..._cache[2] || (_cache[2] = [createBaseVNode("div", { class: "space-y-3" }, [createBaseVNode("div", { class: "space-y-2 rounded-lg border border-black/5 bg-neutral-50/80 p-3 font-mono text-xs dark:border-white/5 dark:bg-neutral-900/80" }, [createBaseVNode("p", null, "Common causes of network errors:"), createBaseVNode("ul", { class: "list-inside list-disc space-y-1" }, [
							createBaseVNode("li", null, "No internet connection"),
							createBaseVNode("li", null, "DNS resolution failure"),
							createBaseVNode("li", null, "Server unreachable / timeout"),
							createBaseVNode("li", null, "CORS errors"),
							createBaseVNode("li", null, "Request aborted by browser")
						])]), createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Network errors are different from HTTP exceptions. The server never responded at all. ")], -1)])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "Error Interception" }, {
						description: withCtx(() => [..._cache[3] || (_cache[3] = [
							createTextVNode(" Toggle global ", -1),
							createBaseVNode("code", { class: "text-xs" }, "networkError", -1),
							createTextVNode(" listener. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [
							createBaseVNode("div", _hoisted_4, [createVNode(unref(Button_default), {
								variant: interceptEnabled.value ? "default" : "outline",
								size: "sm",
								onClick: toggleIntercept
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(interceptEnabled.value ? "Disable" : "Enable") + " Interception ", 1)]),
								_: 1
							}, 8, ["variant"]), createVNode(unref(Badge_default), { variant: interceptEnabled.value ? "default" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(interceptEnabled.value ? "Active" : "Inactive"), 1)]),
								_: 1
							}, 8, ["variant"])]),
							_cache[4] || (_cache[4] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
								createTextVNode(" When active, "),
								createBaseVNode("code", null, "event.preventDefault()"),
								createTextVNode(" stops the default network error behavior. ")
							], -1)),
							_cache[5] || (_cache[5] = createBaseVNode("div", { class: "rounded-lg border border-black/5 bg-neutral-50/80 p-3 font-mono text-xs dark:border-white/5 dark:bg-neutral-900/80" }, [createBaseVNode("p", null, [createBaseVNode("strong", null, "To test:"), createTextVNode(" Open DevTools → Network → set throttling to \"Offline\", then try navigating. ")])], -1))
						])]),
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
							onClick: _cache[0] || (_cache[0] = ($event) => eventLog.value = [])
						}, {
							default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode("Clear", -1)])]),
							_: 1
						})]),
						default: withCtx(() => [eventLog.value.length ? (openBlock(), createElementBlock("div", _hoisted_5, [(openBlock(true), createElementBlock(Fragment, null, renderList(eventLog.value, (entry, i) => {
							return openBlock(), createElementBlock("div", {
								key: i,
								class: "rounded bg-muted px-2 py-1 font-mono text-xs"
							}, toDisplayString(entry), 1);
						}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_6, " Enable interception and simulate an offline state to see events. "))]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "API Reference"
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_7, [createVNode(CodeBlock_default, {
							title: "Per-visit callback:",
							code: "router.get('/url', {}, {\n  onNetworkError: (error) => {\n    // Return false to prevent default\n    return false\n  },\n})"
						}), createVNode(CodeBlock_default, {
							title: "Global event:",
							code: "router.on('networkError', (event) => {\n  console.log(event.detail.exception)\n  event.preventDefault()\n})"
						})])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { NetworkErrors_default as default };

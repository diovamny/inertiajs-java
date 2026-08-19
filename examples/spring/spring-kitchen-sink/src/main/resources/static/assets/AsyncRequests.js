import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, Yn as isRef, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, gr as normalizeClass, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, rr as ref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Navigation/AsyncRequests.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-3" };
var _hoisted_4 = { class: "space-y-3" };
var _hoisted_5 = { class: "space-y-3" };
var _hoisted_6 = { class: "flex gap-2" };
var _hoisted_7 = { class: "flex items-center gap-2" };
var _hoisted_8 = {
	key: 0,
	class: "max-h-64 space-y-1 overflow-y-auto"
};
var _hoisted_9 = {
	key: 1,
	class: "text-xs text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Features/Navigation/AsyncRequests.vue
var AsyncRequests_default = /* @__PURE__ */ defineComponent({
	__name: "AsyncRequests",
	props: { timestamp: {} },
	setup(__props) {
		const breadcrumbs = [{ title: "Navigation" }, { title: "Async Requests" }];
		const eventLog = ref([]);
		let requestCounter = 0;
		function log(message) {
			eventLog.value.unshift(`${(/* @__PURE__ */ new Date()).toLocaleTimeString()} - ${message}`);
			if (eventLog.value.length > 20) eventLog.value.pop();
		}
		function sendSyncRequest() {
			const id = ++requestCounter;
			log(`[#${id}] Sending SYNC request...`);
			router.visit("/features/navigation/async-requests", {
				preserveScroll: true,
				preserveState: true,
				onStart: () => log(`[#${id}] Started`),
				onSuccess: () => log(`[#${id}] Completed`),
				onCancel: () => log(`[#${id}] Cancelled (replaced by newer request)`)
			});
		}
		function sendAsyncRequest() {
			const id = ++requestCounter;
			log(`[#${id}] Sending ASYNC request...`);
			router.visit("/features/navigation/async-requests", {
				async: true,
				preserveScroll: true,
				preserveState: true,
				onStart: () => log(`[#${id}] Started (async)`),
				onSuccess: () => log(`[#${id}] Completed (async)`)
			});
		}
		let cancelToken = null;
		function sendCancellable() {
			const id = ++requestCounter;
			log(`[#${id}] Sending cancellable request (2s delay)...`);
			router.get("/features/navigation/async-slow", { delay: 2 }, {
				preserveScroll: true,
				preserveState: true,
				onCancelToken: (token) => {
					cancelToken = token;
				},
				onStart: () => log(`[#${id}] Started (cancellable)`),
				onSuccess: () => {
					log(`[#${id}] Completed`);
					cancelToken = null;
				},
				onCancel: () => {
					log(`[#${id}] Cancelled by user`);
					cancelToken = null;
				}
			});
		}
		function cancelRequest() {
			if (cancelToken) cancelToken.cancel();
			else log("No active cancellable request");
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Async Requests" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Async Requests",
					docs: "the-basics/manual-visits",
					controller: "app/Http/Controllers/Feature/NavigationController.php#L58"
				}, {
					default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" Request queuing behavior. Sync (default) vs async concurrent requests. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						title: "Synchronous (Default)",
						description: "Each new request cancels any in-flight request. Click rapidly to see cancellation."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createVNode(unref(Button_default), { onClick: _cache[0] || (_cache[0] = ($event) => sendSyncRequest()) }, {
							default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" Send Sync Request ", -1)])]),
							_: 1
						}), _cache[7] || (_cache[7] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Click multiple times quickly. Previous requests get cancelled. ", -1))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "Async (Concurrent)" }, {
						description: withCtx(() => [..._cache[8] || (_cache[8] = [
							createTextVNode(" With ", -1),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "async: true", -1),
							createTextVNode(", requests run concurrently without cancelling each other. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_4, [createVNode(unref(Button_default), { onClick: _cache[1] || (_cache[1] = ($event) => sendAsyncRequest()) }, {
							default: withCtx(() => [..._cache[9] || (_cache[9] = [createTextVNode(" Send Async Request ", -1)])]),
							_: 1
						}), _cache[10] || (_cache[10] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Click multiple times. All requests complete independently. ", -1))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "Cancel Token" }, {
						description: withCtx(() => [..._cache[11] || (_cache[11] = [
							createTextVNode(" Use ", -1),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "onCancelToken", -1),
							createTextVNode(" to manually cancel a specific request. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_5, [createBaseVNode("div", _hoisted_6, [createVNode(unref(Button_default), { onClick: _cache[2] || (_cache[2] = ($event) => sendCancellable()) }, {
							default: withCtx(() => [..._cache[12] || (_cache[12] = [createTextVNode(" Send Slow Request (2s) ", -1)])]),
							_: 1
						}), createVNode(unref(Button_default), {
							variant: "destructive",
							onClick: _cache[3] || (_cache[3] = ($event) => cancelRequest())
						}, {
							default: withCtx(() => [..._cache[13] || (_cache[13] = [createTextVNode(" Cancel ", -1)])]),
							_: 1
						})]), _cache[14] || (_cache[14] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Start a slow request, then cancel it before it completes. ", -1))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Server Response"
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_7, [_cache[15] || (_cache[15] = createBaseVNode("span", { class: "text-sm text-muted-foreground" }, "Timestamp:", -1)), createVNode(unref(Badge_default), { variant: "outline" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.timestamp), 1)]),
							_: 1
						})])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "Request Log"
					}, {
						"header-action": withCtx(() => [createVNode(unref(Button_default), {
							variant: "ghost",
							size: "sm",
							onClick: _cache[4] || (_cache[4] = ($event) => {
								eventLog.value = [];
								isRef(requestCounter) ? requestCounter.value = 0 : requestCounter = 0;
							})
						}, {
							default: withCtx(() => [..._cache[16] || (_cache[16] = [createTextVNode("Clear", -1)])]),
							_: 1
						})]),
						default: withCtx(() => [eventLog.value.length ? (openBlock(), createElementBlock("div", _hoisted_8, [(openBlock(true), createElementBlock(Fragment, null, renderList(eventLog.value, (entry, i) => {
							return openBlock(), createElementBlock("div", {
								key: i,
								class: normalizeClass(["rounded bg-muted px-2 py-1 font-mono text-xs", {
									"text-red-600 dark:text-red-400": entry.includes("Cancelled"),
									"text-green-600 dark:text-green-400": entry.includes("Completed")
								}])
							}, toDisplayString(entry), 3);
						}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_9, " Click buttons above to see request flow. "))]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { AsyncRequests_default as default };

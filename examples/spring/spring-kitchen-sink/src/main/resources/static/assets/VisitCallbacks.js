import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, rr as ref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Events/VisitCallbacks.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "flex flex-wrap gap-2" };
var _hoisted_5 = {
	key: 0,
	class: "max-h-64 space-y-1 overflow-y-auto"
};
var _hoisted_6 = {
	key: 1,
	class: "text-xs text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Features/Events/VisitCallbacks.vue
var VisitCallbacks_default = /* @__PURE__ */ defineComponent({
	__name: "VisitCallbacks",
	setup(__props) {
		const breadcrumbs = [{ title: "Events & Lifecycle" }, { title: "Visit Callbacks" }];
		const eventLog = ref([]);
		function log(event, detail) {
			eventLog.value.unshift(`${(/* @__PURE__ */ new Date()).toLocaleTimeString()} - ${event}${detail ? ": " + detail : ""}`);
			if (eventLog.value.length > 20) eventLog.value.pop();
		}
		function triggerWithCallbacks() {
			router.post("/features/events/visit-callbacks/action", {}, {
				preserveScroll: true,
				onBefore: () => {
					log("onBefore", "visit is about to start");
				},
				onStart: () => {
					log("onStart", "request started");
				},
				onProgress: (progress) => {
					log("onProgress", `${progress?.percentage}%`);
				},
				onSuccess: () => {
					log("onSuccess", "response received");
				},
				onError: (errors) => {
					log("onError", JSON.stringify(errors));
				},
				onFinish: () => {
					log("onFinish", "request completed");
				}
			});
		}
		function triggerWithCancel() {
			router.post("/features/events/visit-callbacks/action", {}, {
				preserveScroll: true,
				onBefore: () => {
					log("onBefore", "returning false, visit cancelled!");
					return false;
				},
				onCancel: () => {
					log("onCancel", "visit was cancelled");
				}
			});
		}
		function triggerWithCancelToken() {
			let token = null;
			router.post("/features/events/visit-callbacks/action", {}, {
				preserveScroll: true,
				onCancelToken: (cancelToken) => {
					token = cancelToken;
					log("onCancelToken", "received cancel token");
					setTimeout(() => {
						token?.cancel();
						log("manual cancel", "called cancelToken.cancel()");
					}, 50);
				},
				onCancel: () => {
					log("onCancel", "visit was cancelled via token");
				},
				onFinish: () => {
					log("onFinish", "finished after cancellation");
				}
			});
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Visit Callbacks" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Visit Callbacks",
					docs: "advanced/events#event-callbacks",
					controller: "app/Http/Controllers/Feature/EventController.php#L22"
				}, {
					default: withCtx(() => [..._cache[1] || (_cache[1] = [
						createTextVNode(" Per-visit event hooks passed as options to ", -1),
						createBaseVNode("code", { class: "text-xs" }, "router.visit()", -1),
						createTextVNode(", ", -1),
						createBaseVNode("code", { class: "text-xs" }, "router.post()", -1),
						createTextVNode(", etc. ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						title: "Trigger Callbacks",
						description: "Each button demonstrates different visit callback behaviors."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [
							createVNode(unref(Button_default), {
								size: "sm",
								onClick: triggerWithCallbacks
							}, {
								default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode(" Full Lifecycle ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: triggerWithCancel
							}, {
								default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" Cancel via onBefore ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: triggerWithCancelToken
							}, {
								default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode(" Cancel via Token ", -1)])]),
								_: 1
							})
						]), _cache[5] || (_cache[5] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Watch the event log to see the callback sequence. ", -1))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Available Callbacks",
						description: "All per-visit event hooks."
					}, {
						default: withCtx(() => [..._cache[6] || (_cache[6] = [createBaseVNode("div", { class: "space-y-2 text-xs" }, [
							createBaseVNode("div", { class: "flex justify-between" }, [createBaseVNode("code", null, "onBefore"), createBaseVNode("span", { class: "text-muted-foreground" }, "Return false to cancel")]),
							createBaseVNode("div", { class: "flex justify-between" }, [createBaseVNode("code", null, "onCancelToken"), createBaseVNode("span", { class: "text-muted-foreground" }, "Receive cancel token")]),
							createBaseVNode("div", { class: "flex justify-between" }, [createBaseVNode("code", null, "onStart"), createBaseVNode("span", { class: "text-muted-foreground" }, "Request started")]),
							createBaseVNode("div", { class: "flex justify-between" }, [createBaseVNode("code", null, "onProgress"), createBaseVNode("span", { class: "text-muted-foreground" }, "Upload progress")]),
							createBaseVNode("div", { class: "flex justify-between" }, [createBaseVNode("code", null, "onSuccess"), createBaseVNode("span", { class: "text-muted-foreground" }, "2xx response")]),
							createBaseVNode("div", { class: "flex justify-between" }, [createBaseVNode("code", null, "onError"), createBaseVNode("span", { class: "text-muted-foreground" }, "422 validation errors")]),
							createBaseVNode("div", { class: "flex justify-between" }, [createBaseVNode("code", null, "onHttpException"), createBaseVNode("span", { class: "text-muted-foreground" }, "Non-Inertia responses")]),
							createBaseVNode("div", { class: "flex justify-between" }, [createBaseVNode("code", null, "onNetworkError"), createBaseVNode("span", { class: "text-muted-foreground" }, "Network failures")]),
							createBaseVNode("div", { class: "flex justify-between" }, [createBaseVNode("code", null, "onCancel"), createBaseVNode("span", { class: "text-muted-foreground" }, "Visit was cancelled")]),
							createBaseVNode("div", { class: "flex justify-between" }, [createBaseVNode("code", null, "onFinish"), createBaseVNode("span", { class: "text-muted-foreground" }, "Always runs last")])
						], -1)])]),
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
							default: withCtx(() => [..._cache[7] || (_cache[7] = [createTextVNode("Clear", -1)])]),
							_: 1
						})]),
						default: withCtx(() => [eventLog.value.length ? (openBlock(), createElementBlock("div", _hoisted_5, [(openBlock(true), createElementBlock(Fragment, null, renderList(eventLog.value, (entry, i) => {
							return openBlock(), createElementBlock("div", {
								key: i,
								class: "rounded bg-muted px-2 py-1 font-mono text-xs"
							}, toDisplayString(entry), 1);
						}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_6, " Click a button to see visit callbacks. "))]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "Code Example"
					}, {
						default: withCtx(() => [createVNode(CodeBlock_default, { code: "\n                        router.post('/endpoint', data, {\n                          onBefore: (visit) => confirm('Proceed?'),\n                          onStart: (visit) => console.log('Started'),\n                          onSuccess: (page) => console.log('Success!'),\n                          onError: (errors) => console.log(errors),\n                          onFinish: (visit) => console.log('Done'),\n                        })\n                    " })]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { VisitCallbacks_default as default };

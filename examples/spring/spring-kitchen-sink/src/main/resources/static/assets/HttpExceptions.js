import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, Zt as onUnmounted, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, rr as ref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Errors/HttpExceptions.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "flex flex-wrap gap-2" };
var _hoisted_4 = { class: "flex flex-wrap gap-2" };
var _hoisted_5 = { class: "space-y-4" };
var _hoisted_6 = { class: "flex items-center gap-3" };
var _hoisted_7 = {
	key: 0,
	class: "max-h-48 space-y-1 overflow-y-auto"
};
var _hoisted_8 = {
	key: 1,
	class: "text-xs text-muted-foreground"
};
var _hoisted_9 = { class: "grid gap-3 sm:grid-cols-2" };
//#endregion
//#region resources/js/pages/Features/Errors/HttpExceptions.vue
var HttpExceptions_default = /* @__PURE__ */ defineComponent({
	__name: "HttpExceptions",
	setup(__props) {
		const breadcrumbs = [{ title: "Error Handling" }, { title: "HTTP Exceptions" }];
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
				log("Global httpException listener removed");
			} else {
				removeListener = router.on("httpException", (event) => {
					event.preventDefault();
					log(`Intercepted HTTP ${event.detail.response.status}. Default error modal suppressed.`);
				});
				interceptEnabled.value = true;
				log("Global httpException listener registered");
			}
		}
		function triggerUnhandledError() {
			router.get("/features/errors/http-exceptions/unhandled", {}, {
				preserveScroll: true,
				preserveState: true,
				onHttpException: (response) => {
					log(`onHttpException callback fired: HTTP ${response.status}`);
					if (interceptEnabled.value) return false;
				}
			});
		}
		onUnmounted(() => {
			removeListener?.();
		});
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "HTTP Exceptions" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "HTTP Exceptions",
					docs: "advanced/error-handling",
					controller: "app/Http/Controllers/Feature/NetworkErrorController.php#L10"
				}, {
					default: withCtx(() => [..._cache[1] || (_cache[1] = [
						createTextVNode(" Errors handled by ", -1),
						createBaseVNode("code", { class: "text-xs" }, "handleExceptionsUsing()", -1),
						createTextVNode(" render as full Inertia pages. Unhandled errors can be intercepted client-side with ", -1),
						createBaseVNode("code", { class: "text-xs" }, "onHttpException", -1),
						createTextVNode(". ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						title: "Server-Handled Errors",
						description: "These status codes are handled by handleExceptionsUsing() and render the ErrorPage component. Use your browser's back button to return."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [
							createVNode(unref(Button_default), {
								"as-child": "",
								variant: "outline",
								size: "sm"
							}, {
								default: withCtx(() => [createVNode(unref(link_default), { href: "/features/errors/http-exceptions/403" }, {
									default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode(" 403 Forbidden ", -1)])]),
									_: 1
								})]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								"as-child": "",
								variant: "outline",
								size: "sm"
							}, {
								default: withCtx(() => [createVNode(unref(link_default), { href: "/features/errors/http-exceptions/404" }, {
									default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" 404 Not Found ", -1)])]),
									_: 1
								})]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								"as-child": "",
								variant: "destructive",
								size: "sm"
							}, {
								default: withCtx(() => [createVNode(unref(link_default), { href: "/features/errors/http-exceptions/500" }, {
									default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode(" 500 Server Error ", -1)])]),
									_: 1
								})]),
								_: 1
							})
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "Java Exceptions",
						description: "Business exceptions are mapped to their semantic HTTP status by the adapter and render the ErrorPage component, just like HTTP exceptions."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_4, [
							createVNode(unref(Button_default), {
								"as-child": "",
								variant: "outline",
								size: "sm"
							}, {
								default: withCtx(() => [createVNode(unref(link_default), { href: "/features/errors/java-exceptions/400" }, {
									default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" 400 IllegalArgumentException ", -1)])]),
									_: 1
								})]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								"as-child": "",
								variant: "outline",
								size: "sm"
							}, {
								default: withCtx(() => [createVNode(unref(link_default), { href: "/features/errors/java-exceptions/403" }, {
									default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" 403 SecurityException ", -1)])]),
									_: 1
								})]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								"as-child": "",
								variant: "outline",
								size: "sm"
							}, {
								default: withCtx(() => [createVNode(unref(link_default), { href: "/features/errors/java-exceptions/409" }, {
									default: withCtx(() => [..._cache[7] || (_cache[7] = [createTextVNode(" 409 IllegalStateException ", -1)])]),
									_: 1
								})]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								"as-child": "",
								variant: "outline",
								size: "sm"
							}, {
								default: withCtx(() => [createVNode(unref(link_default), { href: "/features/errors/java-exceptions/422" }, {
									default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode(" 422 ValidationException ", -1)])]),
									_: 1
								})]),
								_: 1
							})
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "Client-Side Interception",
						description: "Errors NOT handled server-side trigger the httpException event. Enable interception to suppress the default error modal and stay on this page."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_5, [
							createBaseVNode("div", _hoisted_6, [createVNode(unref(Button_default), {
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
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: triggerUnhandledError
							}, {
								default: withCtx(() => [..._cache[9] || (_cache[9] = [createTextVNode(" 418 I'm a Teapot ", -1)])]),
								_: 1
							}),
							_cache[10] || (_cache[10] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
								createTextVNode(" Without interception, this shows the default error modal. With interception enabled, "),
								createBaseVNode("code", null, "event.preventDefault()"),
								createTextVNode(" suppresses it. ")
							], -1))
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
							default: withCtx(() => [..._cache[11] || (_cache[11] = [createTextVNode("Clear", -1)])]),
							_: 1
						})]),
						default: withCtx(() => [eventLog.value.length ? (openBlock(), createElementBlock("div", _hoisted_7, [(openBlock(true), createElementBlock(Fragment, null, renderList(eventLog.value, (entry, i) => {
							return openBlock(), createElementBlock("div", {
								key: i,
								class: "rounded bg-muted px-2 py-1 font-mono text-xs"
							}, toDisplayString(entry), 1);
						}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_8, " Enable interception and trigger an error to see events. "))]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "API Reference"
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_9, [createVNode(CodeBlock_default, {
							title: "Server-side (handled):",
							code: "Inertia::handleExceptionsUsing(function (ExceptionResponse $response) {\n  if (in_array($response->statusCode(), [403, 404, 419, 429, 500, 503])) {\n    return $response->render('ErrorPage', [\n      'status' => $response->statusCode(),\n    ])->withSharedData();\n  }\n});"
						}), createVNode(CodeBlock_default, {
							title: "Client-side (intercepted):",
							code: "router.on('httpException', (event) => {\n  console.log(event.detail.response)\n  event.preventDefault()\n})"
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
export { HttpExceptions_default as default };

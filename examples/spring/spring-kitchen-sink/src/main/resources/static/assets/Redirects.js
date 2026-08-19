import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, Dn as watch, G as Fragment, Mn as withCtx, Sr as router, at as createElementBlock, dt as createTextVNode, f as usePage, ft as createVNode, i as head_default, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, pr as unref, rr as ref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Navigation/Redirects.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "flex flex-wrap items-center gap-4" };
var _hoisted_4 = { class: "flex items-center gap-2" };
var _hoisted_5 = {
	key: 0,
	class: "flex items-center gap-2"
};
var _hoisted_6 = { class: "space-y-3" };
var _hoisted_7 = { class: "space-y-3" };
var _hoisted_8 = { class: "space-y-3" };
//#endregion
//#region resources/js/pages/Features/Navigation/Redirects.vue
var Redirects_default = /* @__PURE__ */ defineComponent({
	__name: "Redirects",
	props: { timestamp: {} },
	setup(__props) {
		const breadcrumbs = [{ title: "Navigation" }, { title: "Redirects" }];
		const page = usePage();
		const flashMessage = ref(null);
		watch(() => page.flash, () => {
			const message = page.flash?.message;
			if (typeof message === "string" && message) flashMessage.value = message;
		}, { deep: true });
		router.on("success", () => {
			const message = page.flash?.message;
			if (typeof message === "string" && message) flashMessage.value = message;
		});
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Redirects" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Redirects",
					docs: "the-basics/redirects",
					controller: "app/Http/Controllers/Feature/NavigationController.php#L111"
				}, {
					default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" Server-side redirect patterns in Inertia. All redirects from PUT, PATCH, and DELETE are automatically converted to 303 responses. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Server Response",
						class: "lg:col-span-2"
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [_cache[4] || (_cache[4] = createBaseVNode("span", { class: "text-sm text-muted-foreground" }, "Timestamp:", -1)), createVNode(unref(Badge_default), { variant: "outline" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.timestamp), 1)]),
							_: 1
						})]), flashMessage.value ? (openBlock(), createElementBlock("div", _hoisted_5, [_cache[5] || (_cache[5] = createBaseVNode("span", { class: "text-sm text-muted-foreground" }, "Flash:", -1)), createVNode(unref(Badge_default), { variant: "default" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(flashMessage.value), 1)]),
							_: 1
						})])) : createCommentVNode("", true)])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "redirect()->back()",
						description: "The most common pattern. Redirects back to the previous page after processing a form or action."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_6, [_cache[7] || (_cache[7] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
							createTextVNode(" Uses "),
							createBaseVNode("code", null, "Inertia::flash('message', ...)->back()"),
							createTextVNode(" to redirect back with flash data. ")
						], -1)), createVNode(unref(Button_default), {
							variant: "outline",
							size: "sm",
							onClick: _cache[0] || (_cache[0] = ($event) => unref(router).post("/features/navigation/redirects/back", {}, { preserveScroll: true }))
						}, {
							default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" Submit and redirect back ", -1)])]),
							_: 1
						})])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "to_route()",
						description: "Redirect to a specific named route. Commonly used after creating or updating a resource."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_7, [_cache[9] || (_cache[9] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
							createTextVNode(" Uses "),
							createBaseVNode("code", null, "Inertia::flash(...)"),
							createTextVNode(" then "),
							createBaseVNode("code", null, "to_route('features.navigation.redirects')"),
							createTextVNode(" to redirect to a named route. ")
						], -1)), createVNode(unref(Button_default), {
							variant: "outline",
							size: "sm",
							onClick: _cache[1] || (_cache[1] = ($event) => unref(router).post("/features/navigation/redirects/to-route"))
						}, {
							default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode(" Submit and redirect to route ", -1)])]),
							_: 1
						})])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "Inertia::location()",
						description: "External redirects that leave the Inertia app. Generates a 409 response with an X-Inertia-Location header."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_8, [_cache[11] || (_cache[11] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
							createTextVNode(" Triggers a full page visit to an external URL via "),
							createBaseVNode("code", null, "window.location"),
							createTextVNode(". The browser navigates away from the SPA entirely. ")
						], -1)), createVNode(unref(Button_default), {
							variant: "outline",
							size: "sm",
							onClick: _cache[2] || (_cache[2] = ($event) => unref(router).post("/features/navigation/redirects/external"))
						}, {
							default: withCtx(() => [..._cache[10] || (_cache[10] = [createTextVNode(" Redirect externally ", -1)])]),
							_: 1
						})])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "303 Redirect Behavior",
						description: "PUT, PATCH, and DELETE requests are automatically converted to GET redirects via 303 status."
					}, {
						default: withCtx(() => [..._cache[12] || (_cache[12] = [createBaseVNode("div", { class: "space-y-3" }, [createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " This prevents browsers from resubmitting the original request method on redirect. Inertia's server-side adapter handles this automatically. "), createBaseVNode("div", { class: "rounded-lg border border-dashed p-3 text-xs text-muted-foreground" }, [createBaseVNode("p", { class: "font-medium text-foreground" }, " How it works: "), createBaseVNode("ol", { class: "mt-2 list-inside list-decimal space-y-1" }, [
							createBaseVNode("li", null, "Client sends PUT/PATCH/DELETE request"),
							createBaseVNode("li", null, "Server responds with 303 redirect"),
							createBaseVNode("li", null, " Browser follows redirect with GET request "),
							createBaseVNode("li", null, "Inertia renders the redirected page")
						])])], -1)])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { Redirects_default as default };

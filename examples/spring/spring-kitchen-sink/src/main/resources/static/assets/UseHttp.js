import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, at as createElementBlock, d as useHttp, dt as createTextVNode, ft as createVNode, i as head_default, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, pr as unref, rr as ref, rt as createBlock, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Label_default } from "./Label.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Input_default } from "./Input.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Http/UseHttp.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "space-y-2" };
var _hoisted_5 = { class: "flex items-center gap-2" };
var _hoisted_6 = {
	key: 1,
	class: "text-sm text-muted-foreground"
};
var _hoisted_7 = { class: "space-y-2 text-sm" };
var _hoisted_8 = { class: "flex items-center justify-between" };
var _hoisted_9 = { class: "flex items-center justify-between" };
var _hoisted_10 = { class: "flex items-center justify-between" };
var _hoisted_11 = { class: "flex items-center justify-between" };
var _hoisted_12 = { class: "flex items-center justify-between" };
var _hoisted_13 = {
	key: 1,
	class: "text-xs text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Features/Http/UseHttp.vue
var UseHttp_default = /* @__PURE__ */ defineComponent({
	__name: "UseHttp",
	setup(__props) {
		const breadcrumbs = [{ title: "HTTP" }, { title: "useHttp" }];
		const http = useHttp({ name: "" });
		const response = ref(null);
		const cancelled = ref(false);
		function submitForm() {
			cancelled.value = false;
			http.post("/features/http/use-http/api", { onSuccess: (res) => {
				response.value = res;
			} });
		}
		function cancelRequest() {
			http.cancel();
			cancelled.value = true;
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "useHttp" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "useHttp",
					docs: "the-basics/http-requests",
					controller: "app/Http/Controllers/Feature/HttpController.php#L12"
				}, {
					default: withCtx(() => [..._cache[1] || (_cache[1] = [
						createTextVNode(" Non-Inertia HTTP requests with ", -1),
						createBaseVNode("code", { class: "text-xs" }, "useHttp()", -1),
						createTextVNode(". Like ", -1),
						createBaseVNode("code", { class: "text-xs" }, "useForm()", -1),
						createTextVNode(" but returns JSON, no page visit. ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						title: "API Request Demo",
						description: "Send a POST request to a JSON endpoint. No page navigation occurs."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [createVNode(unref(Label_default), { for: "name" }, {
							default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode("Your Name", -1)])]),
							_: 1
						}), createVNode(unref(Input_default), {
							id: "name",
							modelValue: unref(http).name,
							"onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => unref(http).name = $event),
							placeholder: "Enter your name..."
						}, null, 8, ["modelValue"])]), createBaseVNode("div", _hoisted_5, [
							createVNode(unref(Button_default), {
								size: "sm",
								disabled: unref(http).processing,
								onClick: submitForm
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(http).processing ? "Sending..." : "Send Request"), 1)]),
								_: 1
							}, 8, ["disabled"]),
							unref(http).processing ? (openBlock(), createBlock(unref(Button_default), {
								key: 0,
								variant: "destructive",
								size: "sm",
								onClick: cancelRequest
							}, {
								default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" Cancel ", -1)])]),
								_: 1
							})) : createCommentVNode("", true),
							cancelled.value ? (openBlock(), createElementBlock("span", _hoisted_6, "Request cancelled")) : createCommentVNode("", true)
						])])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Reactive State"
					}, {
						description: withCtx(() => [..._cache[4] || (_cache[4] = [
							createTextVNode(" All the state properties from ", -1),
							createBaseVNode("code", { class: "text-xs" }, "useHttp()", -1),
							createTextVNode(". ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_7, [
							createBaseVNode("div", _hoisted_8, [_cache[5] || (_cache[5] = createBaseVNode("span", null, "processing", -1)), createVNode(unref(Badge_default), { variant: unref(http).processing ? "default" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(http).processing), 1)]),
								_: 1
							}, 8, ["variant"])]),
							createBaseVNode("div", _hoisted_9, [_cache[6] || (_cache[6] = createBaseVNode("span", null, "isDirty", -1)), createVNode(unref(Badge_default), { variant: unref(http).isDirty ? "default" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(http).isDirty), 1)]),
								_: 1
							}, 8, ["variant"])]),
							createBaseVNode("div", _hoisted_10, [_cache[7] || (_cache[7] = createBaseVNode("span", null, "wasSuccessful", -1)), createVNode(unref(Badge_default), { variant: unref(http).wasSuccessful ? "default" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(http).wasSuccessful), 1)]),
								_: 1
							}, 8, ["variant"])]),
							createBaseVNode("div", _hoisted_11, [_cache[8] || (_cache[8] = createBaseVNode("span", null, "recentlySuccessful", -1)), createVNode(unref(Badge_default), { variant: unref(http).recentlySuccessful ? "default" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(http).recentlySuccessful), 1)]),
								_: 1
							}, 8, ["variant"])]),
							createBaseVNode("div", _hoisted_12, [_cache[9] || (_cache[9] = createBaseVNode("span", null, "hasErrors", -1)), createVNode(unref(Badge_default), { variant: unref(http).hasErrors ? "destructive" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(http).hasErrors), 1)]),
								_: 1
							}, 8, ["variant"])])
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Response",
						description: "The JSON response from the API endpoint."
					}, {
						default: withCtx(() => [response.value ? (openBlock(), createBlock(CodeBlock_default, {
							key: 0,
							code: JSON.stringify(response.value, null, 2)
						}, null, 8, ["code"])) : (openBlock(), createElementBlock("p", _hoisted_13, " Send a request to see the response. "))]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "useHttp vs useForm",
						description: "When to use which."
					}, {
						default: withCtx(() => [..._cache[10] || (_cache[10] = [createBaseVNode("div", { class: "space-y-3 text-xs" }, [createBaseVNode("div", { class: "rounded-md border border-black/10 p-2 dark:border-white/10" }, [createBaseVNode("p", { class: "font-semibold" }, "useForm()"), createBaseVNode("p", { class: "text-muted-foreground" }, " Triggers Inertia page visits. Server returns Inertia responses. Page component gets swapped. ")]), createBaseVNode("div", { class: "rounded-md border-2 border-primary p-2" }, [createBaseVNode("p", { class: "font-semibold" }, "useHttp()"), createBaseVNode("p", { class: "text-muted-foreground" }, " Plain HTTP requests. Server returns JSON. No page navigation. Great for API endpoints, toggles, status checks. ")])], -1)])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { UseHttp_default as default };

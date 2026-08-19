import { $t as openBlock, G as Fragment, Mn as withCtx, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/State/SharedProps.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "space-y-3" };
var _hoisted_5 = { class: "flex items-center justify-between" };
var _hoisted_6 = { class: "flex items-center justify-between" };
var _hoisted_7 = { class: "flex items-center justify-between" };
var _hoisted_8 = { class: "flex items-center justify-between" };
var _hoisted_9 = { class: "space-y-3" };
var _hoisted_10 = { class: "flex items-center justify-between" };
var _hoisted_11 = { class: "flex items-center justify-between" };
var _hoisted_12 = { class: "flex items-center justify-between" };
//#endregion
//#region resources/js/pages/Features/State/SharedProps.vue
var SharedProps_default = /* @__PURE__ */ defineComponent({
	__name: "SharedProps",
	setup(__props) {
		const breadcrumbs = [{ title: "State Management" }, { title: "Shared Props" }];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Shared Props" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Shared Props",
					docs: "data-props/shared-data",
					controller: "app/Http/Controllers/Feature/StateController.php#L42"
				}, {
					default: withCtx(() => [..._cache[0] || (_cache[0] = [
						createTextVNode(" Global data shared across all pages via ", -1),
						createBaseVNode("code", { class: "text-xs" }, "HandleInertiaRequests", -1),
						createTextVNode(" middleware. ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, { title: "Current Shared Props" }, {
						description: withCtx(() => [..._cache[1] || (_cache[1] = [
							createTextVNode(" Accessed via ", -1),
							createBaseVNode("code", { class: "text-xs" }, "usePage()", -1),
							createTextVNode(". Available on every page without explicitly passing them. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [
							createBaseVNode("div", _hoisted_5, [_cache[2] || (_cache[2] = createBaseVNode("span", { class: "text-sm font-medium" }, "App Name", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(_ctx.$page.props.name), 1)]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_6, [_cache[3] || (_cache[3] = createBaseVNode("span", { class: "text-sm font-medium" }, "Auth User", -1)), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(_ctx.$page.props.auth?.user?.name ?? "null"), 1)]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_7, [_cache[4] || (_cache[4] = createBaseVNode("span", { class: "text-sm font-medium" }, "User Email", -1)), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "font-mono text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(_ctx.$page.props.auth?.user?.email ?? "null"), 1)]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_8, [_cache[5] || (_cache[5] = createBaseVNode("span", { class: "text-sm font-medium" }, "Sidebar Open", -1)), createVNode(unref(Badge_default), { variant: _ctx.$page.props.sidebarOpen ? "default" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(_ctx.$page.props.sidebarOpen), 1)]),
								_: 1
							}, 8, ["variant"])])
						])])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "usePage() Data"
					}, {
						description: withCtx(() => [..._cache[6] || (_cache[6] = [
							createTextVNode(" The full ", -1),
							createBaseVNode("code", { class: "text-xs" }, "usePage()", -1),
							createTextVNode(" object includes props, url, component, and more. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_9, [
							createBaseVNode("div", _hoisted_10, [_cache[7] || (_cache[7] = createBaseVNode("span", { class: "text-sm font-medium" }, "URL", -1)), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "font-mono text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(_ctx.$page.url), 1)]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_11, [_cache[8] || (_cache[8] = createBaseVNode("span", { class: "text-sm font-medium" }, "Component", -1)), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "font-mono text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(_ctx.$page.component), 1)]),
								_: 1
							})]),
							createBaseVNode("div", _hoisted_12, [_cache[9] || (_cache[9] = createBaseVNode("span", { class: "text-sm font-medium" }, "Version", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(_ctx.$page.version ?? "null"), 1)]),
								_: 1
							})])
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "Server Configuration",
						description: "How shared props are defined in the HandleInertiaRequests middleware."
					}, {
						default: withCtx(() => [createVNode(CodeBlock_default, { code: "// app/Http/Middleware/HandleInertiaRequests.php\n\npublic function share(Request $request): array\n{\n    return [\n        ...parent::share($request),\n        'name' => config('app.name'),\n        'auth' => [\n            'user' => $request->user(),\n        ],\n        'sidebarOpen' => ! $request->hasCookie('sidebar_state')\n            || $request->cookie('sidebar_state') === 'true',\n    ];\n}" })]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { SharedProps_default as default };

import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Events/Progress.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "flex flex-wrap gap-2" };
var _hoisted_5 = { class: "space-y-3" };
var _hoisted_6 = { class: "space-y-3" };
var _hoisted_7 = { class: "flex items-center gap-3" };
var _hoisted_8 = { class: "flex items-center gap-3" };
var _hoisted_9 = { class: "flex items-center gap-3" };
var _hoisted_10 = { class: "flex items-center gap-3" };
//#endregion
//#region resources/js/pages/Features/Events/Progress.vue
var Progress_default = /* @__PURE__ */ defineComponent({
	__name: "Progress",
	setup(__props) {
		const breadcrumbs = [{ title: "Events & Lifecycle" }, { title: "Progress Bar" }];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Progress Bar" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Progress Bar",
					docs: "advanced/progress-indicators",
					controller: "app/Http/Controllers/Feature/EventController.php#L32"
				}, {
					default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" Built-in progress indicator with configurable delay, color, and spinner. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						title: "Trigger Progress",
						description: "The progress bar appears after a configurable delay (default 250ms)."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [
							createVNode(unref(Button_default), {
								size: "sm",
								onClick: _cache[0] || (_cache[0] = ($event) => unref(router).get("/features/events/progress/slow"))
							}, {
								default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode(" Slow Request (2s) ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[1] || (_cache[1] = ($event) => unref(router).reload())
							}, {
								default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" Normal Reload ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[2] || (_cache[2] = ($event) => unref(router).reload({ showProgress: false }))
							}, {
								default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" No Progress Bar ", -1)])]),
								_: 1
							})
						]), _cache[7] || (_cache[7] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " The slow request takes 2 seconds. Watch the progress bar at the top of the page. ", -1))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Configuration"
					}, {
						description: withCtx(() => [..._cache[8] || (_cache[8] = [
							createTextVNode(" Set in ", -1),
							createBaseVNode("code", { class: "text-xs" }, "createInertiaApp", -1),
							createTextVNode(". ", -1)
						])]),
						default: withCtx(() => [createVNode(CodeBlock_default, { code: "\n                        createInertiaApp({\n                          progress: {\n                            delay: 250,        // ms before bar shows\n                            color: '#29d',     // bar color\n                            includeCSS: true,  // include NProgress styles\n                            showSpinner: false, // show spinner icon\n                          },\n                        })\n                    " })]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Per-Visit Control",
						description: "Disable progress bar for specific visits."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_5, [createVNode(CodeBlock_default, {
							title: "Disable per visit:",
							code: "\n                                router.get('/url', {}, {\n                                  showProgress: false,\n                                })\n                            "
						}), createVNode(CodeBlock_default, {
							title: "Async visits:",
							code: "\n                                // Async requests hide progress by default\n                                router.get('/url', {}, {\n                                  async: true,\n                                  showProgress: true, // opt-in\n                                })\n                            "
						})])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "How It Works"
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_6, [
							createBaseVNode("div", _hoisted_7, [createVNode(unref(Badge_default), {
								variant: "outline",
								class: "w-16 justify-center"
							}, {
								default: withCtx(() => [..._cache[9] || (_cache[9] = [createTextVNode("1", -1)])]),
								_: 1
							}), _cache[10] || (_cache[10] = createBaseVNode("span", { class: "text-sm" }, "Visit starts. Delay timer begins (default 250ms)", -1))]),
							createBaseVNode("div", _hoisted_8, [createVNode(unref(Badge_default), {
								variant: "outline",
								class: "w-16 justify-center"
							}, {
								default: withCtx(() => [..._cache[11] || (_cache[11] = [createTextVNode("2", -1)])]),
								_: 1
							}), _cache[12] || (_cache[12] = createBaseVNode("span", { class: "text-sm" }, "If still loading after delay. Progress bar appears", -1))]),
							createBaseVNode("div", _hoisted_9, [createVNode(unref(Badge_default), {
								variant: "outline",
								class: "w-16 justify-center"
							}, {
								default: withCtx(() => [..._cache[13] || (_cache[13] = [createTextVNode("3", -1)])]),
								_: 1
							}), _cache[14] || (_cache[14] = createBaseVNode("span", { class: "text-sm" }, "Bar trickles forward automatically (easing animation)", -1))]),
							createBaseVNode("div", _hoisted_10, [createVNode(unref(Badge_default), {
								variant: "outline",
								class: "w-16 justify-center"
							}, {
								default: withCtx(() => [..._cache[15] || (_cache[15] = [createTextVNode("4", -1)])]),
								_: 1
							}), _cache[16] || (_cache[16] = createBaseVNode("span", { class: "text-sm" }, "Visit completes. Bar fills to 100% and fades out", -1))])
						])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { Progress_default as default };

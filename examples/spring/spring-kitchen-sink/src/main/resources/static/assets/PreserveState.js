import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, rr as ref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Input_default } from "./Input.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Navigation/PreserveState.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "flex items-center gap-3" };
var _hoisted_5 = { class: "space-y-2" };
var _hoisted_6 = { class: "space-y-3" };
var _hoisted_7 = { class: "flex items-center justify-between" };
var _hoisted_8 = { class: "flex items-center justify-between" };
var _hoisted_9 = { class: "flex flex-wrap gap-3" };
var _hoisted_10 = { class: "space-y-1" };
var _hoisted_11 = { class: "space-y-1" };
var _hoisted_12 = { class: "space-y-1" };
var _hoisted_13 = { class: "mt-4 flex flex-wrap gap-3" };
var _hoisted_14 = { class: "space-y-1" };
var _hoisted_15 = { class: "space-y-1" };
//#endregion
//#region resources/js/pages/Features/Navigation/PreserveState.vue
var PreserveState_default = /* @__PURE__ */ defineComponent({
	__name: "PreserveState",
	props: {
		serverCounter: {},
		timestamp: {}
	},
	setup(__props) {
		const breadcrumbs = [{ title: "Navigation" }, { title: "Preserve State" }];
		const localCounter = ref(0);
		const textInput = ref("");
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Preserve State" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Preserve State",
					docs: "the-basics/manual-visits#state-preservation",
					controller: "app/Http/Controllers/Feature/NavigationController.php#L25"
				}, {
					default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" Compare navigation with and without preserveState to see how local component state behaves. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						title: "Local Component State",
						description: "These values live in the Vue component. They reset on full re-render."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [
							_cache[9] || (_cache[9] = createBaseVNode("span", { class: "text-sm font-medium" }, "Counter:", -1)),
							createVNode(unref(Badge_default), {
								variant: "outline",
								class: "min-w-10 justify-center text-lg tabular-nums"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(localCounter.value), 1)]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								size: "sm",
								onClick: _cache[0] || (_cache[0] = ($event) => localCounter.value++)
							}, {
								default: withCtx(() => [..._cache[7] || (_cache[7] = [createTextVNode("+1", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								size: "sm",
								variant: "outline",
								onClick: _cache[1] || (_cache[1] = ($event) => localCounter.value = 0)
							}, {
								default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode("Reset", -1)])]),
								_: 1
							})
						]), createBaseVNode("div", _hoisted_5, [_cache[10] || (_cache[10] = createBaseVNode("span", { class: "text-sm font-medium" }, "Text input:", -1)), createVNode(unref(Input_default), {
							modelValue: textInput.value,
							"onUpdate:modelValue": _cache[2] || (_cache[2] = ($event) => textInput.value = $event),
							placeholder: "Type something here..."
						}, null, 8, ["modelValue"])])])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Server Props",
						description: "These come from the server and always update."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_6, [createBaseVNode("div", _hoisted_7, [_cache[11] || (_cache[11] = createBaseVNode("span", { class: "text-sm font-medium" }, "serverCounter", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.serverCounter), 1)]),
							_: 1
						})]), createBaseVNode("div", _hoisted_8, [_cache[12] || (_cache[12] = createBaseVNode("span", { class: "text-sm font-medium" }, "timestamp", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.timestamp), 1)]),
							_: 1
						})])])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "Try It"
					}, {
						description: withCtx(() => [..._cache[13] || (_cache[13] = [
							createTextVNode(" Increment the counter and type some text, then click each button below. Notice how ", -1),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "preserveState", -1),
							createTextVNode(" keeps local state while still updating server props. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_9, [
							createBaseVNode("div", _hoisted_10, [createVNode(unref(Button_default), {
								variant: "destructive",
								onClick: _cache[3] || (_cache[3] = ($event) => unref(router).visit("/features/navigation/preserve-state"))
							}, {
								default: withCtx(() => [..._cache[14] || (_cache[14] = [createTextVNode(" Without preserveState ", -1)])]),
								_: 1
							}), _cache[15] || (_cache[15] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Counter & input reset to defaults. ", -1))]),
							createBaseVNode("div", _hoisted_11, [createVNode(unref(Button_default), { onClick: _cache[4] || (_cache[4] = ($event) => unref(router).visit("/features/navigation/preserve-state", { preserveState: true })) }, {
								default: withCtx(() => [..._cache[16] || (_cache[16] = [createTextVNode(" With preserveState: true ", -1)])]),
								_: 1
							}), _cache[17] || (_cache[17] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Counter & input are preserved. ", -1))]),
							createBaseVNode("div", _hoisted_12, [createVNode(unref(Button_default), {
								variant: "secondary",
								onClick: _cache[5] || (_cache[5] = ($event) => unref(router).reload())
							}, {
								default: withCtx(() => [..._cache[18] || (_cache[18] = [createTextVNode(" router.reload() ", -1)])]),
								_: 1
							}), _cache[19] || (_cache[19] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Reload preserves state by default. ", -1))])
						]), createBaseVNode("div", _hoisted_13, [createBaseVNode("div", _hoisted_14, [createVNode(unref(link_default), {
							href: "/features/navigation/preserve-state",
							class: "inline-flex items-center rounded-md border border-black/10 bg-background px-3 py-1.5 text-sm hover:bg-accent dark:border-white/10"
						}, {
							default: withCtx(() => [..._cache[20] || (_cache[20] = [createTextVNode(" <Link> (no preserveState) ", -1)])]),
							_: 1
						}), _cache[21] || (_cache[21] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " State resets. ", -1))]), createBaseVNode("div", _hoisted_15, [createVNode(unref(link_default), {
							href: "/features/navigation/preserve-state",
							"preserve-state": "",
							class: "inline-flex items-center rounded-md border border-black/10 bg-background px-3 py-1.5 text-sm hover:bg-accent dark:border-white/10"
						}, {
							default: withCtx(() => [..._cache[22] || (_cache[22] = [createTextVNode(" <Link preserve-state> ", -1)])]),
							_: 1
						}), _cache[23] || (_cache[23] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " State preserved. ", -1))])])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { PreserveState_default as default };

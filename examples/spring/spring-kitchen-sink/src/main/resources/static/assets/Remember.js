import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, I as vModelText, Mn as withCtx, Pn as withDirectives, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, m as useRemember, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Label_default } from "./Label.js";
import { t as Input_default } from "./Input.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/State/Remember.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "space-y-2" };
var _hoisted_5 = { class: "space-y-2" };
var _hoisted_6 = { class: "space-y-2" };
var _hoisted_7 = { class: "flex gap-2 pt-2" };
var _hoisted_8 = { class: "space-y-4" };
var _hoisted_9 = { class: "flex items-center gap-4" };
//#endregion
//#region resources/js/pages/Features/State/Remember.vue
var Remember_default = /* @__PURE__ */ defineComponent({
	__name: "Remember",
	setup(__props) {
		const breadcrumbs = [{ title: "State Management" }, { title: "Remember" }];
		const form = useRemember({
			name: "",
			email: "",
			notes: ""
		}, "remember-demo");
		const counter = useRemember({ count: 0 }, "remember-counter");
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Remember" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Remember",
					docs: "data-props/remembering-state",
					controller: "app/Http/Controllers/Feature/StateController.php#L11"
				}, {
					default: withCtx(() => [..._cache[6] || (_cache[6] = [
						createTextVNode(" State persistence across browser history navigation with ", -1),
						createBaseVNode("code", { class: "text-xs" }, "useRemember()", -1),
						createTextVNode(". ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [createVNode(FeatureCard_default, {
					title: "Remembered Form State",
					description: "Fill in fields, navigate away, then press the browser back button. The form state is restored."
				}, {
					default: withCtx(() => [createBaseVNode("div", _hoisted_3, [
						createBaseVNode("div", _hoisted_4, [createVNode(unref(Label_default), { for: "name" }, {
							default: withCtx(() => [..._cache[7] || (_cache[7] = [createTextVNode("Name", -1)])]),
							_: 1
						}), createVNode(unref(Input_default), {
							id: "name",
							modelValue: unref(form).name,
							"onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => unref(form).name = $event),
							placeholder: "John Doe"
						}, null, 8, ["modelValue"])]),
						createBaseVNode("div", _hoisted_5, [createVNode(unref(Label_default), { for: "email" }, {
							default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode("Email", -1)])]),
							_: 1
						}), createVNode(unref(Input_default), {
							id: "email",
							type: "text",
							modelValue: unref(form).email,
							"onUpdate:modelValue": _cache[1] || (_cache[1] = ($event) => unref(form).email = $event),
							placeholder: "john@example.com"
						}, null, 8, ["modelValue"])]),
						createBaseVNode("div", _hoisted_6, [createVNode(unref(Label_default), { for: "notes" }, {
							default: withCtx(() => [..._cache[9] || (_cache[9] = [createTextVNode("Notes", -1)])]),
							_: 1
						}), withDirectives(createBaseVNode("textarea", {
							id: "notes",
							"onUpdate:modelValue": _cache[2] || (_cache[2] = ($event) => unref(form).notes = $event),
							rows: "3",
							class: "flex w-full rounded-md border border-input/60 bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:ring-1 focus-visible:ring-ring focus-visible:outline-none",
							placeholder: "Some notes..."
						}, null, 512), [[vModelText, unref(form).notes]])]),
						createBaseVNode("div", _hoisted_7, [createVNode(unref(link_default), {
							href: "/contacts",
							class: "inline-flex items-center rounded-md bg-primary px-3 py-1.5 text-sm font-medium text-primary-foreground hover:bg-primary/90"
						}, {
							default: withCtx(() => [..._cache[10] || (_cache[10] = [createTextVNode(" Navigate Away ", -1)])]),
							_: 1
						}), createVNode(unref(Button_default), {
							variant: "outline",
							size: "sm",
							onClick: _cache[3] || (_cache[3] = ($event) => {
								unref(form).name = "";
								unref(form).email = "";
								unref(form).notes = "";
							})
						}, {
							default: withCtx(() => [..._cache[11] || (_cache[11] = [createTextVNode(" Clear Fields ", -1)])]),
							_: 1
						})]),
						_cache[12] || (_cache[12] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Fill fields, click \"Navigate Away\", then press browser Back. Fields will be restored. ", -1))
					])]),
					_: 1
				}), createVNode(FeatureCard_default, {
					"info-card": "",
					title: "Remembered Counter"
				}, {
					description: withCtx(() => [..._cache[13] || (_cache[13] = [
						createTextVNode(" A separate ", -1),
						createBaseVNode("code", { class: "text-xs" }, "useRemember()", -1),
						createTextVNode(" instance with its own key. ", -1)
					])]),
					default: withCtx(() => [createBaseVNode("div", _hoisted_8, [
						createBaseVNode("div", _hoisted_9, [
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[4] || (_cache[4] = ($event) => unref(counter).count--)
							}, {
								default: withCtx(() => [..._cache[14] || (_cache[14] = [createTextVNode("-", -1)])]),
								_: 1
							}),
							createVNode(unref(Badge_default), {
								variant: "secondary",
								class: "px-4 py-1 text-lg"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(counter).count), 1)]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[5] || (_cache[5] = ($event) => unref(counter).count++)
							}, {
								default: withCtx(() => [..._cache[15] || (_cache[15] = [createTextVNode("+", -1)])]),
								_: 1
							})
						]),
						createVNode(unref(link_default), {
							href: "/contacts",
							class: "inline-flex items-center rounded-md bg-primary px-3 py-1.5 text-sm font-medium text-primary-foreground hover:bg-primary/90"
						}, {
							default: withCtx(() => [..._cache[16] || (_cache[16] = [createTextVNode(" Navigate Away ", -1)])]),
							_: 1
						}),
						_cache[17] || (_cache[17] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Increment the counter, click \"Navigate Away\", then press browser Back. The counter value is restored. ", -1)),
						_cache[18] || (_cache[18] = createBaseVNode("div", { class: "rounded-lg border border-black/5 bg-neutral-50/80 p-3 font-mono text-xs dark:border-white/5 dark:bg-neutral-900/80" }, [
							createBaseVNode("p", null, [createBaseVNode("strong", null, "Manual API:")]),
							createBaseVNode("p", { class: "mt-1" }, [createBaseVNode("code", null, "router.remember(data, 'key')"), createTextVNode(". Save state ")]),
							createBaseVNode("p", null, [createBaseVNode("code", null, "router.restore('key')"), createTextVNode(". Restore state ")])
						], -1))
					])]),
					_: 1
				})])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { Remember_default as default };

import { $t as openBlock, G as Fragment, Mn as withCtx, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, rr as ref } from "./dist.js";
import { t as Label_default } from "./Label.js";
import { t as PersistentDemoLayout_default } from "./PersistentDemoLayout.js";
import { t as Input_default } from "./Input.js";
import { n as FeatureCard_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Layouts/PersistentLayoutsPageTwo.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_2 = { class: "space-y-4" };
var _hoisted_3 = { class: "space-y-2" };
var _hoisted_4 = { class: "space-y-2" };
//#endregion
//#region resources/js/pages/Features/Layouts/PersistentLayoutsPageTwo.vue
var PersistentLayoutsPageTwo_default = /* @__PURE__ */ defineComponent({
	layout: PersistentDemoLayout_default,
	__name: "PersistentLayoutsPageTwo",
	setup(__props) {
		const name = ref("");
		const email = ref("");
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Persistent Layouts - Page 2" }), createBaseVNode("div", _hoisted_1, [createVNode(FeatureCard_default, {
				title: "Page 2 State",
				description: "A different page component with its own state. This also resets on navigation."
			}, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_2, [
					createBaseVNode("div", _hoisted_3, [createVNode(unref(Label_default), { for: "name" }, {
						default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode("Name", -1)])]),
						_: 1
					}), createVNode(unref(Input_default), {
						id: "name",
						modelValue: name.value,
						"onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => name.value = $event),
						placeholder: "Type something..."
					}, null, 8, ["modelValue"])]),
					createBaseVNode("div", _hoisted_4, [createVNode(unref(Label_default), { for: "email" }, {
						default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode("Email", -1)])]),
						_: 1
					}), createVNode(unref(Input_default), {
						id: "email",
						modelValue: email.value,
						"onUpdate:modelValue": _cache[1] || (_cache[1] = ($event) => email.value = $event),
						placeholder: "Type something..."
					}, null, 8, ["modelValue"])]),
					_cache[4] || (_cache[4] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Type in these fields, switch to Page 1, then come back. The inputs clear because this page component remounts. The layout state above stays intact. ", -1))
				])]),
				_: 1
			}), createVNode(FeatureCard_default, {
				"info-card": "",
				title: "What Persists",
				description: "Everything in the layout component survives navigation between pages that share it."
			}, {
				default: withCtx(() => [..._cache[5] || (_cache[5] = [createBaseVNode("div", { class: "space-y-3" }, [createBaseVNode("div", { class: "space-y-2 text-xs text-muted-foreground" }, [
					createBaseVNode("p", null, [createBaseVNode("span", { class: "font-medium text-foreground" }, "Refs and reactive state"), createTextVNode(" keep their values (stopwatch, counter). ")]),
					createBaseVNode("p", null, [createBaseVNode("span", { class: "font-medium text-foreground" }, "Intervals and timers"), createTextVNode(" continue running without interruption. ")]),
					createBaseVNode("p", null, [createBaseVNode("span", { class: "font-medium text-foreground" }, "Event listeners"), createTextVNode(" registered in the layout stay attached. ")]),
					createBaseVNode("p", null, [createBaseVNode("span", { class: "font-medium text-foreground" }, "onMounted"), createTextVNode(" only fires once, not on every navigation. ")])
				]), createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Click \"Leave layout\" to navigate away. When you return, the layout remounts and all state resets. ")], -1)])]),
				_: 1
			})])], 64);
		};
	}
});
//#endregion
export { PersistentLayoutsPageTwo_default as default };

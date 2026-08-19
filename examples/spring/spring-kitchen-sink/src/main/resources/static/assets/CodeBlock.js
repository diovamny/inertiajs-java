import { t as createLucideIcon } from "./createLucideIcon.js";
import { $t as openBlock, Sn as useSlots, at as createElementBlock, gr as normalizeClass, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, on as renderSlot, pr as unref, rr as ref, rt as createBlock, tt as computed, yr as toDisplayString } from "./dist.js";
import { t as Check } from "./check.js";
//#region node_modules/lucide-vue-next/dist/esm/icons/copy.js
/**
* @license lucide-vue-next v0.468.0 - ISC
*
* This source code is licensed under the ISC license.
* See the LICENSE file in the root directory of this source tree.
*/
var Copy = createLucideIcon("CopyIcon", [["rect", {
	width: "14",
	height: "14",
	x: "8",
	y: "8",
	rx: "2",
	ry: "2",
	key: "17jyea"
}], ["path", {
	d: "M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2",
	key: "zix9uf"
}]]);
//#endregion
//#region resources/js/components/CodeBlock.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "group relative rounded-lg border border-black/10 bg-neutral-50 p-3 font-mono text-xs dark:border-white/10 dark:bg-neutral-900/80" };
var _hoisted_2 = {
	key: 0,
	class: "font-semibold"
};
//#endregion
//#region resources/js/components/CodeBlock.vue
var CodeBlock_default = /* @__PURE__ */ defineComponent({
	__name: "CodeBlock",
	props: {
		code: {},
		title: {}
	},
	setup(__props) {
		const props = __props;
		const slots = useSlots();
		const slotRef = ref(null);
		const slotCode = computed(() => slotRef.value?.textContent ?? "");
		const copied = ref(false);
		function dedent(raw) {
			const lines = raw.replace(/^\n/, "").replace(/\s+$/, "").split("\n");
			const indent = Math.min(...lines.filter((l) => l.trim()).map((l) => l.match(/^(\s*)/)?.[1].length ?? 0));
			return lines.map((l) => l.slice(indent)).join("\n");
		}
		function copy() {
			navigator.clipboard.writeText(dedent(props.code ?? slotCode.value));
			copied.value = true;
			setTimeout(() => copied.value = false, 2e3);
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock("div", _hoisted_1, [
				createBaseVNode("button", {
					type: "button",
					class: "absolute top-2 right-2 rounded p-1 text-muted-foreground opacity-0 transition-opacity group-hover:opacity-100 hover:text-foreground",
					onClick: copy
				}, [copied.value ? (openBlock(), createBlock(unref(Check), {
					key: 0,
					class: "size-3.5"
				})) : (openBlock(), createBlock(unref(Copy), {
					key: 1,
					class: "size-3.5"
				}))]),
				__props.title ? (openBlock(), createElementBlock("p", _hoisted_2, toDisplayString(__props.title), 1)) : createCommentVNode("", true),
				createBaseVNode("pre", { class: normalizeClass({ "mt-1": __props.title }) }, toDisplayString(dedent(__props.code ?? slotCode.value)), 3),
				unref(slots).default ? (openBlock(), createElementBlock("span", {
					key: 1,
					ref_key: "slotRef",
					ref: slotRef,
					hidden: ""
				}, [renderSlot(_ctx.$slots, "default")], 512)) : createCommentVNode("", true)
			]);
		};
	}
});
//#endregion
export { CodeBlock_default as t };

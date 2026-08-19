import { s as Button_default, t as createLucideIcon } from "./createLucideIcon.js";
import { $t as openBlock, Mn as withCtx, Zt as onUnmounted, dt as createTextVNode, f as usePage, ft as createVNode, gr as normalizeClass, mt as defineComponent, nt as createBaseVNode, o as link_default, on as renderSlot, pr as unref, qt as onMounted, rr as ref, rt as createBlock, tt as computed, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { t as FeatureHeader_default } from "./FeatureHeader.js";
//#region node_modules/lucide-vue-next/dist/esm/icons/hash.js
/**
* @license lucide-vue-next v0.468.0 - ISC
*
* This source code is licensed under the ISC license.
* See the LICENSE file in the root directory of this source tree.
*/
var Hash = createLucideIcon("HashIcon", [
	["line", {
		x1: "4",
		x2: "20",
		y1: "9",
		y2: "9",
		key: "4lhtct"
	}],
	["line", {
		x1: "4",
		x2: "20",
		y1: "15",
		y2: "15",
		key: "vyu0kd"
	}],
	["line", {
		x1: "10",
		x2: "8",
		y1: "3",
		y2: "21",
		key: "1ggp8o"
	}],
	["line", {
		x1: "16",
		x2: "14",
		y1: "3",
		y2: "21",
		key: "weycgp"
	}]
]);
//#endregion
//#region node_modules/lucide-vue-next/dist/esm/icons/timer.js
/**
* @license lucide-vue-next v0.468.0 - ISC
*
* This source code is licensed under the ISC license.
* See the LICENSE file in the root directory of this source tree.
*/
var Timer = createLucideIcon("TimerIcon", [
	["line", {
		x1: "10",
		x2: "14",
		y1: "2",
		y2: "2",
		key: "14vaq8"
	}],
	["line", {
		x1: "12",
		x2: "15",
		y1: "14",
		y2: "11",
		key: "17fdiu"
	}],
	["circle", {
		cx: "12",
		cy: "14",
		r: "8",
		key: "1e1u0o"
	}]
]);
//#endregion
//#region resources/js/layouts/PersistentDemoLayout.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "flex flex-col gap-4 rounded-xl border border-black/10 bg-card p-4 shadow-sm sm:flex-row sm:items-center sm:justify-between dark:border-white/10" };
var _hoisted_3 = { class: "flex items-center gap-6" };
var _hoisted_4 = { class: "flex items-center gap-2" };
var _hoisted_5 = { class: "flex items-center gap-2" };
var _hoisted_6 = { class: "flex items-center gap-2" };
//#endregion
//#region resources/js/layouts/PersistentDemoLayout.vue
var PersistentDemoLayout_default = /* @__PURE__ */ defineComponent({
	__name: "PersistentDemoLayout",
	setup(__props) {
		const breadcrumbs = [{ title: "Layouts & Head" }, { title: "Persistent Layouts" }];
		const elapsed = ref(0);
		const counter = ref(0);
		let interval;
		onMounted(() => {
			interval = setInterval(() => elapsed.value++, 1e3);
		});
		onUnmounted(() => {
			clearInterval(interval);
		});
		const formattedTime = computed(() => {
			return `${Math.floor(elapsed.value / 60)}:${(elapsed.value % 60).toString().padStart(2, "0")}`;
		});
		const page = usePage();
		const currentPath = computed(() => page.url);
		return (_ctx, _cache) => {
			return openBlock(), createBlock(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [
					createVNode(FeatureHeader_default, {
						title: "Persistent Layouts",
						docs: "the-basics/layouts#persistent-layouts",
						controller: "app/Http/Controllers/Feature/LayoutController.php#L11"
					}, {
						default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode(" Layout state survives page navigations. The stopwatch and counter below live in the layout and persist as you switch pages. ", -1)])]),
						_: 1
					}),
					createBaseVNode("div", _hoisted_2, [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [
						createVNode(unref(Timer), { class: "size-4 text-muted-foreground" }),
						_cache[3] || (_cache[3] = createBaseVNode("span", { class: "text-sm font-medium" }, "Stopwatch", -1)),
						createVNode(unref(Badge_default), {
							variant: "secondary",
							class: "font-mono text-base"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(formattedTime.value), 1)]),
							_: 1
						})
					]), createBaseVNode("div", _hoisted_5, [
						createVNode(unref(Hash), { class: "size-4 text-muted-foreground" }),
						_cache[6] || (_cache[6] = createBaseVNode("span", { class: "text-sm font-medium" }, "Counter", -1)),
						createVNode(unref(Button_default), {
							variant: "outline",
							size: "sm",
							class: "size-7 p-0",
							onClick: _cache[0] || (_cache[0] = ($event) => counter.value--)
						}, {
							default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode(" - ", -1)])]),
							_: 1
						}),
						createVNode(unref(Badge_default), {
							variant: "secondary",
							class: "font-mono text-base"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(counter.value), 1)]),
							_: 1
						}),
						createVNode(unref(Button_default), {
							variant: "outline",
							size: "sm",
							class: "size-7 p-0",
							onClick: _cache[1] || (_cache[1] = ($event) => counter.value++)
						}, {
							default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" + ", -1)])]),
							_: 1
						})
					])]), createBaseVNode("div", _hoisted_6, [
						createVNode(unref(link_default), {
							href: "/features/layouts/persistent-layouts",
							class: normalizeClass(["inline-flex items-center rounded-md px-3 py-1.5 text-sm font-medium transition-colors", currentPath.value === "/features/layouts/persistent-layouts" ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground hover:bg-accent hover:text-foreground"])
						}, {
							default: withCtx(() => [..._cache[7] || (_cache[7] = [createTextVNode(" Page 1 ", -1)])]),
							_: 1
						}, 8, ["class"]),
						createVNode(unref(link_default), {
							href: "/features/layouts/persistent-layouts/page-2",
							class: normalizeClass(["inline-flex items-center rounded-md px-3 py-1.5 text-sm font-medium transition-colors", currentPath.value === "/features/layouts/persistent-layouts/page-2" ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground hover:bg-accent hover:text-foreground"])
						}, {
							default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode(" Page 2 ", -1)])]),
							_: 1
						}, 8, ["class"]),
						createVNode(unref(link_default), {
							href: "/contacts",
							class: "inline-flex items-center rounded-md px-3 py-1.5 text-sm text-muted-foreground hover:text-foreground"
						}, {
							default: withCtx(() => [..._cache[9] || (_cache[9] = [createTextVNode(" Leave layout → ", -1)])]),
							_: 1
						})
					])]),
					renderSlot(_ctx.$slots, "default")
				])]),
				_: 3
			});
		};
	}
});
//#endregion
export { PersistentDemoLayout_default as t };

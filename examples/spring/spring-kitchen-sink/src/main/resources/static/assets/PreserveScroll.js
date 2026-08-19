import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Navigation/PreserveScroll.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "flex flex-wrap gap-3" };
var _hoisted_3 = { class: "space-y-1" };
var _hoisted_4 = { class: "space-y-1" };
var _hoisted_5 = { class: "space-y-1" };
var _hoisted_6 = { class: "mt-3 flex items-center gap-2" };
var _hoisted_7 = {
	class: "h-64 overflow-y-auto rounded-md border border-black/10 dark:border-white/10",
	"scroll-region": ""
};
var _hoisted_8 = { class: "space-y-2 p-3" };
var _hoisted_9 = { class: "flex items-center justify-between" };
var _hoisted_10 = { class: "text-sm font-medium" };
var _hoisted_11 = { class: "mt-1 text-xs text-muted-foreground" };
var _hoisted_12 = { class: "space-y-2" };
var _hoisted_13 = { class: "flex items-center justify-between" };
var _hoisted_14 = { class: "font-medium" };
var _hoisted_15 = { class: "mt-1 text-sm text-muted-foreground" };
//#endregion
//#region resources/js/pages/Features/Navigation/PreserveScroll.vue
var PreserveScroll_default = /* @__PURE__ */ defineComponent({
	__name: "PreserveScroll",
	props: { timestamp: {} },
	setup(__props) {
		const breadcrumbs = [{ title: "Navigation" }, { title: "Preserve Scroll" }];
		const items = Array.from({ length: 30 }, (_, i) => ({
			id: i + 1,
			title: `Item ${i + 1}`,
			description: `This is item number ${i + 1}. Scroll down to see more items and test scroll preservation.`
		}));
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Preserve Scroll" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [
					createVNode(FeatureHeader_default, {
						title: "Preserve Scroll",
						docs: "advanced/scroll-management#scroll-preservation",
						controller: "app/Http/Controllers/Feature/NavigationController.php#L33"
					}, {
						default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" Scroll position management. Scroll down, then click a reload button to see the difference. ", -1)])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "Reload Options",
						description: "Scroll down the list below, then click a button to reload. Compare scroll behavior."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_2, [
							createBaseVNode("div", _hoisted_3, [createVNode(unref(Button_default), {
								variant: "destructive",
								onClick: _cache[0] || (_cache[0] = ($event) => unref(router).visit("/features/navigation/preserve-scroll"))
							}, {
								default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode(" Default (scroll to top) ", -1)])]),
								_: 1
							}), _cache[5] || (_cache[5] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Page scrolls back to top. ", -1))]),
							createBaseVNode("div", _hoisted_4, [createVNode(unref(Button_default), { onClick: _cache[1] || (_cache[1] = ($event) => unref(router).visit("/features/navigation/preserve-scroll", { preserveScroll: true })) }, {
								default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" preserveScroll: true ", -1)])]),
								_: 1
							}), _cache[7] || (_cache[7] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Scroll position maintained. ", -1))]),
							createBaseVNode("div", _hoisted_5, [createVNode(unref(Button_default), {
								variant: "secondary",
								onClick: _cache[2] || (_cache[2] = ($event) => unref(router).reload())
							}, {
								default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode(" router.reload() ", -1)])]),
								_: 1
							}), _cache[9] || (_cache[9] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Reload preserves scroll by default. ", -1))])
						]), createBaseVNode("div", _hoisted_6, [_cache[10] || (_cache[10] = createBaseVNode("span", { class: "text-sm text-muted-foreground" }, "Server timestamp:", -1)), createVNode(unref(Badge_default), { variant: "outline" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.timestamp), 1)]),
							_: 1
						})])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "Scroll Region" }, {
						description: withCtx(() => [..._cache[11] || (_cache[11] = [
							createTextVNode(" This container has ", -1),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "scroll-region", -1),
							createTextVNode(". Inertia tracks its scroll position separately. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_7, [createBaseVNode("div", _hoisted_8, [(openBlock(true), createElementBlock(Fragment, null, renderList(unref(items), (item) => {
							return openBlock(), createElementBlock("div", {
								key: item.id,
								class: "rounded-md border border-black/10 p-3 dark:border-white/10"
							}, [createBaseVNode("div", _hoisted_9, [createBaseVNode("span", _hoisted_10, toDisplayString(item.title), 1), createVNode(unref(Badge_default), {
								variant: "secondary",
								class: "text-xs"
							}, {
								default: withCtx(() => [createTextVNode("#" + toDisplayString(item.id), 1)]),
								_: 2
							}, 1024)]), createBaseVNode("p", _hoisted_11, toDisplayString(item.description), 1)]);
						}), 128))])])]),
						_: 1
					}),
					createBaseVNode("div", _hoisted_12, [(openBlock(true), createElementBlock(Fragment, null, renderList(unref(items), (item) => {
						return openBlock(), createElementBlock("div", {
							key: "page-" + item.id,
							class: "rounded-xl bg-muted/30 p-4"
						}, [createBaseVNode("div", _hoisted_13, [createBaseVNode("span", _hoisted_14, toDisplayString(item.title), 1), createVNode(unref(Badge_default), { variant: "outline" }, {
							default: withCtx(() => [createTextVNode("#" + toDisplayString(item.id), 1)]),
							_: 2
						}, 1024)]), createBaseVNode("p", _hoisted_15, toDisplayString(item.description), 1)]);
					}), 128))])
				])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { PreserveScroll_default as default };

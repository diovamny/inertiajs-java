import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Navigation/ScrollManagement.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "space-y-2" };
var _hoisted_5 = { class: "flex items-center gap-2" };
var _hoisted_6 = { class: "space-y-2" };
var _hoisted_7 = { class: "flex items-center gap-2" };
var _hoisted_8 = { class: "space-y-2" };
var _hoisted_9 = { class: "space-y-3" };
var _hoisted_10 = {
	"scroll-region": "",
	class: "h-48 space-y-2 overflow-y-auto rounded-lg border border-black/10 p-3 dark:border-white/10"
};
var _hoisted_11 = { class: "text-sm font-medium" };
var _hoisted_12 = { class: "text-xs text-muted-foreground" };
//#endregion
//#region resources/js/pages/Features/Navigation/ScrollManagement.vue
var ScrollManagement_default = /* @__PURE__ */ defineComponent({
	__name: "ScrollManagement",
	props: {
		timestamp: {},
		items: {}
	},
	setup(__props) {
		const breadcrumbs = [{ title: "Navigation" }, { title: "Scroll Management" }];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Scroll Management" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Scroll Management",
					docs: "advanced/scroll-management",
					controller: "app/Http/Controllers/Feature/NavigationController.php#L135"
				}, {
					default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" Inertia resets scroll position to the top on every page visit and restores it during back/forward navigation. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [createVNode(FeatureCard_default, {
					title: "Scroll Behavior",
					description: "By default, scroll resets to the top on navigation. Use preserveScroll to keep the current position."
				}, {
					default: withCtx(() => [createBaseVNode("div", _hoisted_3, [
						createBaseVNode("div", _hoisted_4, [_cache[6] || (_cache[6] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " Default (resets scroll) ", -1)), createBaseVNode("div", _hoisted_5, [createVNode(unref(Button_default), {
							variant: "outline",
							size: "sm",
							onClick: _cache[0] || (_cache[0] = ($event) => unref(router).visit("/features/navigation/scroll-management"))
						}, {
							default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode(" router.visit() ", -1)])]),
							_: 1
						}), createVNode(unref(link_default), {
							href: "/features/navigation/scroll-management",
							class: "text-sm text-primary underline underline-offset-2"
						}, {
							default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" <Link> ", -1)])]),
							_: 1
						})])]),
						createBaseVNode("div", _hoisted_6, [_cache[9] || (_cache[9] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " preserveScroll: true ", -1)), createBaseVNode("div", _hoisted_7, [createVNode(unref(Button_default), {
							variant: "outline",
							size: "sm",
							onClick: _cache[1] || (_cache[1] = ($event) => unref(router).visit("/features/navigation/scroll-management", { preserveScroll: true }))
						}, {
							default: withCtx(() => [..._cache[7] || (_cache[7] = [createTextVNode(" router.visit() ", -1)])]),
							_: 1
						}), createVNode(unref(link_default), {
							href: "/features/navigation/scroll-management",
							"preserve-scroll": "",
							class: "text-sm text-primary underline underline-offset-2"
						}, {
							default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode(" <Link preserve-scroll> ", -1)])]),
							_: 1
						})])]),
						createBaseVNode("div", _hoisted_8, [
							_cache[11] || (_cache[11] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " preserveScroll: callback ", -1)),
							_cache[12] || (_cache[12] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Dynamically decide based on the response. ", -1)),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[2] || (_cache[2] = ($event) => unref(router).visit("/features/navigation/scroll-management", { preserveScroll: (page) => page.props.timestamp !== null }))
							}, {
								default: withCtx(() => [..._cache[10] || (_cache[10] = [createTextVNode(" preserveScroll: callback ", -1)])]),
								_: 1
							})
						])
					])]),
					_: 1
				}), createVNode(FeatureCard_default, {
					title: "Scroll Regions",
					description: "Track scroll position for elements with overflow, not just the document."
				}, {
					default: withCtx(() => [createBaseVNode("div", _hoisted_9, [
						_cache[13] || (_cache[13] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
							createTextVNode(" Add the "),
							createBaseVNode("code", null, "scroll-region"),
							createTextVNode(" attribute to any scrollable container. Inertia will track and restore its scroll position during back/forward navigation. ")
						], -1)),
						createBaseVNode("div", _hoisted_10, [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.items, (item) => {
							return openBlock(), createElementBlock("div", {
								key: item.id,
								class: "flex items-center justify-between rounded-lg bg-muted/30 px-3 py-2"
							}, [createBaseVNode("div", null, [createBaseVNode("span", _hoisted_11, toDisplayString(item.title), 1), createBaseVNode("p", _hoisted_12, toDisplayString(item.description), 1)]), createVNode(unref(Badge_default), {
								variant: "secondary",
								class: "text-xs"
							}, {
								default: withCtx(() => [createTextVNode(" #" + toDisplayString(item.id), 1)]),
								_: 2
							}, 1024)]);
						}), 128))]),
						_cache[14] || (_cache[14] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Scroll down in the list above, navigate away, then use the browser back button. The scroll position will be restored. ", -1))
					])]),
					_: 1
				})])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { ScrollManagement_default as default };

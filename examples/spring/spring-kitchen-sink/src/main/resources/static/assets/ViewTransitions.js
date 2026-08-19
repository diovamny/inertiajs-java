import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Navigation/ViewTransitions.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-2" };
var _hoisted_4 = { class: "space-y-2" };
var _hoisted_5 = { class: "flex flex-wrap gap-2" };
//#endregion
//#region resources/js/pages/Features/Navigation/ViewTransitions.vue
var ViewTransitions_default = /* @__PURE__ */ defineComponent({
	__name: "ViewTransitions",
	setup(__props) {
		const breadcrumbs = [{ title: "Navigation" }, { title: "View Transitions" }];
		const pages = [
			{
				title: "Dashboard",
				href: "/dashboard"
			},
			{
				title: "Contacts",
				href: "/contacts"
			},
			{
				title: "Organizations",
				href: "/organizations"
			},
			{
				title: "This Page",
				href: "/features/navigation/view-transitions"
			}
		];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "View Transitions" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "View Transitions",
					docs: "the-basics/view-transitions",
					controller: "app/Http/Controllers/Feature/NavigationController.php#L40"
				}, {
					default: withCtx(() => [..._cache[0] || (_cache[0] = [createTextVNode(" Use the browser's native View Transitions API for smooth page navigation animations. Falls back to standard transitions in unsupported browsers. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, { title: "With View Transitions" }, {
						description: withCtx(() => [..._cache[1] || (_cache[1] = [
							createTextVNode(" These links use ", -1),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "view-transition", -1),
							createTextVNode(" for a smooth cross-fade animation. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [(openBlock(), createElementBlock(Fragment, null, renderList(pages, (page) => {
							return createVNode(unref(link_default), {
								key: page.href,
								href: page.href,
								"view-transition": "",
								class: "flex items-center rounded-md border border-black/10 bg-background px-4 py-2 text-sm transition-colors hover:bg-accent dark:border-white/10"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(page.title), 1)]),
								_: 2
							}, 1032, ["href"]);
						}), 64))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "Without View Transitions",
						description: "Standard navigation. No cross-fade animation."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_4, [(openBlock(), createElementBlock(Fragment, null, renderList(pages, (page) => {
							return createVNode(unref(link_default), {
								key: page.href,
								href: page.href,
								class: "flex items-center rounded-md border border-black/10 bg-background px-4 py-2 text-sm transition-colors hover:bg-accent dark:border-white/10"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(page.title), 1)]),
								_: 2
							}, 1032, ["href"]);
						}), 64))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "Programmatic Visits"
					}, {
						description: withCtx(() => [..._cache[2] || (_cache[2] = [
							createTextVNode(" Use ", -1),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "router.visit(url, {'{'} viewTransition: true {'}'})", -1),
							createTextVNode(" for programmatic navigation with transitions. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_5, [(openBlock(), createElementBlock(Fragment, null, renderList(pages, (page) => {
							return createVNode(unref(Button_default), {
								key: page.href,
								variant: "outline",
								onClick: ($event) => unref(router).visit(page.href, { viewTransition: true })
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(page.title), 1)]),
								_: 2
							}, 1032, ["onClick"]);
						}), 64))])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { ViewTransitions_default as default };

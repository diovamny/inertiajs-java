import { $t as openBlock, G as Fragment, Mn as withCtx, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, h as whenVisible_default, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/DataLoading/WhenVisible.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "space-y-2" };
var _hoisted_3 = { class: "space-y-2" };
var _hoisted_4 = { class: "flex flex-wrap gap-3" };
var _hoisted_5 = { class: "space-y-2" };
var _hoisted_6 = {
	key: 0,
	class: "space-y-2"
};
var _hoisted_7 = {
	key: 1,
	class: "text-sm text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Features/DataLoading/WhenVisible.vue
var WhenVisible_default = /* @__PURE__ */ defineComponent({
	__name: "WhenVisible",
	props: {
		section1: {},
		section2: {},
		section3: {}
	},
	setup(__props) {
		const breadcrumbs = [{ title: "Data Loading" }, { title: "When Visible" }];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "When Visible" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [
					createVNode(FeatureHeader_default, {
						title: "When Visible",
						docs: "data-props/load-when-visible",
						controller: "app/Http/Controllers/Feature/DataLoadingController.php#L70"
					}, {
						default: withCtx(() => [..._cache[0] || (_cache[0] = [
							createTextVNode(" Viewport-triggered data loading using ", -1),
							createBaseVNode("code", { class: "text-xs" }, "Inertia::optional()", -1),
							createTextVNode(" + ", -1),
							createBaseVNode("code", { class: "text-xs" }, "<WhenVisible>", -1),
							createTextVNode(". ", -1)
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "How It Works" }, {
						description: withCtx(() => [..._cache[1] || (_cache[1] = [
							createTextVNode(" Each section below uses ", -1),
							createBaseVNode("code", { class: "text-xs" }, "Inertia::optional()", -1),
							createTextVNode(" on the server and ", -1),
							createBaseVNode("code", { class: "text-xs" }, "<WhenVisible>", -1),
							createTextVNode(" on the client. Data is only fetched when the component scrolls into the viewport. ", -1)
						])]),
						default: withCtx(() => [_cache[2] || (_cache[2] = createBaseVNode("p", { class: "text-sm text-muted-foreground" }, " Scroll down to trigger each section. Each has a simulated server delay. ", -1))]),
						_: 1
					}),
					_cache[6] || (_cache[6] = createBaseVNode("div", { class: "flex h-[75vh] items-center justify-center rounded-lg bg-gradient-to-b from-transparent to-muted/50" }, [createBaseVNode("p", { class: "animate-bounce text-sm text-muted-foreground" }, " ↓ Scroll down to trigger lazy loading ")], -1)),
					createVNode(FeatureCard_default, { title: "Recent Contacts" }, {
						default: withCtx(() => [createVNode(unref(whenVisible_default), { data: "section1" }, {
							fallback: withCtx(() => [createBaseVNode("div", _hoisted_2, [(openBlock(), createElementBlock(Fragment, null, renderList(3, (i) => {
								return createBaseVNode("div", {
									key: i,
									class: "h-8 animate-pulse rounded bg-muted"
								});
							}), 64))])]),
							default: withCtx(() => [createBaseVNode("div", _hoisted_3, [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.section1, (contact) => {
								return openBlock(), createElementBlock("div", {
									key: contact.id,
									class: "flex items-center justify-between rounded bg-muted/50 px-3 py-2 text-sm"
								}, [createBaseVNode("span", null, toDisplayString(contact.name), 1), createVNode(unref(Badge_default), {
									variant: "outline",
									class: "text-xs"
								}, {
									default: withCtx(() => [createTextVNode("#" + toDisplayString(contact.id), 1)]),
									_: 2
								}, 1024)]);
							}), 128))])]),
							_: 1
						})]),
						_: 1
					}),
					_cache[7] || (_cache[7] = createBaseVNode("div", { class: "flex h-[75vh] items-center justify-center rounded-lg bg-gradient-to-b from-transparent to-muted/50" }, [createBaseVNode("p", { class: "animate-bounce text-sm text-muted-foreground" }, " ↓ Keep scrolling ")], -1)),
					createVNode(FeatureCard_default, { title: "Statistics" }, {
						description: withCtx(() => [..._cache[3] || (_cache[3] = [
							createTextVNode(" Loaded with ", -1),
							createBaseVNode("code", { class: "text-xs" }, ":buffer=\"200\"", -1),
							createTextVNode(". Starts loading 200px before visible. ", -1)
						])]),
						default: withCtx(() => [createVNode(unref(whenVisible_default), {
							data: "section2",
							buffer: 200
						}, {
							fallback: withCtx(() => [..._cache[4] || (_cache[4] = [createBaseVNode("div", { class: "flex gap-4" }, [createBaseVNode("div", { class: "h-8 w-32 animate-pulse rounded bg-muted" }), createBaseVNode("div", { class: "h-8 w-48 animate-pulse rounded bg-muted" })], -1)])]),
							default: withCtx(() => [createBaseVNode("div", _hoisted_4, [createVNode(unref(Badge_default), { variant: "secondary" }, {
								default: withCtx(() => [createTextVNode("Total Contacts: " + toDisplayString(__props.section2?.totalContacts), 1)]),
								_: 1
							}), createVNode(unref(Badge_default), { variant: "secondary" }, {
								default: withCtx(() => [createTextVNode("Generated: " + toDisplayString(__props.section2?.generated), 1)]),
								_: 1
							})])]),
							_: 1
						})]),
						_: 1
					}),
					_cache[8] || (_cache[8] = createBaseVNode("div", { class: "flex h-[75vh] items-center justify-center rounded-lg bg-gradient-to-b from-transparent to-muted/50" }, [createBaseVNode("p", { class: "animate-bounce text-sm text-muted-foreground" }, " ↓ Almost there ")], -1)),
					createVNode(FeatureCard_default, { title: "Favorite Contacts" }, {
						default: withCtx(() => [createVNode(unref(whenVisible_default), { data: "section3" }, {
							fallback: withCtx(() => [createBaseVNode("div", _hoisted_5, [(openBlock(), createElementBlock(Fragment, null, renderList(5, (i) => {
								return createBaseVNode("div", {
									key: i,
									class: "h-8 animate-pulse rounded bg-muted"
								});
							}), 64))])]),
							default: withCtx(() => [__props.section3?.length ? (openBlock(), createElementBlock("div", _hoisted_6, [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.section3, (contact) => {
								return openBlock(), createElementBlock("div", {
									key: contact.id,
									class: "flex items-center justify-between rounded bg-muted/50 px-3 py-2 text-sm"
								}, [createBaseVNode("span", null, toDisplayString(contact.name), 1), _cache[5] || (_cache[5] = createBaseVNode("span", { class: "text-yellow-500" }, "★", -1))]);
							}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_7, " No favorite contacts found. "))]),
							_: 1
						})]),
						_: 1
					})
				])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { WhenVisible_default as default };

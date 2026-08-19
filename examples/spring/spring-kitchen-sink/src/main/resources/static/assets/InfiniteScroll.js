import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, M as vModelCheckbox, Mn as withCtx, Pn as withDirectives, Sr as router, a as infiniteScroll_default, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, pr as unref, rr as ref, rt as createBlock, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/DataLoading/InfiniteScroll.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "flex flex-wrap gap-2" };
var _hoisted_3 = { class: "text-sm font-medium" };
var _hoisted_4 = { class: "text-xs text-muted-foreground" };
var _hoisted_5 = { class: "flex items-center gap-2" };
var _hoisted_6 = {
	key: 1,
	class: "text-yellow-500"
};
var _hoisted_7 = { class: "text-sm font-medium" };
var _hoisted_8 = { class: "text-xs text-muted-foreground" };
var _hoisted_9 = { class: "flex items-center gap-2" };
var _hoisted_10 = {
	key: 1,
	class: "text-yellow-500"
};
var _hoisted_11 = {
	key: 0,
	class: "pt-3 text-center"
};
var _hoisted_12 = {
	key: 1,
	class: "pt-3 text-center text-sm text-muted-foreground"
};
var _hoisted_13 = { class: "text-sm font-medium" };
var _hoisted_14 = { class: "text-xs text-muted-foreground" };
var _hoisted_15 = { class: "flex items-center gap-2" };
var _hoisted_16 = {
	key: 1,
	class: "text-yellow-500"
};
var _hoisted_17 = {
	key: 0,
	class: "pt-3 text-center"
};
var _hoisted_18 = { class: "mb-4 flex items-center gap-2 rounded-md bg-muted p-3" };
var _hoisted_19 = { class: "text-sm font-medium" };
var _hoisted_20 = { class: "text-xs text-muted-foreground" };
var _hoisted_21 = { class: "flex items-center gap-2" };
var _hoisted_22 = {
	key: 1,
	class: "text-yellow-500"
};
var _hoisted_23 = { class: "mt-4 space-y-2" };
//#endregion
//#region resources/js/pages/Features/DataLoading/InfiniteScroll.vue
var InfiniteScroll_default = /* @__PURE__ */ defineComponent({
	__name: "InfiniteScroll",
	props: { contacts: {} },
	setup(__props) {
		const breadcrumbs = [{ title: "Data Loading" }, { title: "Infinite Scroll" }];
		const mode = ref("auto");
		const infiniteScrollRef = ref(null);
		const favoritesOnly = ref(false);
		function applyFilter() {
			router.reload({
				data: { favorites: favoritesOnly.value ? "1" : "0" },
				only: ["contacts"],
				reset: ["contacts"]
			});
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Infinite Scroll" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [
					createVNode(FeatureHeader_default, {
						title: "Infinite Scroll",
						docs: "data-props/infinite-scroll",
						controller: "app/Http/Controllers/Feature/DataLoadingController.php#L56"
					}, {
						default: withCtx(() => [..._cache[5] || (_cache[5] = [
							createTextVNode(" Auto, manual, and manual-after modes using ", -1),
							createBaseVNode("code", { class: "text-xs" }, "Inertia::scroll()", -1),
							createTextVNode(" with pagination. ", -1)
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "Scroll Mode",
						description: "Switch between InfiniteScroll modes. The list below updates accordingly."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_2, [
							createVNode(unref(Button_default), {
								variant: mode.value === "auto" ? "default" : "outline",
								size: "sm",
								onClick: _cache[0] || (_cache[0] = ($event) => mode.value = "auto")
							}, {
								default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" Auto (default) ", -1)])]),
								_: 1
							}, 8, ["variant"]),
							createVNode(unref(Button_default), {
								variant: mode.value === "manual" ? "default" : "outline",
								size: "sm",
								onClick: _cache[1] || (_cache[1] = ($event) => mode.value = "manual")
							}, {
								default: withCtx(() => [..._cache[7] || (_cache[7] = [createTextVNode(" Manual ", -1)])]),
								_: 1
							}, 8, ["variant"]),
							createVNode(unref(Button_default), {
								variant: mode.value === "manual-after" ? "default" : "outline",
								size: "sm",
								onClick: _cache[2] || (_cache[2] = ($event) => mode.value = "manual-after")
							}, {
								default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode(" Manual After 2 Pages ", -1)])]),
								_: 1
							}, 8, ["variant"]),
							createVNode(unref(Button_default), {
								variant: mode.value === "reset" ? "default" : "outline",
								size: "sm",
								onClick: _cache[3] || (_cache[3] = ($event) => mode.value = "reset")
							}, {
								default: withCtx(() => [..._cache[9] || (_cache[9] = [createTextVNode(" Reset on Filter ", -1)])]),
								_: 1
							}, 8, ["variant"])
						])]),
						_: 1
					}),
					mode.value === "auto" ? (openBlock(), createBlock(FeatureCard_default, {
						key: 0,
						title: "Auto Mode",
						description: "Loads more items automatically as you scroll near the bottom. The buffer (200px) triggers loading before you reach the end for a smoother experience."
					}, {
						default: withCtx(() => [createVNode(unref(infiniteScroll_default), {
							data: "contacts",
							class: "space-y-2",
							buffer: 200,
							"preserve-url": ""
						}, {
							loading: withCtx(() => [..._cache[10] || (_cache[10] = [createBaseVNode("div", { class: "py-3 text-center text-sm text-muted-foreground" }, " Loading more contacts... ", -1)])]),
							default: withCtx(() => [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.contacts.data, (contact) => {
								return openBlock(), createElementBlock("div", {
									key: contact.id,
									class: "flex items-center justify-between rounded border border-black/10 px-3 py-2 dark:border-white/10"
								}, [createBaseVNode("div", null, [createBaseVNode("p", _hoisted_3, toDisplayString(contact.first_name) + " " + toDisplayString(contact.last_name), 1), createBaseVNode("p", _hoisted_4, toDisplayString(contact.email), 1)]), createBaseVNode("div", _hoisted_5, [contact.organization ? (openBlock(), createBlock(unref(Badge_default), {
									key: 0,
									variant: "outline",
									class: "text-xs"
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(contact.organization.name), 1)]),
									_: 2
								}, 1024)) : createCommentVNode("", true), contact.is_favorite ? (openBlock(), createElementBlock("span", _hoisted_6, "★")) : createCommentVNode("", true)])]);
							}), 128))]),
							_: 1
						})]),
						_: 1
					})) : createCommentVNode("", true),
					mode.value === "manual" ? (openBlock(), createBlock(FeatureCard_default, {
						key: 1,
						title: "Manual Mode",
						description: "Click the button to load more items. No automatic loading."
					}, {
						default: withCtx(() => [createVNode(unref(infiniteScroll_default), {
							ref_key: "infiniteScrollRef",
							ref: infiniteScrollRef,
							data: "contacts",
							class: "space-y-2",
							manual: "",
							"preserve-url": ""
						}, {
							next: withCtx(({ loading, fetch, hasMore }) => [hasMore ? (openBlock(), createElementBlock("div", _hoisted_11, [createVNode(unref(Button_default), {
								variant: "outline",
								disabled: loading,
								onClick: fetch
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(loading ? "Loading..." : "Load More"), 1)]),
								_: 2
							}, 1032, ["disabled", "onClick"])])) : (openBlock(), createElementBlock("p", _hoisted_12, " No more contacts. "))]),
							default: withCtx(() => [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.contacts.data, (contact) => {
								return openBlock(), createElementBlock("div", {
									key: contact.id,
									class: "flex items-center justify-between rounded border border-black/10 px-3 py-2 dark:border-white/10"
								}, [createBaseVNode("div", null, [createBaseVNode("p", _hoisted_7, toDisplayString(contact.first_name) + " " + toDisplayString(contact.last_name), 1), createBaseVNode("p", _hoisted_8, toDisplayString(contact.email), 1)]), createBaseVNode("div", _hoisted_9, [contact.organization ? (openBlock(), createBlock(unref(Badge_default), {
									key: 0,
									variant: "outline",
									class: "text-xs"
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(contact.organization.name), 1)]),
									_: 2
								}, 1024)) : createCommentVNode("", true), contact.is_favorite ? (openBlock(), createElementBlock("span", _hoisted_10, "★")) : createCommentVNode("", true)])]);
							}), 128))]),
							_: 1
						}, 512)]),
						_: 1
					})) : createCommentVNode("", true),
					mode.value === "manual-after" ? (openBlock(), createBlock(FeatureCard_default, {
						key: 2,
						title: "Manual After 2 Pages",
						description: "Auto-loads for the first 2 pages, then switches to a manual button."
					}, {
						default: withCtx(() => [createVNode(unref(infiniteScroll_default), {
							data: "contacts",
							class: "space-y-2",
							"manual-after": 2,
							"preserve-url": ""
						}, {
							loading: withCtx(() => [..._cache[11] || (_cache[11] = [createBaseVNode("div", { class: "py-3 text-center text-sm text-muted-foreground" }, " Loading more contacts... ", -1)])]),
							next: withCtx(({ loading, fetch, hasMore, manualMode }) => [manualMode && hasMore ? (openBlock(), createElementBlock("div", _hoisted_17, [createVNode(unref(Button_default), {
								variant: "outline",
								disabled: loading,
								onClick: fetch
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(loading ? "Loading..." : "Load More"), 1)]),
								_: 2
							}, 1032, ["disabled", "onClick"])])) : createCommentVNode("", true)]),
							default: withCtx(() => [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.contacts.data, (contact) => {
								return openBlock(), createElementBlock("div", {
									key: contact.id,
									class: "flex items-center justify-between rounded border border-black/10 px-3 py-2 dark:border-white/10"
								}, [createBaseVNode("div", null, [createBaseVNode("p", _hoisted_13, toDisplayString(contact.first_name) + " " + toDisplayString(contact.last_name), 1), createBaseVNode("p", _hoisted_14, toDisplayString(contact.email), 1)]), createBaseVNode("div", _hoisted_15, [contact.organization ? (openBlock(), createBlock(unref(Badge_default), {
									key: 0,
									variant: "outline",
									class: "text-xs"
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(contact.organization.name), 1)]),
									_: 2
								}, 1024)) : createCommentVNode("", true), contact.is_favorite ? (openBlock(), createElementBlock("span", _hoisted_16, "★")) : createCommentVNode("", true)])]);
							}), 128))]),
							_: 1
						})]),
						_: 1
					})) : createCommentVNode("", true),
					mode.value === "reset" ? (openBlock(), createBlock(FeatureCard_default, {
						key: 3,
						title: "Reset on Filter Change"
					}, {
						description: withCtx(() => [..._cache[12] || (_cache[12] = [
							createTextVNode(" Use ", -1),
							createBaseVNode("code", { class: "text-xs" }, "reset: ['contacts']", -1),
							createTextVNode(" to clear loaded pages when filters change. ", -1)
						])]),
						default: withCtx(() => [
							createBaseVNode("div", _hoisted_18, [withDirectives(createBaseVNode("input", {
								id: "favorites-filter",
								type: "checkbox",
								"onUpdate:modelValue": _cache[4] || (_cache[4] = ($event) => favoritesOnly.value = $event),
								onChange: applyFilter,
								class: "size-4 rounded border"
							}, null, 544), [[vModelCheckbox, favoritesOnly.value]]), _cache[13] || (_cache[13] = createBaseVNode("label", {
								for: "favorites-filter",
								class: "text-sm"
							}, " Show favorites only ", -1))]),
							createVNode(unref(infiniteScroll_default), {
								data: "contacts",
								class: "space-y-2",
								"preserve-url": ""
							}, {
								loading: withCtx(() => [..._cache[14] || (_cache[14] = [createBaseVNode("div", { class: "py-3 text-center text-sm text-muted-foreground" }, " Loading more contacts... ", -1)])]),
								default: withCtx(() => [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.contacts.data, (contact) => {
									return openBlock(), createElementBlock("div", {
										key: contact.id,
										class: "flex items-center justify-between rounded border border-black/10 px-3 py-2 dark:border-white/10"
									}, [createBaseVNode("div", null, [createBaseVNode("p", _hoisted_19, toDisplayString(contact.first_name) + " " + toDisplayString(contact.last_name), 1), createBaseVNode("p", _hoisted_20, toDisplayString(contact.email), 1)]), createBaseVNode("div", _hoisted_21, [contact.organization ? (openBlock(), createBlock(unref(Badge_default), {
										key: 0,
										variant: "outline",
										class: "text-xs"
									}, {
										default: withCtx(() => [createTextVNode(toDisplayString(contact.organization.name), 1)]),
										_: 2
									}, 1024)) : createCommentVNode("", true), contact.is_favorite ? (openBlock(), createElementBlock("span", _hoisted_22, "★")) : createCommentVNode("", true)])]);
								}), 128))]),
								_: 1
							}),
							createBaseVNode("div", _hoisted_23, [createVNode(CodeBlock_default, {
								title: "Code",
								code: "router.reload({\n  data: { favorites: '1' },\n  only: ['contacts'],\n  reset: ['contacts'], // Clears loaded pages\n})"
							}), _cache[15] || (_cache[15] = createBaseVNode("p", { class: "text-sm text-muted-foreground" }, [
								createTextVNode(" Without "),
								createBaseVNode("code", null, "reset"),
								createTextVNode(", new filtered results would merge with stale pages. ")
							], -1))])
						]),
						_: 1
					})) : createCommentVNode("", true)
				])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { InfiniteScroll_default as default };

import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, Dn as watch, G as Fragment, Mn as withCtx, Sr as router, a as infiniteScroll_default, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, gr as normalizeClass, i as head_default, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, rr as ref, rt as createBlock, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Heart } from "./heart.js";
import { t as Plus } from "./plus.js";
import { t as Search } from "./search.js";
import { t as Input_default } from "./Input.js";
import { t as Badge_default } from "./badge.js";
//#region resources/js/pages/Contacts/Index.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-4 p-4" };
var _hoisted_2 = { class: "flex items-center justify-between" };
var _hoisted_3 = { class: "flex items-center gap-3" };
var _hoisted_4 = { class: "relative max-w-sm flex-1" };
var _hoisted_5 = { class: "flex size-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-sm font-medium text-primary" };
var _hoisted_6 = { class: "min-w-0 flex-1" };
var _hoisted_7 = { class: "flex items-center gap-2" };
var _hoisted_8 = { class: "font-medium" };
var _hoisted_9 = { class: "truncate text-sm text-muted-foreground" };
var _hoisted_10 = {
	key: 0,
	class: "flex flex-col items-center justify-center py-12 text-center"
};
//#endregion
//#region resources/js/pages/Contacts/Index.vue
var Index_default = /* @__PURE__ */ defineComponent({
	__name: "Index",
	props: {
		contacts: {},
		filters: {}
	},
	setup(__props) {
		const props = __props;
		const breadcrumbs = [{ title: "CRM" }, {
			title: "Contacts",
			href: "/contacts"
		}];
		const search = ref(props.filters.search ?? "");
		const favoriteFilter = ref(props.filters.favorite);
		let searchTimeout;
		watch(search, (value) => {
			clearTimeout(searchTimeout);
			searchTimeout = setTimeout(() => {
				router.visit("/contacts", {
					data: {
						search: value || void 0,
						favorite: favoriteFilter.value ? true : void 0
					},
					only: ["contacts", "filters"],
					reset: ["contacts"],
					preserveState: true,
					preserveScroll: true
				});
			}, 300);
		});
		function toggleFavoriteFilter() {
			favoriteFilter.value = !favoriteFilter.value;
			router.visit("/contacts", {
				data: {
					search: search.value || void 0,
					favorite: favoriteFilter.value ? true : void 0
				},
				only: ["contacts", "filters"],
				reset: ["contacts"],
				preserveState: true
			});
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Contacts" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [
					createBaseVNode("div", _hoisted_2, [_cache[2] || (_cache[2] = createBaseVNode("h1", { class: "text-2xl font-semibold tracking-tight" }, "Contacts", -1)), createVNode(unref(Button_default), { "as-child": "" }, {
						default: withCtx(() => [createVNode(unref(link_default), { href: "/contacts/create" }, {
							default: withCtx(() => [createVNode(unref(Plus), { class: "size-4" }), _cache[1] || (_cache[1] = createTextVNode(" Add Contact ", -1))]),
							_: 1
						})]),
						_: 1
					})]),
					createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [createVNode(unref(Search), { class: "absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground" }), createVNode(unref(Input_default), {
						modelValue: search.value,
						"onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => search.value = $event),
						placeholder: "Search contacts...",
						class: "pl-9"
					}, null, 8, ["modelValue"])]), createVNode(unref(Button_default), {
						variant: "outline",
						size: "sm",
						class: normalizeClass({ "bg-accent": favoriteFilter.value }),
						onClick: toggleFavoriteFilter
					}, {
						default: withCtx(() => [createVNode(unref(Heart), { class: normalizeClass(["size-4", { "fill-current text-red-500": favoriteFilter.value }]) }, null, 8, ["class"]), _cache[3] || (_cache[3] = createTextVNode(" Favorites ", -1))]),
						_: 1
					}, 8, ["class"])]),
					createVNode(unref(infiniteScroll_default), {
						data: "contacts",
						buffer: 300,
						"preserve-url": "",
						class: "space-y-2"
					}, {
						loading: withCtx(() => [..._cache[4] || (_cache[4] = [createBaseVNode("div", { class: "flex justify-center py-4" }, [createBaseVNode("div", { class: "text-sm text-muted-foreground" }, " Loading more contacts... ")], -1)])]),
						default: withCtx(() => [(openBlock(true), createElementBlock(Fragment, null, renderList(props.contacts.data, (contact) => {
							return openBlock(), createBlock(unref(link_default), {
								key: contact.id,
								href: "/contacts/" + contact.id,
								prefetch: "hover",
								class: "flex items-center gap-4 rounded-lg bg-muted/30 p-4 hover:bg-muted/50"
							}, {
								default: withCtx(() => [
									createBaseVNode("div", _hoisted_5, toDisplayString(contact.first_name[0]) + toDisplayString(contact.last_name[0]), 1),
									createBaseVNode("div", _hoisted_6, [createBaseVNode("div", _hoisted_7, [createBaseVNode("span", _hoisted_8, toDisplayString(contact.first_name) + " " + toDisplayString(contact.last_name), 1), contact.is_favorite ? (openBlock(), createBlock(unref(Heart), {
										key: 0,
										class: "size-3 shrink-0 fill-red-500 text-red-500"
									})) : createCommentVNode("", true)]), createBaseVNode("div", _hoisted_9, toDisplayString(contact.email), 1)]),
									contact.organization ? (openBlock(), createBlock(unref(Badge_default), {
										key: 0,
										variant: "secondary"
									}, {
										default: withCtx(() => [createTextVNode(toDisplayString(contact.organization.name), 1)]),
										_: 2
									}, 1024)) : createCommentVNode("", true)
								]),
								_: 2
							}, 1032, ["href"]);
						}), 128))]),
						_: 1
					}),
					props.contacts.data.length === 0 ? (openBlock(), createElementBlock("div", _hoisted_10, [..._cache[5] || (_cache[5] = [createBaseVNode("p", { class: "text-muted-foreground" }, "No contacts found.", -1)])])) : createCommentVNode("", true)
				])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { Index_default as default };

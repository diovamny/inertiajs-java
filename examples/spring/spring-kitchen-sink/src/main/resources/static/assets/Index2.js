import { $t as openBlock, Dn as watch, G as Fragment, Mn as withCtx, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, gr as normalizeClass, i as head_default, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, rr as ref, rt as createBlock, yr as toDisplayString } from "./dist.js";
import { i as Building2, t as AppLayout_default } from "./AppLayout.js";
import { t as Search } from "./search.js";
import { t as Input_default } from "./Input.js";
import { t as Badge_default } from "./badge.js";
//#region resources/js/pages/Organizations/Index.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex flex-1 flex-col gap-4 p-4" };
var _hoisted_2 = { class: "relative max-w-sm" };
var _hoisted_3 = { class: "space-y-2" };
var _hoisted_4 = { class: "flex size-10 shrink-0 items-center justify-center rounded-full bg-primary/10" };
var _hoisted_5 = { class: "flex-1" };
var _hoisted_6 = { class: "font-medium" };
var _hoisted_7 = {
	key: 0,
	class: "flex flex-col items-center justify-center py-12 text-center"
};
var _hoisted_8 = {
	key: 1,
	class: "flex items-center justify-center gap-1"
};
var _hoisted_9 = ["innerHTML"];
var _hoisted_10 = {
	key: 1,
	class: "rounded-md px-3 py-1 text-sm text-muted-foreground"
};
var _hoisted_11 = ["innerHTML"];
//#endregion
//#region resources/js/pages/Organizations/Index.vue
var Index_default = /* @__PURE__ */ defineComponent({
	__name: "Index",
	props: {
		organizations: {},
		filters: {}
	},
	setup(__props) {
		const props = __props;
		const breadcrumbs = [{ title: "CRM" }, {
			title: "Organizations",
			href: "/organizations"
		}];
		const search = ref(props.filters.search ?? "");
		let searchTimeout;
		watch(search, (value) => {
			clearTimeout(searchTimeout);
			searchTimeout = setTimeout(() => {
				router.visit("/organizations", {
					data: { search: value || void 0 },
					only: ["organizations", "filters"],
					preserveState: true
				});
			}, 300);
		});
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Organizations" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [
					_cache[2] || (_cache[2] = createBaseVNode("h1", { class: "text-2xl font-semibold tracking-tight" }, "Organizations", -1)),
					createBaseVNode("div", _hoisted_2, [createVNode(unref(Search), { class: "absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground" }), createVNode(unref(Input_default), {
						modelValue: search.value,
						"onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => search.value = $event),
						placeholder: "Search organizations...",
						class: "pl-9"
					}, null, 8, ["modelValue"])]),
					createBaseVNode("div", _hoisted_3, [(openBlock(true), createElementBlock(Fragment, null, renderList(props.organizations.data, (org) => {
						return openBlock(), createBlock(unref(link_default), {
							key: org.id,
							href: "/organizations/" + org.id,
							class: "flex items-center gap-4 rounded-lg bg-muted/30 p-4 hover:bg-muted/50"
						}, {
							default: withCtx(() => [
								createBaseVNode("div", _hoisted_4, [createVNode(unref(Building2), { class: "size-5 shrink-0 text-primary" })]),
								createBaseVNode("div", _hoisted_5, [createBaseVNode("span", _hoisted_6, toDisplayString(org.name), 1)]),
								createVNode(unref(Badge_default), { variant: "outline" }, {
									default: withCtx(() => [createTextVNode(toDisplayString(org.contacts_count) + " " + toDisplayString(org.contacts_count === 1 ? "contact" : "contacts"), 1)]),
									_: 2
								}, 1024)
							]),
							_: 2
						}, 1032, ["href"]);
					}), 128))]),
					props.organizations.data.length === 0 ? (openBlock(), createElementBlock("div", _hoisted_7, [..._cache[1] || (_cache[1] = [createBaseVNode("p", { class: "text-muted-foreground" }, "No organizations found.", -1)])])) : createCommentVNode("", true),
					props.organizations.meta.links.length > 3 ? (openBlock(), createElementBlock("div", _hoisted_8, [(openBlock(true), createElementBlock(Fragment, null, renderList(props.organizations.meta.links, (link) => {
						return openBlock(), createElementBlock(Fragment, { key: link.label }, [link.url ? (openBlock(), createBlock(unref(link_default), {
							key: 0,
							href: link.url,
							class: normalizeClass(["rounded-md px-3 py-1 text-sm hover:bg-accent", { "bg-primary text-primary-foreground hover:bg-primary/90": link.active }]),
							"preserve-state": ""
						}, {
							default: withCtx(() => [createBaseVNode("span", { innerHTML: link.label }, null, 8, _hoisted_9)]),
							_: 2
						}, 1032, ["href", "class"])) : (openBlock(), createElementBlock("span", _hoisted_10, [createBaseVNode("span", { innerHTML: link.label }, null, 8, _hoisted_11)]))], 64);
					}), 128))])) : createCommentVNode("", true)
				])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { Index_default as default };

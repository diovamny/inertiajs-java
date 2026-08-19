import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, a as infiniteScroll_default, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, r as form_default, rt as createBlock, yr as toDisplayString } from "./dist.js";
import { i as Building2, t as AppLayout_default } from "./AppLayout.js";
import { t as Input_default } from "./Input.js";
import { i as Card_default, n as CardHeader_default, r as CardContent_default, t as CardTitle_default } from "./CardTitle.js";
import { t as Badge_default } from "./badge.js";
//#region resources/js/pages/Organizations/Show.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "flex items-center gap-3" };
var _hoisted_3 = { class: "flex size-12 shrink-0 items-center justify-center rounded-full bg-primary/10" };
var _hoisted_4 = { class: "flex max-w-md items-end gap-3" };
var _hoisted_5 = { class: "flex-1 space-y-2" };
var _hoisted_6 = {
	key: 0,
	class: "text-sm text-red-600"
};
var _hoisted_7 = {
	key: 0,
	class: "mt-2 text-sm text-green-600"
};
var _hoisted_8 = { class: "flex size-8 shrink-0 items-center justify-center rounded-full bg-primary/10 text-xs font-medium text-primary" };
var _hoisted_9 = { class: "min-w-0 flex-1" };
var _hoisted_10 = { class: "text-sm font-medium" };
var _hoisted_11 = {
	key: 0,
	class: "truncate text-xs text-muted-foreground"
};
var _hoisted_12 = {
	key: 0,
	class: "py-4 text-center text-sm text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Organizations/Show.vue
var Show_default = /* @__PURE__ */ defineComponent({
	__name: "Show",
	props: {
		organization: {},
		contacts: {}
	},
	setup(__props) {
		const props = __props;
		const breadcrumbs = [
			{ title: "CRM" },
			{
				title: "Organizations",
				href: "/organizations"
			},
			{ title: props.organization.name }
		];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: __props.organization.name }, null, 8, ["title"]), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(unref(Card_default), null, {
					default: withCtx(() => [createVNode(unref(CardHeader_default), null, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_2, [createBaseVNode("div", _hoisted_3, [createVNode(unref(Building2), { class: "size-6 shrink-0 text-primary" })]), createBaseVNode("div", null, [createVNode(unref(CardTitle_default), { class: "text-2xl tracking-tight" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.organization.name), 1)]),
							_: 1
						}), createVNode(unref(Badge_default), {
							variant: "outline",
							class: "mt-1"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.organization.contacts_count) + " " + toDisplayString(__props.organization.contacts_count === 1 ? "contact" : "contacts"), 1)]),
							_: 1
						})])])]),
						_: 1
					}), createVNode(unref(CardContent_default), null, {
						default: withCtx(() => [createVNode(unref(form_default), {
							action: "/organizations/" + __props.organization.id,
							method: "put",
							"set-defaults-on-success": ""
						}, {
							default: withCtx(({ errors, processing, isDirty, recentlySuccessful }) => [createBaseVNode("div", _hoisted_4, [createBaseVNode("div", _hoisted_5, [
								_cache[0] || (_cache[0] = createBaseVNode("label", {
									for: "name",
									class: "text-sm font-medium"
								}, "Organization Name", -1)),
								createVNode(unref(Input_default), {
									id: "name",
									name: "name",
									"default-value": __props.organization.name
								}, null, 8, ["default-value"]),
								errors.name ? (openBlock(), createElementBlock("p", _hoisted_6, toDisplayString(errors.name), 1)) : createCommentVNode("", true)
							]), createVNode(unref(Button_default), {
								type: "submit",
								disabled: processing || !isDirty
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(processing ? "Saving..." : "Update"), 1)]),
								_: 2
							}, 1032, ["disabled"])]), recentlySuccessful ? (openBlock(), createElementBlock("p", _hoisted_7, " Saved! ")) : createCommentVNode("", true)]),
							_: 1
						}, 8, ["action"])]),
						_: 1
					})]),
					_: 1
				}), createVNode(unref(Card_default), null, {
					default: withCtx(() => [createVNode(unref(CardHeader_default), null, {
						default: withCtx(() => [createVNode(unref(CardTitle_default), null, {
							default: withCtx(() => [..._cache[1] || (_cache[1] = [createTextVNode("Members", -1)])]),
							_: 1
						})]),
						_: 1
					}), createVNode(unref(CardContent_default), null, {
						default: withCtx(() => [createVNode(unref(infiniteScroll_default), {
							data: "contacts",
							buffer: 300,
							"preserve-url": "",
							class: "space-y-2"
						}, {
							loading: withCtx(() => [..._cache[2] || (_cache[2] = [createBaseVNode("div", { class: "flex justify-center py-4" }, [createBaseVNode("div", { class: "text-sm text-muted-foreground" }, " Loading more... ")], -1)])]),
							default: withCtx(() => [(openBlock(true), createElementBlock(Fragment, null, renderList(props.contacts.data, (contact) => {
								return openBlock(), createBlock(unref(link_default), {
									key: contact.id,
									href: "/contacts/" + contact.id,
									class: "flex items-center gap-3 rounded-lg bg-muted/30 p-3 hover:bg-muted/50"
								}, {
									default: withCtx(() => [createBaseVNode("div", _hoisted_8, toDisplayString(contact.first_name[0]) + toDisplayString(contact.last_name[0]), 1), createBaseVNode("div", _hoisted_9, [createBaseVNode("span", _hoisted_10, toDisplayString(contact.first_name) + " " + toDisplayString(contact.last_name), 1), contact.email ? (openBlock(), createElementBlock("div", _hoisted_11, toDisplayString(contact.email), 1)) : createCommentVNode("", true)])]),
									_: 2
								}, 1032, ["href"]);
							}), 128))]),
							_: 1
						}), props.contacts.data.length === 0 ? (openBlock(), createElementBlock("div", _hoisted_12, " No contacts in this organization. ")) : createCommentVNode("", true)]),
						_: 1
					})]),
					_: 1
				})])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { Show_default as default };

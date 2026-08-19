import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, pr as unref, r as form_default, yr as toDisplayString } from "./dist.js";
import { t as InputError_default } from "./InputError.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Label_default } from "./Label.js";
import { t as Input_default } from "./Input.js";
import { i as Card_default, n as CardHeader_default, r as CardContent_default, t as CardTitle_default } from "./CardTitle.js";
//#region resources/js/pages/Contacts/Create.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-4 p-4" };
var _hoisted_2 = { class: "grid gap-4 sm:grid-cols-2" };
var _hoisted_3 = { class: "space-y-2" };
var _hoisted_4 = { class: "space-y-2" };
var _hoisted_5 = { class: "grid gap-4 sm:grid-cols-2" };
var _hoisted_6 = { class: "space-y-2" };
var _hoisted_7 = { class: "space-y-2" };
var _hoisted_8 = { class: "space-y-2" };
var _hoisted_9 = {
	id: "organization_id",
	name: "organization_id",
	class: "flex h-9 w-full rounded-md border border-input/60 bg-background px-3 py-1 text-sm placeholder:text-muted-foreground focus-visible:ring-1 focus-visible:ring-ring focus-visible:outline-none"
};
var _hoisted_10 = ["value"];
var _hoisted_11 = { class: "flex items-center gap-3 pt-2" };
var _hoisted_12 = {
	key: 0,
	class: "text-sm text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Contacts/Create.vue
var Create_default = /* @__PURE__ */ defineComponent({
	__name: "Create",
	props: { organizations: {} },
	setup(__props) {
		const breadcrumbs = [
			{ title: "CRM" },
			{
				title: "Contacts",
				href: "/contacts"
			},
			{ title: "Create Contact" }
		];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Create Contact" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [_cache[7] || (_cache[7] = createBaseVNode("h1", { class: "text-2xl font-semibold tracking-tight" }, " Create Contact ", -1)), createVNode(unref(Card_default), { class: "max-w-2xl" }, {
					default: withCtx(() => [createVNode(unref(CardHeader_default), null, {
						default: withCtx(() => [createVNode(unref(CardTitle_default), null, {
							default: withCtx(() => [..._cache[0] || (_cache[0] = [createTextVNode("Contact Information", -1)])]),
							_: 1
						})]),
						_: 1
					}), createVNode(unref(CardContent_default), null, {
						default: withCtx(() => [createVNode(unref(form_default), {
							action: "/contacts",
							method: "post",
							class: "space-y-4"
						}, {
							default: withCtx(({ errors, processing, isDirty }) => [
								createBaseVNode("div", _hoisted_2, [createBaseVNode("div", _hoisted_3, [
									createVNode(unref(Label_default), { for: "first_name" }, {
										default: withCtx(() => [..._cache[1] || (_cache[1] = [createTextVNode("First Name", -1)])]),
										_: 1
									}),
									createVNode(unref(Input_default), {
										id: "first_name",
										name: "first_name"
									}),
									createVNode(InputError_default, { message: errors.first_name }, null, 8, ["message"])
								]), createBaseVNode("div", _hoisted_4, [
									createVNode(unref(Label_default), { for: "last_name" }, {
										default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode("Last Name", -1)])]),
										_: 1
									}),
									createVNode(unref(Input_default), {
										id: "last_name",
										name: "last_name"
									}),
									createVNode(InputError_default, { message: errors.last_name }, null, 8, ["message"])
								])]),
								createBaseVNode("div", _hoisted_5, [createBaseVNode("div", _hoisted_6, [
									createVNode(unref(Label_default), { for: "email" }, {
										default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode("Email", -1)])]),
										_: 1
									}),
									createVNode(unref(Input_default), {
										id: "email",
										type: "email",
										name: "email"
									}),
									createVNode(InputError_default, { message: errors.email }, null, 8, ["message"])
								]), createBaseVNode("div", _hoisted_7, [
									createVNode(unref(Label_default), { for: "phone" }, {
										default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode("Phone", -1)])]),
										_: 1
									}),
									createVNode(unref(Input_default), {
										id: "phone",
										name: "phone"
									}),
									createVNode(InputError_default, { message: errors.phone }, null, 8, ["message"])
								])]),
								createBaseVNode("div", _hoisted_8, [
									createVNode(unref(Label_default), { for: "organization_id" }, {
										default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode("Organization", -1)])]),
										_: 1
									}),
									createBaseVNode("select", _hoisted_9, [_cache[6] || (_cache[6] = createBaseVNode("option", { value: "" }, "None", -1)), (openBlock(true), createElementBlock(Fragment, null, renderList(__props.organizations, (org) => {
										return openBlock(), createElementBlock("option", {
											key: org.id,
											value: org.id
										}, toDisplayString(org.name), 9, _hoisted_10);
									}), 128))]),
									createVNode(InputError_default, { message: errors.organization_id }, null, 8, ["message"])
								]),
								createBaseVNode("div", _hoisted_11, [createVNode(unref(Button_default), {
									type: "submit",
									disabled: processing
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(processing ? "Creating..." : "Create Contact"), 1)]),
									_: 2
								}, 1032, ["disabled"]), isDirty ? (openBlock(), createElementBlock("span", _hoisted_12, "Unsaved changes")) : createCommentVNode("", true)])
							]),
							_: 1
						})]),
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
export { Create_default as default };

import { s as Button_default, t as createLucideIcon } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, gr as normalizeClass, i as head_default, it as createCommentVNode, mt as defineComponent, n as deferred_default, nt as createBaseVNode, o as link_default, pr as unref, r as form_default, rt as createBlock, yr as toDisplayString } from "./dist.js";
import { i as Building2, t as AppLayout_default } from "./AppLayout.js";
import { t as Heart } from "./heart.js";
import { i as Card_default, n as CardHeader_default, r as CardContent_default, t as CardTitle_default } from "./CardTitle.js";
import { t as CardDescription_default } from "./CardDescription.js";
import { t as Skeleton_default } from "./Skeleton.js";
import { t as Badge_default } from "./badge.js";
//#region node_modules/lucide-vue-next/dist/esm/icons/mail.js
/**
* @license lucide-vue-next v0.468.0 - ISC
*
* This source code is licensed under the ISC license.
* See the LICENSE file in the root directory of this source tree.
*/
var Mail = createLucideIcon("MailIcon", [["rect", {
	width: "20",
	height: "16",
	x: "2",
	y: "4",
	rx: "2",
	key: "18n3k1"
}], ["path", {
	d: "m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7",
	key: "1ocrg3"
}]]);
//#endregion
//#region node_modules/lucide-vue-next/dist/esm/icons/pencil.js
/**
* @license lucide-vue-next v0.468.0 - ISC
*
* This source code is licensed under the ISC license.
* See the LICENSE file in the root directory of this source tree.
*/
var Pencil = createLucideIcon("PencilIcon", [["path", {
	d: "M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z",
	key: "1a8usu"
}], ["path", {
	d: "m15 5 4 4",
	key: "1mk7zo"
}]]);
//#endregion
//#region node_modules/lucide-vue-next/dist/esm/icons/phone.js
/**
* @license lucide-vue-next v0.468.0 - ISC
*
* This source code is licensed under the ISC license.
* See the LICENSE file in the root directory of this source tree.
*/
var Phone = createLucideIcon("PhoneIcon", [["path", {
	d: "M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z",
	key: "foiqr5"
}]]);
//#endregion
//#region node_modules/lucide-vue-next/dist/esm/icons/trash-2.js
/**
* @license lucide-vue-next v0.468.0 - ISC
*
* This source code is licensed under the ISC license.
* See the LICENSE file in the root directory of this source tree.
*/
var Trash2 = createLucideIcon("Trash2Icon", [
	["path", {
		d: "M3 6h18",
		key: "d0wm0j"
	}],
	["path", {
		d: "M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6",
		key: "4alrt4"
	}],
	["path", {
		d: "M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2",
		key: "v07s0e"
	}],
	["line", {
		x1: "10",
		x2: "10",
		y1: "11",
		y2: "17",
		key: "1uufr5"
	}],
	["line", {
		x1: "14",
		x2: "14",
		y1: "11",
		y2: "17",
		key: "xtxkd"
	}]
]);
//#endregion
//#region resources/js/pages/Contacts/Show.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "flex items-start justify-between" };
var _hoisted_3 = { class: "flex items-center gap-4" };
var _hoisted_4 = { class: "flex size-16 shrink-0 items-center justify-center rounded-full bg-primary/10 text-xl font-semibold text-primary" };
var _hoisted_5 = { class: "flex items-center gap-2" };
var _hoisted_6 = { class: "text-2xl font-semibold tracking-tight" };
var _hoisted_7 = { class: "flex gap-2" };
var _hoisted_8 = { class: "grid gap-6 md:grid-cols-2" };
var _hoisted_9 = {
	key: 0,
	class: "flex items-center gap-2 text-sm"
};
var _hoisted_10 = ["href"];
var _hoisted_11 = {
	key: 1,
	class: "flex items-center gap-2 text-sm"
};
var _hoisted_12 = {
	key: 2,
	class: "text-sm text-muted-foreground"
};
var _hoisted_13 = {
	key: 0,
	class: "mt-1 text-sm text-red-600"
};
var _hoisted_14 = { class: "space-y-3" };
var _hoisted_15 = {
	key: 0,
	class: "space-y-3"
};
var _hoisted_16 = { class: "mb-1 flex items-center justify-between" };
var _hoisted_17 = { class: "text-sm font-medium" };
var _hoisted_18 = { class: "text-xs text-muted-foreground" };
var _hoisted_19 = { class: "text-sm text-muted-foreground" };
var _hoisted_20 = {
	key: 1,
	class: "text-sm text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Contacts/Show.vue
var Show_default = /* @__PURE__ */ defineComponent({
	__name: "Show",
	props: {
		contact: {},
		notes: {}
	},
	setup(__props) {
		const props = __props;
		const breadcrumbs = [
			{ title: "CRM" },
			{
				title: "Contacts",
				href: "/contacts"
			},
			{ title: `${props.contact.first_name} ${props.contact.last_name}` }
		];
		function toggleFavorite() {
			router.post("/contacts/" + props.contact.id + "/favorite", {}, {
				preserveScroll: true,
				optimistic: (currentProps) => {
					const contact = currentProps.contact;
					return { contact: {
						...contact,
						is_favorite: !contact.is_favorite
					} };
				}
			});
		}
		function deleteContact() {
			if (confirm("Are you sure you want to delete this contact?")) router.delete("/contacts/" + props.contact.id);
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: `${__props.contact.first_name} ${__props.contact.last_name}` }, null, 8, ["title"]), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createBaseVNode("div", _hoisted_2, [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, toDisplayString(__props.contact.first_name[0]) + toDisplayString(__props.contact.last_name[0]), 1), createBaseVNode("div", null, [createBaseVNode("div", _hoisted_5, [createBaseVNode("h1", _hoisted_6, toDisplayString(__props.contact.first_name) + " " + toDisplayString(__props.contact.last_name), 1), createBaseVNode("button", {
					type: "button",
					onClick: toggleFavorite,
					class: "focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
				}, [createVNode(unref(Heart), { class: normalizeClass(["size-5 shrink-0", __props.contact.is_favorite ? "fill-red-500 text-red-500" : "text-muted-foreground hover:text-red-500"]) }, null, 8, ["class"])])]), __props.contact.organization ? (openBlock(), createBlock(unref(Badge_default), {
					key: 0,
					variant: "secondary",
					class: "mt-1"
				}, {
					default: withCtx(() => [createVNode(unref(Building2), { class: "size-3 shrink-0" }), createTextVNode(" " + toDisplayString(__props.contact.organization.name), 1)]),
					_: 1
				})) : createCommentVNode("", true)])]), createBaseVNode("div", _hoisted_7, [createVNode(unref(Button_default), {
					variant: "outline",
					"as-child": ""
				}, {
					default: withCtx(() => [createVNode(unref(link_default), { href: "/contacts/" + __props.contact.id + "/edit" }, {
						default: withCtx(() => [createVNode(unref(Pencil), { class: "size-4" }), _cache[0] || (_cache[0] = createTextVNode(" Edit ", -1))]),
						_: 1
					}, 8, ["href"])]),
					_: 1
				}), createVNode(unref(Button_default), {
					variant: "outline",
					class: "text-destructive",
					onClick: deleteContact
				}, {
					default: withCtx(() => [createVNode(unref(Trash2), { class: "size-4" }), _cache[1] || (_cache[1] = createTextVNode(" Delete ", -1))]),
					_: 1
				})])]), createBaseVNode("div", _hoisted_8, [createVNode(unref(Card_default), null, {
					default: withCtx(() => [createVNode(unref(CardHeader_default), null, {
						default: withCtx(() => [createVNode(unref(CardTitle_default), null, {
							default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode("Contact Information", -1)])]),
							_: 1
						})]),
						_: 1
					}), createVNode(unref(CardContent_default), { class: "space-y-3" }, {
						default: withCtx(() => [
							__props.contact.email ? (openBlock(), createElementBlock("div", _hoisted_9, [createVNode(unref(Mail), { class: "size-4 shrink-0 text-muted-foreground" }), createBaseVNode("a", {
								href: `mailto:${__props.contact.email}`,
								class: "text-primary hover:underline"
							}, toDisplayString(__props.contact.email), 9, _hoisted_10)])) : createCommentVNode("", true),
							__props.contact.phone ? (openBlock(), createElementBlock("div", _hoisted_11, [createVNode(unref(Phone), { class: "size-4 shrink-0 text-muted-foreground" }), createBaseVNode("span", null, toDisplayString(__props.contact.phone), 1)])) : createCommentVNode("", true),
							!__props.contact.email && !__props.contact.phone ? (openBlock(), createElementBlock("div", _hoisted_12, " No contact information available. ")) : createCommentVNode("", true)
						]),
						_: 1
					})]),
					_: 1
				}), createVNode(unref(Card_default), null, {
					default: withCtx(() => [createVNode(unref(CardHeader_default), null, {
						default: withCtx(() => [createVNode(unref(CardTitle_default), null, {
							default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode("Notes", -1)])]),
							_: 1
						}), createVNode(unref(CardDescription_default), null, {
							default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode("Add and view notes for this contact", -1)])]),
							_: 1
						})]),
						_: 1
					}), createVNode(unref(CardContent_default), null, {
						default: withCtx(() => [createVNode(unref(form_default), {
							action: "/contacts/" + __props.contact.id + "/notes",
							method: "post",
							"reset-on-success": "",
							options: { preserveScroll: true },
							class: "mb-4"
						}, {
							default: withCtx(({ errors, processing }) => [
								_cache[6] || (_cache[6] = createBaseVNode("textarea", {
									name: "body",
									placeholder: "Add a note...",
									class: "w-full rounded-md border bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus:ring-2 focus:ring-ring focus:outline-none",
									rows: "3"
								}, null, -1)),
								errors.body ? (openBlock(), createElementBlock("div", _hoisted_13, toDisplayString(errors.body), 1)) : createCommentVNode("", true),
								createVNode(unref(Button_default), {
									type: "submit",
									size: "sm",
									class: "mt-2",
									disabled: processing
								}, {
									default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" Add Note ", -1)])]),
									_: 1
								}, 8, ["disabled"])
							]),
							_: 1
						}, 8, ["action"]), createVNode(unref(deferred_default), { data: "notes" }, {
							fallback: withCtx(() => [createBaseVNode("div", _hoisted_14, [createVNode(unref(Skeleton_default), { class: "h-16 w-full" }), createVNode(unref(Skeleton_default), { class: "h-16 w-full" })])]),
							default: withCtx(() => [__props.notes && __props.notes.length > 0 ? (openBlock(), createElementBlock("div", _hoisted_15, [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.notes, (note) => {
								return openBlock(), createElementBlock("div", {
									key: note.id,
									class: "rounded-lg bg-muted/30 p-3"
								}, [createBaseVNode("div", _hoisted_16, [createBaseVNode("span", _hoisted_17, toDisplayString(note.user?.name), 1), createBaseVNode("time", _hoisted_18, toDisplayString(new Date(note.created_at).toLocaleDateString()), 1)]), createBaseVNode("p", _hoisted_19, toDisplayString(note.body), 1)]);
							}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_20, " No notes yet. "))]),
							_: 1
						})]),
						_: 1
					})]),
					_: 1
				})])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { Show_default as default };

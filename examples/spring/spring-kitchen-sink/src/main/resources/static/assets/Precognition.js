import { s as Button_default, t as createLucideIcon } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, gr as normalizeClass, i as head_default, it as createCommentVNode, l as useForm, mt as defineComponent, nt as createBaseVNode, pr as unref, r as form_default, rr as ref, rt as createBlock, yr as toDisplayString, z as withModifiers } from "./dist.js";
import { t as InputError_default } from "./InputError.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Label_default } from "./Label.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Input_default } from "./Input.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region node_modules/lucide-vue-next/dist/esm/icons/circle-check.js
/**
* @license lucide-vue-next v0.468.0 - ISC
*
* This source code is licensed under the ISC license.
* See the LICENSE file in the root directory of this source tree.
*/
var CircleCheck = createLucideIcon("CircleCheckIcon", [["circle", {
	cx: "12",
	cy: "12",
	r: "10",
	key: "1mglay"
}], ["path", {
	d: "m9 12 2 2 4-4",
	key: "dzmm74"
}]]);
//#endregion
//#region node_modules/lucide-vue-next/dist/esm/icons/circle-x.js
/**
* @license lucide-vue-next v0.468.0 - ISC
*
* This source code is licensed under the ISC license.
* See the LICENSE file in the root directory of this source tree.
*/
var CircleX = createLucideIcon("CircleXIcon", [
	["circle", {
		cx: "12",
		cy: "12",
		r: "10",
		key: "1mglay"
	}],
	["path", {
		d: "m15 9-6 6",
		key: "1uzhvr"
	}],
	["path", {
		d: "m9 9 6 6",
		key: "z0biqf"
	}]
]);
//#endregion
//#region resources/js/pages/Features/Forms/Precognition.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "flex gap-2" };
var _hoisted_3 = {
	key: 0,
	class: "grid gap-6 lg:grid-cols-2"
};
var _hoisted_4 = { class: "space-y-2" };
var _hoisted_5 = { class: "relative" };
var _hoisted_6 = { class: "absolute top-1/2 right-2 -translate-y-1/2" };
var _hoisted_7 = {
	key: 0,
	class: "text-sm text-red-600"
};
var _hoisted_8 = {
	key: 1,
	class: "text-sm text-green-600"
};
var _hoisted_9 = { class: "space-y-2" };
var _hoisted_10 = { class: "relative" };
var _hoisted_11 = { class: "absolute top-1/2 right-2 -translate-y-1/2" };
var _hoisted_12 = {
	key: 0,
	class: "text-sm text-red-600"
};
var _hoisted_13 = { class: "space-y-2" };
var _hoisted_14 = {
	key: 0,
	class: "text-sm text-red-600"
};
var _hoisted_15 = {
	key: 1,
	class: "text-xs text-muted-foreground"
};
var _hoisted_16 = { class: "space-y-2" };
var _hoisted_17 = {
	key: 0,
	class: "text-sm text-red-600"
};
var _hoisted_18 = { class: "pt-2" };
var _hoisted_19 = { class: "space-y-6" };
var _hoisted_20 = { class: "space-y-3" };
var _hoisted_21 = { class: "flex items-center justify-between" };
var _hoisted_22 = { class: "flex items-center justify-between" };
var _hoisted_23 = { class: "flex items-center justify-between" };
var _hoisted_24 = { class: "space-y-3" };
var _hoisted_25 = { class: "font-medium" };
var _hoisted_26 = { class: "flex gap-1" };
var _hoisted_27 = { class: "flex flex-wrap gap-2" };
var _hoisted_28 = {
	key: 1,
	class: "grid gap-6 lg:grid-cols-2"
};
var _hoisted_29 = { class: "space-y-2" };
var _hoisted_30 = { class: "relative" };
var _hoisted_31 = { class: "absolute top-1/2 right-2 -translate-y-1/2" };
var _hoisted_32 = {
	key: 0,
	class: "text-sm text-green-600"
};
var _hoisted_33 = { class: "space-y-2" };
var _hoisted_34 = { class: "relative" };
var _hoisted_35 = { class: "absolute top-1/2 right-2 -translate-y-1/2" };
var _hoisted_36 = { class: "space-y-2" };
var _hoisted_37 = { class: "space-y-2" };
var _hoisted_38 = { class: "pt-2" };
var _hoisted_39 = { class: "space-y-6" };
var _hoisted_40 = { class: "space-y-3 text-sm text-muted-foreground" };
//#endregion
//#region resources/js/pages/Features/Forms/Precognition.vue
var Precognition_default = /* @__PURE__ */ defineComponent({
	__name: "Precognition",
	setup(__props) {
		const breadcrumbs = [{ title: "Forms" }, { title: "Precognition" }];
		const activeTab = ref("useForm");
		const form = useForm("post", "/features/forms/precognition", {
			username: "",
			email: "",
			password: "",
			password_confirmation: ""
		});
		form.setValidationTimeout(500);
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Precognition" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [
					createVNode(FeatureHeader_default, {
						title: "Precognition",
						docs: "the-basics/forms#precognition",
						controller: "app/Http/Controllers/Feature/FormController.php#L71"
					}, {
						default: withCtx(() => [..._cache[15] || (_cache[15] = [createTextVNode(" Real-time server-side validation on field change. No page submission needed. ", -1)])]),
						_: 1
					}),
					createBaseVNode("div", _hoisted_2, [createVNode(unref(Button_default), {
						variant: activeTab.value === "useForm" ? "default" : "outline",
						size: "sm",
						onClick: _cache[0] || (_cache[0] = ($event) => activeTab.value = "useForm")
					}, {
						default: withCtx(() => [..._cache[16] || (_cache[16] = [createTextVNode(" useForm + Precognition ", -1)])]),
						_: 1
					}, 8, ["variant"]), createVNode(unref(Button_default), {
						variant: activeTab.value === "formComponent" ? "default" : "outline",
						size: "sm",
						onClick: _cache[1] || (_cache[1] = ($event) => activeTab.value = "formComponent")
					}, {
						default: withCtx(() => [..._cache[17] || (_cache[17] = [createTextVNode(" Form Component + Precognition ", -1)])]),
						_: 1
					}, 8, ["variant"])]),
					activeTab.value === "useForm" ? (openBlock(), createElementBlock("div", _hoisted_3, [createVNode(FeatureCard_default, {
						title: "Create Account (useForm)",
						description: "Type in each field and tab away. Validation runs against the server in real time via precognition."
					}, {
						default: withCtx(() => [createBaseVNode("form", {
							class: "space-y-4",
							onSubmit: _cache[10] || (_cache[10] = withModifiers(($event) => unref(form).submit(), ["prevent"]))
						}, [
							createBaseVNode("div", _hoisted_4, [
								createVNode(unref(Label_default), { for: "p-username" }, {
									default: withCtx(() => [..._cache[18] || (_cache[18] = [createTextVNode("Username", -1)])]),
									_: 1
								}),
								createBaseVNode("div", _hoisted_5, [createVNode(unref(Input_default), {
									id: "p-username",
									modelValue: unref(form).username,
									"onUpdate:modelValue": _cache[2] || (_cache[2] = ($event) => unref(form).username = $event),
									onChange: _cache[3] || (_cache[3] = ($event) => unref(form).validate("username")),
									class: normalizeClass({
										"border-green-500": unref(form).valid("username"),
										"border-red-500": unref(form).invalid("username")
									})
								}, null, 8, ["modelValue", "class"]), createBaseVNode("div", _hoisted_6, [unref(form).valid("username") ? (openBlock(), createBlock(unref(CircleCheck), {
									key: 0,
									class: "size-4 text-green-500"
								})) : unref(form).invalid("username") ? (openBlock(), createBlock(unref(CircleX), {
									key: 1,
									class: "size-4 text-red-500"
								})) : createCommentVNode("", true)])]),
								unref(form).invalid("username") ? (openBlock(), createElementBlock("p", _hoisted_7, toDisplayString(unref(form).errors.username), 1)) : unref(form).valid("username") ? (openBlock(), createElementBlock("p", _hoisted_8, " Username available! ")) : createCommentVNode("", true),
								_cache[19] || (_cache[19] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " 3-20 characters, letters, numbers, dashes only. ", -1))
							]),
							createBaseVNode("div", _hoisted_9, [
								createVNode(unref(Label_default), { for: "p-email" }, {
									default: withCtx(() => [..._cache[20] || (_cache[20] = [createTextVNode("Email", -1)])]),
									_: 1
								}),
								createBaseVNode("div", _hoisted_10, [createVNode(unref(Input_default), {
									id: "p-email",
									type: "text",
									modelValue: unref(form).email,
									"onUpdate:modelValue": _cache[4] || (_cache[4] = ($event) => unref(form).email = $event),
									onChange: _cache[5] || (_cache[5] = ($event) => unref(form).validate("email")),
									class: normalizeClass({
										"border-green-500": unref(form).valid("email"),
										"border-red-500": unref(form).invalid("email")
									})
								}, null, 8, ["modelValue", "class"]), createBaseVNode("div", _hoisted_11, [unref(form).valid("email") ? (openBlock(), createBlock(unref(CircleCheck), {
									key: 0,
									class: "size-4 text-green-500"
								})) : unref(form).invalid("email") ? (openBlock(), createBlock(unref(CircleX), {
									key: 1,
									class: "size-4 text-red-500"
								})) : createCommentVNode("", true)])]),
								unref(form).invalid("email") ? (openBlock(), createElementBlock("p", _hoisted_12, toDisplayString(unref(form).errors.email), 1)) : createCommentVNode("", true)
							]),
							createBaseVNode("div", _hoisted_13, [
								createVNode(unref(Label_default), { for: "p-password" }, {
									default: withCtx(() => [..._cache[21] || (_cache[21] = [createTextVNode("Password", -1)])]),
									_: 1
								}),
								createVNode(unref(Input_default), {
									id: "p-password",
									type: "password",
									modelValue: unref(form).password,
									"onUpdate:modelValue": _cache[6] || (_cache[6] = ($event) => unref(form).password = $event),
									onChange: _cache[7] || (_cache[7] = ($event) => unref(form).validate("password")),
									class: normalizeClass({
										"border-green-500": unref(form).valid("password"),
										"border-red-500": unref(form).invalid("password")
									})
								}, null, 8, ["modelValue", "class"]),
								unref(form).invalid("password") ? (openBlock(), createElementBlock("p", _hoisted_14, toDisplayString(unref(form).errors.password), 1)) : (openBlock(), createElementBlock("p", _hoisted_15, " Minimum 8 characters. "))
							]),
							createBaseVNode("div", _hoisted_16, [
								createVNode(unref(Label_default), { for: "p-password-confirmation" }, {
									default: withCtx(() => [..._cache[22] || (_cache[22] = [createTextVNode("Confirm Password", -1)])]),
									_: 1
								}),
								createVNode(unref(Input_default), {
									id: "p-password-confirmation",
									type: "password",
									modelValue: unref(form).password_confirmation,
									"onUpdate:modelValue": _cache[8] || (_cache[8] = ($event) => unref(form).password_confirmation = $event),
									onChange: _cache[9] || (_cache[9] = ($event) => unref(form).validate("password_confirmation")),
									class: normalizeClass({
										"border-green-500": unref(form).valid("password_confirmation"),
										"border-red-500": unref(form).invalid("password_confirmation")
									})
								}, null, 8, ["modelValue", "class"]),
								unref(form).invalid("password_confirmation") ? (openBlock(), createElementBlock("p", _hoisted_17, toDisplayString(unref(form).errors.password_confirmation), 1)) : createCommentVNode("", true)
							]),
							createBaseVNode("div", _hoisted_18, [createVNode(unref(Button_default), {
								type: "submit",
								disabled: unref(form).processing || unref(form).validating
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(form).processing ? "Creating..." : unref(form).validating ? "Validating..." : "Create Account"), 1)]),
								_: 1
							}, 8, ["disabled"])])
						], 32)]),
						_: 1
					}), createBaseVNode("div", _hoisted_19, [
						createVNode(FeatureCard_default, {
							"info-card": "",
							title: "Precognition State",
							description: "Real-time state from the precognition validation system."
						}, {
							default: withCtx(() => [createBaseVNode("div", _hoisted_20, [
								createBaseVNode("div", _hoisted_21, [_cache[23] || (_cache[23] = createBaseVNode("span", { class: "text-sm font-medium" }, "validating", -1)), createVNode(unref(Badge_default), { variant: unref(form).validating ? "default" : "secondary" }, {
									default: withCtx(() => [createTextVNode(toDisplayString(unref(form).validating), 1)]),
									_: 1
								}, 8, ["variant"])]),
								createBaseVNode("div", _hoisted_22, [_cache[24] || (_cache[24] = createBaseVNode("span", { class: "text-sm font-medium" }, "hasErrors", -1)), createVNode(unref(Badge_default), { variant: unref(form).hasErrors ? "destructive" : "secondary" }, {
									default: withCtx(() => [createTextVNode(toDisplayString(unref(form).hasErrors), 1)]),
									_: 1
								}, 8, ["variant"])]),
								createBaseVNode("div", _hoisted_23, [_cache[25] || (_cache[25] = createBaseVNode("span", { class: "text-sm font-medium" }, "processing", -1)), createVNode(unref(Badge_default), { variant: unref(form).processing ? "default" : "secondary" }, {
									default: withCtx(() => [createTextVNode(toDisplayString(unref(form).processing), 1)]),
									_: 1
								}, 8, ["variant"])])
							])]),
							_: 1
						}),
						createVNode(FeatureCard_default, {
							"info-card": "",
							title: "Field Status",
							description: "Per-field touched, valid, and invalid state."
						}, {
							default: withCtx(() => [createBaseVNode("div", _hoisted_24, [(openBlock(), createElementBlock(Fragment, null, renderList([
								"username",
								"email",
								"password",
								"password_confirmation"
							], (field) => {
								return createBaseVNode("div", {
									key: field,
									class: "flex items-center justify-between text-sm"
								}, [createBaseVNode("span", _hoisted_25, toDisplayString(field), 1), createBaseVNode("div", _hoisted_26, [
									unref(form).touched(field) ? (openBlock(), createBlock(unref(Badge_default), {
										key: 0,
										variant: "outline",
										class: "text-xs"
									}, {
										default: withCtx(() => [..._cache[26] || (_cache[26] = [createTextVNode("touched", -1)])]),
										_: 1
									})) : createCommentVNode("", true),
									unref(form).valid(field) ? (openBlock(), createBlock(unref(Badge_default), {
										key: 1,
										variant: "default",
										class: "bg-green-600 text-xs"
									}, {
										default: withCtx(() => [..._cache[27] || (_cache[27] = [createTextVNode("valid", -1)])]),
										_: 1
									})) : createCommentVNode("", true),
									unref(form).invalid(field) ? (openBlock(), createBlock(unref(Badge_default), {
										key: 2,
										variant: "destructive",
										class: "text-xs"
									}, {
										default: withCtx(() => [..._cache[28] || (_cache[28] = [createTextVNode("invalid", -1)])]),
										_: 1
									})) : createCommentVNode("", true)
								])]);
							}), 64))])]),
							_: 1
						}),
						createVNode(FeatureCard_default, {
							"info-card": "",
							title: "Actions",
							description: "validate() only triggers a precognitive request when the field's value differs from its default. Unchanged fields are skipped."
						}, {
							default: withCtx(() => [createBaseVNode("div", _hoisted_27, [
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[11] || (_cache[11] = ($event) => unref(form).touch("username"))
								}, {
									default: withCtx(() => [..._cache[29] || (_cache[29] = [createTextVNode("touch('username')", -1)])]),
									_: 1
								}),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[12] || (_cache[12] = ($event) => unref(form).validate("username"))
								}, {
									default: withCtx(() => [..._cache[30] || (_cache[30] = [createTextVNode("validate('username')", -1)])]),
									_: 1
								}),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[13] || (_cache[13] = ($event) => unref(form).clearErrors())
								}, {
									default: withCtx(() => [..._cache[31] || (_cache[31] = [createTextVNode("clearErrors()", -1)])]),
									_: 1
								}),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[14] || (_cache[14] = ($event) => unref(form).reset())
								}, {
									default: withCtx(() => [..._cache[32] || (_cache[32] = [createTextVNode("reset()", -1)])]),
									_: 1
								})
							])]),
							_: 1
						})
					])])) : createCommentVNode("", true),
					activeTab.value === "formComponent" ? (openBlock(), createElementBlock("div", _hoisted_28, [createVNode(FeatureCard_default, { title: "Create Account (Form Component)" }, {
						description: withCtx(() => [..._cache[33] || (_cache[33] = [
							createTextVNode(" The ", -1),
							createBaseVNode("code", { class: "text-xs" }, "<Form>", -1),
							createTextVNode(" component has built-in precognition support via ", -1),
							createBaseVNode("code", { class: "text-xs" }, ":validation-timeout", -1),
							createTextVNode(" and slot props like ", -1),
							createBaseVNode("code", { class: "text-xs" }, "validate", -1),
							createTextVNode(", ", -1),
							createBaseVNode("code", { class: "text-xs" }, "valid", -1),
							createTextVNode(", ", -1),
							createBaseVNode("code", { class: "text-xs" }, "invalid", -1),
							createTextVNode(". ", -1)
						])]),
						default: withCtx(() => [createVNode(unref(form_default), {
							action: "/features/forms/precognition",
							method: "post",
							"validation-timeout": 500,
							class: "space-y-4"
						}, {
							default: withCtx(({ errors, processing, validating, validate, valid, invalid }) => [
								createBaseVNode("div", _hoisted_29, [
									createVNode(unref(Label_default), { for: "fc-username" }, {
										default: withCtx(() => [..._cache[34] || (_cache[34] = [createTextVNode("Username", -1)])]),
										_: 1
									}),
									createBaseVNode("div", _hoisted_30, [createVNode(unref(Input_default), {
										id: "fc-username",
										name: "username",
										onChange: ($event) => validate("username"),
										class: normalizeClass({
											"border-green-500": valid("username"),
											"border-red-500": invalid("username")
										})
									}, null, 8, ["onChange", "class"]), createBaseVNode("div", _hoisted_31, [valid("username") ? (openBlock(), createBlock(unref(CircleCheck), {
										key: 0,
										class: "size-4 text-green-500"
									})) : invalid("username") ? (openBlock(), createBlock(unref(CircleX), {
										key: 1,
										class: "size-4 text-red-500"
									})) : createCommentVNode("", true)])]),
									createVNode(InputError_default, { message: errors.username }, null, 8, ["message"]),
									valid("username") ? (openBlock(), createElementBlock("p", _hoisted_32, " Username available! ")) : createCommentVNode("", true)
								]),
								createBaseVNode("div", _hoisted_33, [
									createVNode(unref(Label_default), { for: "fc-email" }, {
										default: withCtx(() => [..._cache[35] || (_cache[35] = [createTextVNode("Email", -1)])]),
										_: 1
									}),
									createBaseVNode("div", _hoisted_34, [createVNode(unref(Input_default), {
										id: "fc-email",
										type: "text",
										name: "email",
										onChange: ($event) => validate("email"),
										class: normalizeClass({
											"border-green-500": valid("email"),
											"border-red-500": invalid("email")
										})
									}, null, 8, ["onChange", "class"]), createBaseVNode("div", _hoisted_35, [valid("email") ? (openBlock(), createBlock(unref(CircleCheck), {
										key: 0,
										class: "size-4 text-green-500"
									})) : invalid("email") ? (openBlock(), createBlock(unref(CircleX), {
										key: 1,
										class: "size-4 text-red-500"
									})) : createCommentVNode("", true)])]),
									createVNode(InputError_default, { message: errors.email }, null, 8, ["message"])
								]),
								createBaseVNode("div", _hoisted_36, [
									createVNode(unref(Label_default), { for: "fc-password" }, {
										default: withCtx(() => [..._cache[36] || (_cache[36] = [createTextVNode("Password", -1)])]),
										_: 1
									}),
									createVNode(unref(Input_default), {
										id: "fc-password",
										type: "password",
										name: "password",
										onChange: ($event) => validate("password"),
										class: normalizeClass({
											"border-green-500": valid("password"),
											"border-red-500": invalid("password")
										})
									}, null, 8, ["onChange", "class"]),
									createVNode(InputError_default, { message: errors.password }, null, 8, ["message"])
								]),
								createBaseVNode("div", _hoisted_37, [
									createVNode(unref(Label_default), { for: "fc-password-confirm" }, {
										default: withCtx(() => [..._cache[37] || (_cache[37] = [createTextVNode("Confirm Password", -1)])]),
										_: 1
									}),
									createVNode(unref(Input_default), {
										id: "fc-password-confirm",
										type: "password",
										name: "password_confirmation",
										onChange: ($event) => validate("password_confirmation"),
										class: normalizeClass({
											"border-green-500": valid("password_confirmation"),
											"border-red-500": invalid("password_confirmation")
										})
									}, null, 8, ["onChange", "class"]),
									createVNode(InputError_default, { message: errors.password_confirmation }, null, 8, ["message"])
								]),
								createBaseVNode("div", _hoisted_38, [createVNode(unref(Button_default), {
									type: "submit",
									disabled: processing || validating
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(processing ? "Creating..." : validating ? "Validating..." : "Create Account"), 1)]),
									_: 2
								}, 1032, ["disabled"])])
							]),
							_: 1
						})]),
						_: 1
					}), createBaseVNode("div", _hoisted_39, [createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Form Component vs useForm"
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_40, [
							createVNode(CodeBlock_default, { title: "Form Component" }, {
								default: withCtx(() => [..._cache[38] || (_cache[38] = [createBaseVNode("textarea", null, "                                <Form\n                                  action=\"/endpoint\"\n                                  method=\"post\"\n                                  :validation-timeout=\"500\"\n                                  #default=\"{ validate, valid, invalid, ... }\"\n                                >\n                                ", -1)])]),
								_: 1
							}),
							createVNode(CodeBlock_default, {
								title: "useForm",
								code: "\n                                const form = useForm('post', '/endpoint', { ... })\n                                form.setValidationTimeout(500)\n                                // form.validate(), form.valid(), etc.\n                            "
							}),
							_cache[39] || (_cache[39] = createBaseVNode("p", null, [
								createTextVNode(" Both approaches provide the same precognition features. The "),
								createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "<Form>"),
								createTextVNode(" component uses native HTML inputs and exposes validation via slot props, while "),
								createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "useForm"),
								createTextVNode(" uses "),
								createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "v-model"),
								createTextVNode(" bindings. ")
							], -1))
						])]),
						_: 1
					})])])) : createCommentVNode("", true)
				])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { Precognition_default as default };

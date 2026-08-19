import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, I as vModelText, Mn as withCtx, Pn as withDirectives, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, it as createCommentVNode, l as useForm, mt as defineComponent, nt as createBaseVNode, pr as unref, yr as toDisplayString, z as withModifiers } from "./dist.js";
import { t as InputError_default } from "./InputError.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Label_default } from "./Label.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Input_default } from "./Input.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Forms/Validation.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-2" };
var _hoisted_4 = { class: "space-y-2" };
var _hoisted_5 = { class: "space-y-2" };
var _hoisted_6 = { class: "space-y-2" };
var _hoisted_7 = { class: "flex items-center gap-2 pt-2" };
var _hoisted_8 = {
	key: 0,
	class: "mt-4 space-y-2"
};
var _hoisted_9 = { class: "space-y-2" };
var _hoisted_10 = { class: "space-y-2" };
var _hoisted_11 = {
	key: 0,
	class: "mt-4 space-y-2"
};
var _hoisted_12 = { class: "flex flex-wrap gap-2" };
var _hoisted_13 = { class: "mt-4 grid gap-4 sm:grid-cols-2" };
var _hoisted_14 = { class: "space-y-2" };
var _hoisted_15 = { class: "flex items-center justify-between text-sm" };
var _hoisted_16 = { class: "flex items-center justify-between text-sm" };
var _hoisted_17 = { class: "space-y-2" };
var _hoisted_18 = { class: "flex items-center justify-between text-sm" };
var _hoisted_19 = { class: "flex items-center justify-between text-sm" };
//#endregion
//#region resources/js/pages/Features/Forms/Validation.vue
var Validation_default = /* @__PURE__ */ defineComponent({
	__name: "Validation",
	setup(__props) {
		const breadcrumbs = [{ title: "Forms" }, { title: "Validation" }];
		const primaryForm = useForm({
			name: "",
			email: "",
			age: "",
			website: ""
		});
		function submitPrimary() {
			primaryForm.submit("post", "/features/forms/validation", { preserveScroll: true });
		}
		const secondaryForm = useForm({
			title: "",
			body: ""
		});
		function submitSecondary() {
			secondaryForm.submit("post", "/features/forms/validation/secondary", {
				preserveScroll: true,
				errorBag: "secondaryForm"
			});
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Validation" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Validation",
					docs: "the-basics/validation",
					controller: "app/Http/Controllers/Feature/FormController.php#L56"
				}, {
					default: withCtx(() => [..._cache[12] || (_cache[12] = [createTextVNode(" Error handling patterns. Server-side errors, error bags, and manual error management. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						title: "Server-Side Validation",
						description: "Submit an empty form to see server validation errors with custom messages."
					}, {
						default: withCtx(() => [createBaseVNode("form", {
							class: "space-y-4",
							onSubmit: withModifiers(submitPrimary, ["prevent"])
						}, [
							createBaseVNode("div", _hoisted_3, [
								createVNode(unref(Label_default), { for: "v-name" }, {
									default: withCtx(() => [..._cache[13] || (_cache[13] = [createTextVNode("Name", -1)])]),
									_: 1
								}),
								createVNode(unref(Input_default), {
									id: "v-name",
									modelValue: unref(primaryForm).name,
									"onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => unref(primaryForm).name = $event)
								}, null, 8, ["modelValue"]),
								createVNode(InputError_default, { message: unref(primaryForm).errors.name }, null, 8, ["message"])
							]),
							createBaseVNode("div", _hoisted_4, [
								createVNode(unref(Label_default), { for: "v-email" }, {
									default: withCtx(() => [..._cache[14] || (_cache[14] = [createTextVNode("Email", -1)])]),
									_: 1
								}),
								createVNode(unref(Input_default), {
									id: "v-email",
									type: "text",
									modelValue: unref(primaryForm).email,
									"onUpdate:modelValue": _cache[1] || (_cache[1] = ($event) => unref(primaryForm).email = $event)
								}, null, 8, ["modelValue"]),
								createVNode(InputError_default, { message: unref(primaryForm).errors.email }, null, 8, ["message"])
							]),
							createBaseVNode("div", _hoisted_5, [
								createVNode(unref(Label_default), { for: "v-age" }, {
									default: withCtx(() => [..._cache[15] || (_cache[15] = [createTextVNode("Age", -1)])]),
									_: 1
								}),
								createVNode(unref(Input_default), {
									id: "v-age",
									type: "number",
									modelValue: unref(primaryForm).age,
									"onUpdate:modelValue": _cache[2] || (_cache[2] = ($event) => unref(primaryForm).age = $event)
								}, null, 8, ["modelValue"]),
								createVNode(InputError_default, { message: unref(primaryForm).errors.age }, null, 8, ["message"])
							]),
							createBaseVNode("div", _hoisted_6, [
								createVNode(unref(Label_default), { for: "v-website" }, {
									default: withCtx(() => [..._cache[16] || (_cache[16] = [createTextVNode("Website", -1)])]),
									_: 1
								}),
								createVNode(unref(Input_default), {
									id: "v-website",
									modelValue: unref(primaryForm).website,
									"onUpdate:modelValue": _cache[3] || (_cache[3] = ($event) => unref(primaryForm).website = $event),
									placeholder: "https://example.com"
								}, null, 8, ["modelValue"]),
								createVNode(InputError_default, { message: unref(primaryForm).errors.website }, null, 8, ["message"])
							]),
							createBaseVNode("div", _hoisted_7, [createVNode(unref(Button_default), {
								type: "submit",
								disabled: unref(primaryForm).processing
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(primaryForm).processing ? "Validating..." : "Submit"), 1)]),
								_: 1
							}, 8, ["disabled"]), createVNode(unref(Button_default), {
								type: "button",
								variant: "outline",
								onClick: _cache[4] || (_cache[4] = ($event) => unref(primaryForm).clearErrors())
							}, {
								default: withCtx(() => [..._cache[17] || (_cache[17] = [createTextVNode(" clearErrors() ", -1)])]),
								_: 1
							})])
						], 32), unref(primaryForm).hasErrors ? (openBlock(), createElementBlock("div", _hoisted_8, [_cache[18] || (_cache[18] = createBaseVNode("h4", { class: "text-sm font-semibold" }, "form.errors", -1)), createVNode(CodeBlock_default, { code: JSON.stringify(unref(primaryForm).errors, null, 2) }, null, 8, ["code"])])) : createCommentVNode("", true)]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "Error Bags" }, {
						description: withCtx(() => [..._cache[19] || (_cache[19] = [
							createTextVNode(" When multiple forms share field names, validation errors can bleed between them. The ", -1),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "errorBag", -1),
							createTextVNode(" option scopes errors under a unique key (", -1),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "page.props.errors.secondaryForm", -1),
							createTextVNode(") so each form only displays its own errors. If the backend uses ", -1),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "validateWithBag()", -1),
							createTextVNode(", you must specify a matching ", -1),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "errorBag", -1),
							createTextVNode(" on the frontend so Inertia knows where to find the errors. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("form", {
							class: "space-y-4",
							onSubmit: withModifiers(submitSecondary, ["prevent"])
						}, [
							createBaseVNode("div", _hoisted_9, [
								createVNode(unref(Label_default), { for: "v2-title" }, {
									default: withCtx(() => [..._cache[20] || (_cache[20] = [createTextVNode("Title", -1)])]),
									_: 1
								}),
								createVNode(unref(Input_default), {
									id: "v2-title",
									modelValue: unref(secondaryForm).title,
									"onUpdate:modelValue": _cache[5] || (_cache[5] = ($event) => unref(secondaryForm).title = $event)
								}, null, 8, ["modelValue"]),
								createVNode(InputError_default, { message: unref(secondaryForm).errors.title }, null, 8, ["message"])
							]),
							createBaseVNode("div", _hoisted_10, [
								createVNode(unref(Label_default), { for: "v2-body" }, {
									default: withCtx(() => [..._cache[21] || (_cache[21] = [createTextVNode("Body (min 10 characters)", -1)])]),
									_: 1
								}),
								withDirectives(createBaseVNode("textarea", {
									id: "v2-body",
									"onUpdate:modelValue": _cache[6] || (_cache[6] = ($event) => unref(secondaryForm).body = $event),
									rows: "3",
									class: "flex w-full rounded-md border border-input/60 bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:ring-1 focus-visible:ring-ring focus-visible:outline-none"
								}, null, 512), [[vModelText, unref(secondaryForm).body]]),
								createVNode(InputError_default, { message: unref(secondaryForm).errors.body }, null, 8, ["message"])
							]),
							createVNode(unref(Button_default), {
								type: "submit",
								disabled: unref(secondaryForm).processing
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(secondaryForm).processing ? "Validating..." : "Submit (Error Bag)"), 1)]),
								_: 1
							}, 8, ["disabled"])
						], 32), unref(secondaryForm).hasErrors ? (openBlock(), createElementBlock("div", _hoisted_11, [_cache[22] || (_cache[22] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " secondaryForm.errors ", -1)), createVNode(CodeBlock_default, { code: JSON.stringify(unref(secondaryForm).errors, null, 2) }, null, 8, ["code"])])) : createCommentVNode("", true)]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "Manual Error Management",
						description: "Use setError() and clearErrors() to manage errors programmatically."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_12, [
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[7] || (_cache[7] = ($event) => unref(primaryForm).setError("name", "This name is already taken"))
							}, {
								default: withCtx(() => [..._cache[23] || (_cache[23] = [createTextVNode(" setError('name', ...) ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[8] || (_cache[8] = ($event) => unref(primaryForm).setError({
									email: "Email is blocked",
									website: "Domain not allowed"
								}))
							}, {
								default: withCtx(() => [..._cache[24] || (_cache[24] = [createTextVNode(" setError({ email, website }) ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[9] || (_cache[9] = ($event) => unref(primaryForm).clearErrors("name"))
							}, {
								default: withCtx(() => [..._cache[25] || (_cache[25] = [createTextVNode(" clearErrors('name') ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[10] || (_cache[10] = ($event) => unref(primaryForm).clearErrors("email", "website"))
							}, {
								default: withCtx(() => [..._cache[26] || (_cache[26] = [createTextVNode(" clearErrors('email', 'website') ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[11] || (_cache[11] = ($event) => unref(primaryForm).clearErrors())
							}, {
								default: withCtx(() => [..._cache[27] || (_cache[27] = [createTextVNode(" clearErrors() (all) ", -1)])]),
								_: 1
							})
						]), createBaseVNode("div", _hoisted_13, [createBaseVNode("div", null, [_cache[30] || (_cache[30] = createBaseVNode("h4", { class: "mb-2 text-sm font-semibold" }, " Primary Form State ", -1)), createBaseVNode("div", _hoisted_14, [createBaseVNode("div", _hoisted_15, [_cache[28] || (_cache[28] = createBaseVNode("span", null, "hasErrors", -1)), createVNode(unref(Badge_default), { variant: unref(primaryForm).hasErrors ? "destructive" : "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(unref(primaryForm).hasErrors), 1)]),
							_: 1
						}, 8, ["variant"])]), createBaseVNode("div", _hoisted_16, [_cache[29] || (_cache[29] = createBaseVNode("span", null, "isDirty", -1)), createVNode(unref(Badge_default), { variant: unref(primaryForm).isDirty ? "default" : "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(unref(primaryForm).isDirty), 1)]),
							_: 1
						}, 8, ["variant"])])])]), createBaseVNode("div", null, [_cache[33] || (_cache[33] = createBaseVNode("h4", { class: "mb-2 text-sm font-semibold" }, " Secondary Form State ", -1)), createBaseVNode("div", _hoisted_17, [createBaseVNode("div", _hoisted_18, [_cache[31] || (_cache[31] = createBaseVNode("span", null, "hasErrors", -1)), createVNode(unref(Badge_default), { variant: unref(secondaryForm).hasErrors ? "destructive" : "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(unref(secondaryForm).hasErrors), 1)]),
							_: 1
						}, 8, ["variant"])]), createBaseVNode("div", _hoisted_19, [_cache[32] || (_cache[32] = createBaseVNode("span", null, "isDirty", -1)), createVNode(unref(Badge_default), { variant: unref(secondaryForm).isDirty ? "default" : "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(unref(secondaryForm).isDirty), 1)]),
							_: 1
						}, 8, ["variant"])])])])])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { Validation_default as default };

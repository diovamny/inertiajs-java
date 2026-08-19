import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, F as vModelSelect, G as Fragment, I as vModelText, M as vModelCheckbox, Mn as withCtx, Pn as withDirectives, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, it as createCommentVNode, l as useForm, mt as defineComponent, nt as createBaseVNode, pr as unref, rt as createBlock, vr as normalizeStyle, yr as toDisplayString, z as withModifiers } from "./dist.js";
import { t as InputError_default } from "./InputError.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Label_default } from "./Label.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Input_default } from "./Input.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Forms/UseForm.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-2" };
var _hoisted_4 = { class: "space-y-2" };
var _hoisted_5 = { class: "space-y-2" };
var _hoisted_6 = { class: "space-y-2" };
var _hoisted_7 = { class: "flex items-center gap-2" };
var _hoisted_8 = {
	key: 0,
	class: "w-full rounded-full bg-secondary"
};
var _hoisted_9 = { class: "flex flex-wrap items-center gap-2 pt-2" };
var _hoisted_10 = { class: "space-y-6" };
var _hoisted_11 = { class: "space-y-3" };
var _hoisted_12 = { class: "flex items-center justify-between" };
var _hoisted_13 = { class: "flex items-center justify-between" };
var _hoisted_14 = { class: "flex items-center justify-between" };
var _hoisted_15 = { class: "flex items-center justify-between" };
var _hoisted_16 = { class: "flex items-center justify-between" };
var _hoisted_17 = { class: "flex items-center justify-between" };
var _hoisted_18 = { class: "flex flex-wrap gap-2" };
//#endregion
//#region resources/js/pages/Features/Forms/UseForm.vue
var UseForm_default = /* @__PURE__ */ defineComponent({
	__name: "UseForm",
	setup(__props) {
		const breadcrumbs = [{ title: "Forms" }, { title: "useForm" }];
		const form = useForm("use-form-demo", {
			name: "",
			email: "",
			bio: "",
			role: "developer",
			subscribe: false
		});
		function submit() {
			form.submit("post", "/features/forms/use-form", {
				preserveScroll: true,
				onSuccess: () => form.defaults()
			});
		}
		function submitWithTransform() {
			form.transform((data) => ({
				...data,
				name: data.name.toUpperCase()
			})).submit("post", "/features/forms/use-form", { preserveScroll: true });
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "useForm" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "useForm",
					docs: "the-basics/forms#form-helper",
					controller: "app/Http/Controllers/Feature/FormController.php#L22"
				}, {
					default: withCtx(() => [..._cache[10] || (_cache[10] = [createTextVNode(" Complete useForm API demonstration with all reactive state and methods. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [createVNode(FeatureCard_default, {
					title: "Demo Form",
					description: "Fill out the form to see reactive state changes in real time."
				}, {
					default: withCtx(() => [createBaseVNode("form", {
						class: "space-y-4",
						onSubmit: withModifiers(submit, ["prevent"])
					}, [
						createBaseVNode("div", _hoisted_3, [
							createVNode(unref(Label_default), { for: "name" }, {
								default: withCtx(() => [..._cache[11] || (_cache[11] = [createTextVNode("Name", -1)])]),
								_: 1
							}),
							createVNode(unref(Input_default), {
								id: "name",
								modelValue: unref(form).name,
								"onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => unref(form).name = $event)
							}, null, 8, ["modelValue"]),
							createVNode(InputError_default, { message: unref(form).errors.name }, null, 8, ["message"])
						]),
						createBaseVNode("div", _hoisted_4, [
							createVNode(unref(Label_default), { for: "email" }, {
								default: withCtx(() => [..._cache[12] || (_cache[12] = [createTextVNode("Email", -1)])]),
								_: 1
							}),
							createVNode(unref(Input_default), {
								id: "email",
								type: "text",
								modelValue: unref(form).email,
								"onUpdate:modelValue": _cache[1] || (_cache[1] = ($event) => unref(form).email = $event)
							}, null, 8, ["modelValue"]),
							createVNode(InputError_default, { message: unref(form).errors.email }, null, 8, ["message"])
						]),
						createBaseVNode("div", _hoisted_5, [
							createVNode(unref(Label_default), { for: "bio" }, {
								default: withCtx(() => [..._cache[13] || (_cache[13] = [createTextVNode("Bio", -1)])]),
								_: 1
							}),
							withDirectives(createBaseVNode("textarea", {
								id: "bio",
								"onUpdate:modelValue": _cache[2] || (_cache[2] = ($event) => unref(form).bio = $event),
								rows: "3",
								class: "flex w-full rounded-md border border-input/60 bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:ring-1 focus-visible:ring-ring focus-visible:outline-none",
								placeholder: "Tell us about yourself..."
							}, null, 512), [[vModelText, unref(form).bio]]),
							createVNode(InputError_default, { message: unref(form).errors.bio }, null, 8, ["message"])
						]),
						createBaseVNode("div", _hoisted_6, [
							createVNode(unref(Label_default), { for: "role" }, {
								default: withCtx(() => [..._cache[14] || (_cache[14] = [createTextVNode("Role", -1)])]),
								_: 1
							}),
							withDirectives(createBaseVNode("select", {
								id: "role",
								"onUpdate:modelValue": _cache[3] || (_cache[3] = ($event) => unref(form).role = $event),
								class: "flex h-9 w-full rounded-md border border-input/60 bg-background px-3 py-1 text-sm focus-visible:ring-1 focus-visible:ring-ring focus-visible:outline-none"
							}, [..._cache[15] || (_cache[15] = [
								createBaseVNode("option", { value: "developer" }, "Developer", -1),
								createBaseVNode("option", { value: "designer" }, "Designer", -1),
								createBaseVNode("option", { value: "manager" }, "Manager", -1),
								createBaseVNode("option", { value: "other" }, "Other", -1)
							])], 512), [[vModelSelect, unref(form).role]]),
							createVNode(InputError_default, { message: unref(form).errors.role }, null, 8, ["message"])
						]),
						createBaseVNode("div", _hoisted_7, [withDirectives(createBaseVNode("input", {
							id: "subscribe",
							type: "checkbox",
							"onUpdate:modelValue": _cache[4] || (_cache[4] = ($event) => unref(form).subscribe = $event),
							class: "size-4 rounded border"
						}, null, 512), [[vModelCheckbox, unref(form).subscribe]]), createVNode(unref(Label_default), { for: "subscribe" }, {
							default: withCtx(() => [..._cache[16] || (_cache[16] = [createTextVNode("Subscribe to newsletter", -1)])]),
							_: 1
						})]),
						unref(form).progress ? (openBlock(), createElementBlock("div", _hoisted_8, [createBaseVNode("div", {
							class: "h-2 rounded-full bg-primary transition-all",
							style: normalizeStyle({ width: `${unref(form).progress.percentage}%` })
						}, null, 4)])) : createCommentVNode("", true),
						createBaseVNode("div", _hoisted_9, [createVNode(unref(Button_default), {
							type: "submit",
							disabled: unref(form).processing
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(unref(form).processing ? "Submitting..." : "Submit"), 1)]),
							_: 1
						}, 8, ["disabled"]), createVNode(unref(Button_default), {
							type: "button",
							variant: "secondary",
							onClick: submitWithTransform
						}, {
							default: withCtx(() => [..._cache[17] || (_cache[17] = [createTextVNode(" Submit with Transform ", -1)])]),
							_: 1
						})])
					], 32)]),
					_: 1
				}), createBaseVNode("div", _hoisted_10, [
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Reactive State",
						description: "These values update in real time as you interact with the form."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_11, [
							createBaseVNode("div", _hoisted_12, [_cache[18] || (_cache[18] = createBaseVNode("span", { class: "text-sm font-medium" }, "processing", -1)), createVNode(unref(Badge_default), { variant: unref(form).processing ? "default" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(form).processing), 1)]),
								_: 1
							}, 8, ["variant"])]),
							createBaseVNode("div", _hoisted_13, [_cache[19] || (_cache[19] = createBaseVNode("span", { class: "text-sm font-medium" }, "isDirty", -1)), createVNode(unref(Badge_default), { variant: unref(form).isDirty ? "default" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(form).isDirty), 1)]),
								_: 1
							}, 8, ["variant"])]),
							createBaseVNode("div", _hoisted_14, [_cache[20] || (_cache[20] = createBaseVNode("span", { class: "text-sm font-medium" }, "hasErrors", -1)), createVNode(unref(Badge_default), { variant: unref(form).hasErrors ? "destructive" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(form).hasErrors), 1)]),
								_: 1
							}, 8, ["variant"])]),
							createBaseVNode("div", _hoisted_15, [_cache[21] || (_cache[21] = createBaseVNode("span", { class: "text-sm font-medium" }, "wasSuccessful", -1)), createVNode(unref(Badge_default), { variant: unref(form).wasSuccessful ? "default" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(form).wasSuccessful), 1)]),
								_: 1
							}, 8, ["variant"])]),
							createBaseVNode("div", _hoisted_16, [_cache[22] || (_cache[22] = createBaseVNode("span", { class: "text-sm font-medium" }, "recentlySuccessful", -1)), createVNode(unref(Badge_default), { variant: unref(form).recentlySuccessful ? "default" : "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(form).recentlySuccessful), 1)]),
								_: 1
							}, 8, ["variant"])]),
							createBaseVNode("div", _hoisted_17, [_cache[23] || (_cache[23] = createBaseVNode("span", { class: "text-sm font-medium" }, "progress", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(unref(form).progress?.percentage ?? "null"), 1)]),
								_: 1
							})])
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Form Data",
						description: "Current form values that will be submitted."
					}, {
						default: withCtx(() => [createVNode(CodeBlock_default, { code: JSON.stringify(unref(form).data(), null, 2) }, null, 8, ["code"])]),
						_: 1
					}),
					unref(form).hasErrors ? (openBlock(), createBlock(FeatureCard_default, {
						key: 0,
						"info-card": "",
						title: "Errors"
					}, {
						default: withCtx(() => [createVNode(CodeBlock_default, { code: JSON.stringify(unref(form).errors, null, 2) }, null, 8, ["code"])]),
						_: 1
					})) : createCommentVNode("", true),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Actions",
						description: "Methods available on the useForm instance."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_18, [
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[5] || (_cache[5] = ($event) => unref(form).reset())
							}, {
								default: withCtx(() => [..._cache[24] || (_cache[24] = [createTextVNode(" reset() ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[6] || (_cache[6] = ($event) => unref(form).clearErrors())
							}, {
								default: withCtx(() => [..._cache[25] || (_cache[25] = [createTextVNode(" clearErrors() ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[7] || (_cache[7] = ($event) => unref(form).cancel())
							}, {
								default: withCtx(() => [..._cache[26] || (_cache[26] = [createTextVNode(" cancel() ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[8] || (_cache[8] = ($event) => unref(form).defaults())
							}, {
								default: withCtx(() => [..._cache[27] || (_cache[27] = [createTextVNode(" defaults() ", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[9] || (_cache[9] = ($event) => unref(form).setError("name", "This is a manual error"))
							}, {
								default: withCtx(() => [..._cache[28] || (_cache[28] = [createTextVNode(" setError('name', ...) ", -1)])]),
								_: 1
							})
						])]),
						_: 1
					})
				])])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { UseForm_default as default };

import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, M as vModelCheckbox, Mn as withCtx, Pn as withDirectives, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, pr as unref, r as form_default, rr as ref, vr as normalizeStyle, yr as toDisplayString } from "./dist.js";
import { t as InputError_default } from "./InputError.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Label_default } from "./Label.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Input_default } from "./Input.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Forms/FormComponent.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "mb-4 flex flex-wrap gap-4 rounded-md bg-muted p-3" };
var _hoisted_4 = { class: "flex items-center gap-2 text-sm" };
var _hoisted_5 = { class: "flex items-center gap-2 text-sm" };
var _hoisted_6 = { class: "space-y-2" };
var _hoisted_7 = { class: "space-y-2" };
var _hoisted_8 = { class: "space-y-2" };
var _hoisted_9 = { class: "space-y-2" };
var _hoisted_10 = { class: "flex items-center gap-2" };
var _hoisted_11 = {
	key: 0,
	class: "w-full rounded-full bg-secondary"
};
var _hoisted_12 = { class: "flex items-center gap-2 pt-2" };
var _hoisted_13 = {
	key: 0,
	class: "text-sm text-muted-foreground"
};
var _hoisted_14 = {
	key: 1,
	class: "text-sm text-green-600"
};
var _hoisted_15 = { class: "space-y-3 rounded-md border border-black/10 p-4 dark:border-white/10" };
var _hoisted_16 = { class: "flex flex-wrap gap-2" };
var _hoisted_17 = { class: "flex flex-col gap-6" };
var _hoisted_18 = { class: "grid grid-cols-2 gap-2 text-sm" };
var _hoisted_19 = { class: "flex items-center justify-between" };
var _hoisted_20 = { class: "flex items-center justify-between" };
var _hoisted_21 = { class: "flex items-center justify-between" };
var _hoisted_22 = { class: "flex items-center justify-between" };
var _hoisted_23 = { class: "flex items-center justify-between" };
var _hoisted_24 = { class: "flex items-center justify-between" };
var _hoisted_25 = { class: "space-y-4" };
var _hoisted_26 = { class: "flex flex-wrap gap-2" };
//#endregion
//#region resources/js/pages/Features/Forms/FormComponent.vue
var FormComponent_default = /* @__PURE__ */ defineComponent({
	__name: "FormComponent",
	setup(__props) {
		const breadcrumbs = [{ title: "Forms" }, { title: "Form Component" }];
		const formRef = ref(null);
		const resetOnSuccess = ref(false);
		const setDefaultsOnSuccess = ref(true);
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Form Component" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Form Component",
					docs: "the-basics/forms#form-component",
					controller: "app/Http/Controllers/Feature/FormController.php#L32"
				}, {
					default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" Template-based <Form> component with slot props. No useForm composable needed. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [createVNode(FeatureCard_default, { title: "Demo Form" }, {
					description: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" Uses the <Form> component with native HTML inputs and slot props. ", -1)])]),
					default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createBaseVNode("label", _hoisted_4, [withDirectives(createBaseVNode("input", {
						type: "checkbox",
						"onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => resetOnSuccess.value = $event),
						class: "size-4 rounded border"
					}, null, 512), [[vModelCheckbox, resetOnSuccess.value]]), _cache[7] || (_cache[7] = createTextVNode(" resetOnSuccess ", -1))]), createBaseVNode("label", _hoisted_5, [withDirectives(createBaseVNode("input", {
						type: "checkbox",
						"onUpdate:modelValue": _cache[1] || (_cache[1] = ($event) => setDefaultsOnSuccess.value = $event),
						class: "size-4 rounded border"
					}, null, 512), [[vModelCheckbox, setDefaultsOnSuccess.value]]), _cache[8] || (_cache[8] = createTextVNode(" setDefaultsOnSuccess ", -1))])]), createVNode(unref(form_default), {
						ref_key: "formRef",
						ref: formRef,
						action: "/features/forms/form-component",
						method: "post",
						"reset-on-success": resetOnSuccess.value,
						"set-defaults-on-success": setDefaultsOnSuccess.value,
						class: "space-y-4"
					}, {
						default: withCtx(({ errors, processing, isDirty, recentlySuccessful, progress, submit, reset, clearErrors, setError }) => [
							createBaseVNode("div", _hoisted_6, [
								createVNode(unref(Label_default), { for: "fc-name" }, {
									default: withCtx(() => [..._cache[9] || (_cache[9] = [createTextVNode("Name", -1)])]),
									_: 1
								}),
								createVNode(unref(Input_default), {
									id: "fc-name",
									name: "name"
								}),
								createVNode(InputError_default, { message: errors.name }, null, 8, ["message"])
							]),
							createBaseVNode("div", _hoisted_7, [
								createVNode(unref(Label_default), { for: "fc-email" }, {
									default: withCtx(() => [..._cache[10] || (_cache[10] = [createTextVNode("Email", -1)])]),
									_: 1
								}),
								createVNode(unref(Input_default), {
									id: "fc-email",
									type: "text",
									name: "email"
								}),
								createVNode(InputError_default, { message: errors.email }, null, 8, ["message"])
							]),
							createBaseVNode("div", _hoisted_8, [
								createVNode(unref(Label_default), { for: "fc-bio" }, {
									default: withCtx(() => [..._cache[11] || (_cache[11] = [createTextVNode("Bio", -1)])]),
									_: 1
								}),
								_cache[12] || (_cache[12] = createBaseVNode("textarea", {
									id: "fc-bio",
									name: "bio",
									rows: "3",
									class: "flex w-full rounded-md border border-input/60 bg-background px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:ring-1 focus-visible:ring-ring focus-visible:outline-none",
									placeholder: "Tell us about yourself..."
								}, null, -1)),
								createVNode(InputError_default, { message: errors.bio }, null, 8, ["message"])
							]),
							createBaseVNode("div", _hoisted_9, [
								createVNode(unref(Label_default), { for: "fc-role" }, {
									default: withCtx(() => [..._cache[13] || (_cache[13] = [createTextVNode("Role", -1)])]),
									_: 1
								}),
								_cache[14] || (_cache[14] = createBaseVNode("select", {
									id: "fc-role",
									name: "role",
									class: "flex h-9 w-full rounded-md border border-input/60 bg-background px-3 py-1 text-sm focus-visible:ring-1 focus-visible:ring-ring focus-visible:outline-none"
								}, [
									createBaseVNode("option", { value: "developer" }, "Developer"),
									createBaseVNode("option", { value: "designer" }, "Designer"),
									createBaseVNode("option", { value: "manager" }, "Manager"),
									createBaseVNode("option", { value: "other" }, "Other")
								], -1)),
								createVNode(InputError_default, { message: errors.role }, null, 8, ["message"])
							]),
							createBaseVNode("div", _hoisted_10, [_cache[16] || (_cache[16] = createBaseVNode("input", {
								id: "fc-subscribe",
								type: "checkbox",
								name: "subscribe",
								value: true,
								class: "size-4 rounded border"
							}, null, -1)), createVNode(unref(Label_default), { for: "fc-subscribe" }, {
								default: withCtx(() => [..._cache[15] || (_cache[15] = [createTextVNode("Subscribe to newsletter", -1)])]),
								_: 1
							})]),
							progress ? (openBlock(), createElementBlock("div", _hoisted_11, [createBaseVNode("div", {
								class: "h-2 rounded-full bg-primary transition-all",
								style: normalizeStyle({ width: `${progress.percentage}%` })
							}, null, 4)])) : createCommentVNode("", true),
							createBaseVNode("div", _hoisted_12, [
								createVNode(unref(Button_default), {
									type: "submit",
									disabled: processing
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(processing ? "Submitting..." : "Submit"), 1)]),
									_: 2
								}, 1032, ["disabled"]),
								isDirty ? (openBlock(), createElementBlock("span", _hoisted_13, "Unsaved changes")) : createCommentVNode("", true),
								recentlySuccessful ? (openBlock(), createElementBlock("span", _hoisted_14, "Saved!")) : createCommentVNode("", true)
							]),
							createBaseVNode("div", _hoisted_15, [_cache[21] || (_cache[21] = createBaseVNode("h3", { class: "text-sm font-semibold" }, "Slot Methods", -1)), createBaseVNode("div", _hoisted_16, [
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: submit
								}, {
									default: withCtx(() => [..._cache[17] || (_cache[17] = [createTextVNode("submit()", -1)])]),
									_: 1
								}, 8, ["onClick"]),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: ($event) => reset()
								}, {
									default: withCtx(() => [..._cache[18] || (_cache[18] = [createTextVNode("reset()", -1)])]),
									_: 1
								}, 8, ["onClick"]),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: ($event) => clearErrors()
								}, {
									default: withCtx(() => [..._cache[19] || (_cache[19] = [createTextVNode("clearErrors()", -1)])]),
									_: 1
								}, 8, ["onClick"]),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: ($event) => setError("name", "Manual error via setError()")
								}, {
									default: withCtx(() => [..._cache[20] || (_cache[20] = [createTextVNode("setError()", -1)])]),
									_: 1
								}, 8, ["onClick"])
							])])
						]),
						_: 1
					}, 8, ["reset-on-success", "set-defaults-on-success"])]),
					_: 1
				}), createBaseVNode("div", _hoisted_17, [createVNode(FeatureCard_default, {
					"info-card": "",
					title: "Slot Props (Reactive State)",
					description: "Live state from the Form component, read via template ref."
				}, {
					default: withCtx(() => [createBaseVNode("div", _hoisted_18, [
						createBaseVNode("div", _hoisted_19, [_cache[22] || (_cache[22] = createBaseVNode("span", null, "processing", -1)), createVNode(unref(Badge_default), {
							variant: formRef.value?.processing ? "default" : "secondary",
							class: "text-xs"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(formRef.value?.processing ?? false), 1)]),
							_: 1
						}, 8, ["variant"])]),
						createBaseVNode("div", _hoisted_20, [_cache[23] || (_cache[23] = createBaseVNode("span", null, "isDirty", -1)), createVNode(unref(Badge_default), {
							variant: formRef.value?.isDirty ? "default" : "secondary",
							class: "text-xs"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(formRef.value?.isDirty ?? false), 1)]),
							_: 1
						}, 8, ["variant"])]),
						createBaseVNode("div", _hoisted_21, [_cache[24] || (_cache[24] = createBaseVNode("span", null, "hasErrors", -1)), createVNode(unref(Badge_default), {
							variant: formRef.value?.hasErrors ? "destructive" : "secondary",
							class: "text-xs"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(formRef.value?.hasErrors ?? false), 1)]),
							_: 1
						}, 8, ["variant"])]),
						createBaseVNode("div", _hoisted_22, [_cache[25] || (_cache[25] = createBaseVNode("span", null, "wasSuccessful", -1)), createVNode(unref(Badge_default), {
							variant: formRef.value?.wasSuccessful ? "default" : "secondary",
							class: "text-xs"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(formRef.value?.wasSuccessful ?? false), 1)]),
							_: 1
						}, 8, ["variant"])]),
						createBaseVNode("div", _hoisted_23, [_cache[26] || (_cache[26] = createBaseVNode("span", null, "recentlySuccessful", -1)), createVNode(unref(Badge_default), {
							variant: formRef.value?.recentlySuccessful ? "default" : "secondary",
							class: "text-xs"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(formRef.value?.recentlySuccessful ?? false), 1)]),
							_: 1
						}, 8, ["variant"])]),
						createBaseVNode("div", _hoisted_24, [_cache[27] || (_cache[27] = createBaseVNode("span", null, "progress", -1)), createVNode(unref(Badge_default), {
							variant: "secondary",
							class: "text-xs"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(formRef.value?.progress ? `${formRef.value.progress.percentage}%` : "null"), 1)]),
							_: 1
						})])
					])]),
					_: 1
				}), createVNode(FeatureCard_default, {
					"info-card": "",
					title: "Template Ref Methods",
					description: "Control the form from outside using a template ref."
				}, {
					default: withCtx(() => [createBaseVNode("div", _hoisted_25, [
						createVNode(CodeBlock_default, null, {
							default: withCtx(() => [..._cache[28] || (_cache[28] = [createBaseVNode("textarea", null, "                                    import { Form } from '@inertiajs/vue3'\n                                    import { ref } from 'vue'\n\n                                    const formRef = ref(null)\n\n                                    // Use in template: <Form ref=\"formRef\" ...>\n                                    // Then call methods from anywhere:\n                                    formRef.value?.submit()\n                                    formRef.value?.reset()\n                                    formRef.value?.clearErrors()\n                                ", -1)])]),
							_: 1
						}),
						_cache[32] || (_cache[32] = createBaseVNode("p", { class: "text-sm text-muted-foreground" }, [
							createTextVNode(" These buttons call methods via "),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "formRef.value?.method()"),
							createTextVNode(" from outside the <Form> slot scope. ")
						], -1)),
						createBaseVNode("div", _hoisted_26, [
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[2] || (_cache[2] = ($event) => formRef.value?.submit())
							}, {
								default: withCtx(() => [..._cache[29] || (_cache[29] = [createTextVNode("ref.submit()", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[3] || (_cache[3] = ($event) => formRef.value?.reset())
							}, {
								default: withCtx(() => [..._cache[30] || (_cache[30] = [createTextVNode("ref.reset()", -1)])]),
								_: 1
							}),
							createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[4] || (_cache[4] = ($event) => formRef.value?.clearErrors())
							}, {
								default: withCtx(() => [..._cache[31] || (_cache[31] = [createTextVNode("ref.clearErrors()", -1)])]),
								_: 1
							})
						])
					])]),
					_: 1
				})])])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { FormComponent_default as default };

import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, pr as unref, r as form_default, u as useFormContext, yr as toDisplayString } from "./dist.js";
import { t as InputError_default } from "./InputError.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Label_default } from "./Label.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Input_default } from "./Input.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/components/FormContextField.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1$1 = { class: "space-y-2" };
var _hoisted_2$1 = { class: "flex items-center justify-between" };
var _hoisted_3$1 = {
	key: 0,
	class: "text-xs text-muted-foreground"
};
//#endregion
//#region resources/js/components/FormContextField.vue
var FormContextField_default = /* @__PURE__ */ defineComponent({
	__name: "FormContextField",
	props: {
		name: {},
		label: {},
		type: {},
		placeholder: {}
	},
	setup(__props) {
		const form = useFormContext();
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock("div", _hoisted_1$1, [
				createBaseVNode("div", _hoisted_2$1, [createVNode(unref(Label_default), { for: `ctx-${__props.name}` }, {
					default: withCtx(() => [createTextVNode(toDisplayString(__props.label), 1)]),
					_: 1
				}, 8, ["for"]), unref(form)?.touched(__props.name) ? (openBlock(), createElementBlock("span", _hoisted_3$1, "touched")) : createCommentVNode("", true)]),
				createVNode(unref(Input_default), {
					id: `ctx-${__props.name}`,
					name: __props.name,
					type: __props.type ?? "text",
					placeholder: __props.placeholder,
					onBlur: _cache[0] || (_cache[0] = ($event) => unref(form)?.validate(__props.name))
				}, null, 8, [
					"id",
					"name",
					"type",
					"placeholder"
				]),
				createVNode(InputError_default, { message: unref(form)?.errors[__props.name] }, null, 8, ["message"])
			]);
		};
	}
});
//#endregion
//#region resources/js/pages/Features/Forms/UseFormContext.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "flex items-center gap-2 pt-2" };
var _hoisted_4 = {
	key: 0,
	class: "text-sm text-muted-foreground"
};
var _hoisted_5 = {
	key: 1,
	class: "text-sm text-green-600"
};
var _hoisted_6 = { class: "mt-4 space-y-3 rounded-md border border-black/10 p-4 dark:border-white/10" };
var _hoisted_7 = { class: "grid grid-cols-2 gap-2 text-sm" };
var _hoisted_8 = { class: "flex items-center justify-between" };
var _hoisted_9 = { class: "flex items-center justify-between" };
var _hoisted_10 = { class: "flex items-center justify-between" };
var _hoisted_11 = { class: "flex items-center justify-between" };
var _hoisted_12 = { class: "space-y-6" };
var _hoisted_13 = { class: "space-y-3" };
//#endregion
//#region resources/js/pages/Features/Forms/UseFormContext.vue
var UseFormContext_default = /* @__PURE__ */ defineComponent({
	__name: "UseFormContext",
	setup(__props) {
		const breadcrumbs = [{ title: "Forms" }, { title: "useFormContext" }];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "useFormContext" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "useFormContext",
					docs: "the-basics/forms#form-context",
					controller: "app/Http/Controllers/Feature/FormController.php#L90"
				}, {
					default: withCtx(() => [..._cache[0] || (_cache[0] = [
						createTextVNode(" Access parent ", -1),
						createBaseVNode("code", { class: "text-xs" }, "<Form>", -1),
						createTextVNode(" state from deeply nested child components. No prop drilling needed. ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [createVNode(FeatureCard_default, { title: "Parent Form" }, {
					description: withCtx(() => [..._cache[1] || (_cache[1] = [
						createTextVNode(" The ", -1),
						createBaseVNode("code", { class: "text-xs" }, "<Form>", -1),
						createTextVNode(" wraps child ", -1),
						createBaseVNode("code", { class: "text-xs" }, "FormContextField", -1),
						createTextVNode(" components that use ", -1),
						createBaseVNode("code", { class: "text-xs" }, "useFormContext()", -1),
						createTextVNode(" to access form state. ", -1)
					])]),
					default: withCtx(() => [createVNode(unref(form_default), {
						action: "/features/forms/form-component",
						method: "post",
						class: "space-y-4"
					}, {
						default: withCtx(({ processing, isDirty, hasErrors, recentlySuccessful, validating }) => [
							_cache[7] || (_cache[7] = createBaseVNode("input", {
								type: "hidden",
								name: "role",
								value: "developer"
							}, null, -1)),
							createVNode(FormContextField_default, {
								name: "name",
								label: "Name",
								placeholder: "Enter your name..."
							}),
							createVNode(FormContextField_default, {
								name: "email",
								label: "Email",
								type: "text",
								placeholder: "you@example.com"
							}),
							createVNode(FormContextField_default, {
								name: "bio",
								label: "Bio",
								placeholder: "Tell us about yourself..."
							}),
							createBaseVNode("div", _hoisted_3, [
								createVNode(unref(Button_default), {
									type: "submit",
									disabled: processing
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(processing ? "Submitting..." : "Submit"), 1)]),
									_: 2
								}, 1032, ["disabled"]),
								isDirty ? (openBlock(), createElementBlock("span", _hoisted_4, "Unsaved changes")) : createCommentVNode("", true),
								recentlySuccessful ? (openBlock(), createElementBlock("span", _hoisted_5, "Saved!")) : createCommentVNode("", true)
							]),
							createBaseVNode("div", _hoisted_6, [_cache[6] || (_cache[6] = createBaseVNode("h3", { class: "text-sm font-semibold" }, " Parent Slot Props ", -1)), createBaseVNode("div", _hoisted_7, [
								createBaseVNode("div", _hoisted_8, [_cache[2] || (_cache[2] = createBaseVNode("span", null, "processing", -1)), createVNode(unref(Badge_default), {
									variant: processing ? "default" : "secondary",
									class: "text-xs"
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(processing), 1)]),
									_: 2
								}, 1032, ["variant"])]),
								createBaseVNode("div", _hoisted_9, [_cache[3] || (_cache[3] = createBaseVNode("span", null, "isDirty", -1)), createVNode(unref(Badge_default), {
									variant: isDirty ? "default" : "secondary",
									class: "text-xs"
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(isDirty), 1)]),
									_: 2
								}, 1032, ["variant"])]),
								createBaseVNode("div", _hoisted_10, [_cache[4] || (_cache[4] = createBaseVNode("span", null, "hasErrors", -1)), createVNode(unref(Badge_default), {
									variant: hasErrors ? "destructive" : "secondary",
									class: "text-xs"
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(hasErrors), 1)]),
									_: 2
								}, 1032, ["variant"])]),
								createBaseVNode("div", _hoisted_11, [_cache[5] || (_cache[5] = createBaseVNode("span", null, "validating", -1)), createVNode(unref(Badge_default), {
									variant: validating ? "default" : "secondary",
									class: "text-xs"
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(validating), 1)]),
									_: 2
								}, 1032, ["variant"])])
							])])
						]),
						_: 1
					})]),
					_: 1
				}), createBaseVNode("div", _hoisted_12, [createVNode(FeatureCard_default, {
					"info-card": "",
					title: "How It Works"
				}, {
					default: withCtx(() => [..._cache[8] || (_cache[8] = [createBaseVNode("div", { class: "space-y-3 text-sm text-muted-foreground" }, [
						createBaseVNode("p", null, [
							createBaseVNode("strong", null, "1."),
							createTextVNode(" The parent renders a "),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "<Form>"),
							createTextVNode(" component. ")
						]),
						createBaseVNode("p", null, [
							createBaseVNode("strong", null, "2."),
							createTextVNode(" Child components call "),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "useFormContext()"),
							createTextVNode(" to access the same form state. ")
						]),
						createBaseVNode("p", null, [createBaseVNode("strong", null, "3."), createTextVNode(" No props need to be passed. Context is provided automatically via Vue's provide/inject. ")]),
						createBaseVNode("p", null, [
							createBaseVNode("strong", null, "4."),
							createTextVNode(" Children can read "),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "errors"),
							createTextVNode(", "),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "processing"),
							createTextVNode(", "),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "isDirty"),
							createTextVNode(", and call "),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "validate()"),
							createTextVNode(" / "),
							createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "touch()"),
							createTextVNode(". ")
						])
					], -1)])]),
					_: 1
				}), createVNode(FeatureCard_default, {
					"info-card": "",
					title: "API Reference"
				}, {
					default: withCtx(() => [createBaseVNode("div", _hoisted_13, [createVNode(CodeBlock_default, {
						title: "Child Component",
						code: "import { useFormContext } from '@inertiajs/vue3'\n\nconst form = useFormContext()\n// form.errors, form.processing, form.isDirty\n// form.validate('field'), form.touch('field')"
					}), createVNode(CodeBlock_default, { title: "Returns null outside Form" }, {
						default: withCtx(() => [..._cache[9] || (_cache[9] = [createBaseVNode("textarea", null, "const form = useFormContext()\n// Returns null if not inside a <Form>\n                                ", -1)])]),
						_: 1
					})])]),
					_: 1
				})])])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { UseFormContext_default as default };

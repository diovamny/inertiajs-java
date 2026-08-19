import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, an as renderList, at as createElementBlock, dt as createTextVNode, f as usePage, ft as createVNode, i as head_default, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, pr as unref, r as form_default, rr as ref, rt as createBlock, tt as computed, yr as toDisplayString } from "./dist.js";
import { t as InputError_default } from "./InputError.js";
import { r as X, t as AppLayout_default } from "./AppLayout.js";
import { t as Label_default } from "./Label.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Plus } from "./plus.js";
import { t as Input_default } from "./Input.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Forms/DottedKeys.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-3 rounded-lg border border-black/10 p-4 dark:border-white/10" };
var _hoisted_4 = { class: "space-y-2" };
var _hoisted_5 = { class: "space-y-2" };
var _hoisted_6 = { class: "space-y-3 rounded-lg border border-black/10 p-4 dark:border-white/10" };
var _hoisted_7 = { class: "space-y-2" };
var _hoisted_8 = { class: "space-y-2" };
var _hoisted_9 = { class: "space-y-3 rounded-lg border border-black/10 p-4 dark:border-white/10" };
var _hoisted_10 = { class: "flex items-center gap-2 pt-2" };
var _hoisted_11 = {
	key: 0,
	class: "text-sm text-muted-foreground"
};
var _hoisted_12 = {
	key: 1,
	class: "text-sm text-green-600"
};
var _hoisted_13 = { class: "space-y-6" };
var _hoisted_14 = {
	key: 1,
	class: "text-sm text-muted-foreground"
};
var _hoisted_15 = { class: "space-y-3 text-sm text-muted-foreground" };
//#endregion
//#region resources/js/pages/Features/Forms/DottedKeys.vue
var DottedKeys_default = /* @__PURE__ */ defineComponent({
	__name: "DottedKeys",
	setup(__props) {
		const breadcrumbs = [{ title: "Forms" }, { title: "Dotted Keys" }];
		const page = usePage();
		const tags = ref([""]);
		function addTag() {
			tags.value.push("");
		}
		function removeTag(index) {
			tags.value.splice(index, 1);
		}
		const parsedData = computed(() => page.flash.parsedData);
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Dotted Keys" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Dotted Keys",
					docs: "the-basics/forms#dotted-key-notation",
					controller: "app/Http/Controllers/Feature/FormController.php#L95"
				}, {
					default: withCtx(() => [..._cache[0] || (_cache[0] = [createTextVNode(" Nested objects, arrays, and escaped dots in form field names. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [createVNode(FeatureCard_default, { title: "Nested Form Fields" }, {
					description: withCtx(() => [..._cache[1] || (_cache[1] = [
						createTextVNode(" Dotted notation creates nested objects: ", -1),
						createBaseVNode("code", { class: "text-xs" }, "user.name", -1),
						createTextVNode(" becomes ", -1),
						createBaseVNode("code", { class: "text-xs" }, toDisplayString("{ user: { name: ... } }"), -1),
						createTextVNode(". ", -1)
					])]),
					default: withCtx(() => [createVNode(unref(form_default), {
						action: "/features/forms/dotted-keys",
						method: "post",
						class: "space-y-6"
					}, {
						default: withCtx(({ errors, processing, isDirty, recentlySuccessful }) => [
							createBaseVNode("fieldset", _hoisted_3, [
								_cache[4] || (_cache[4] = createBaseVNode("legend", { class: "px-2 text-sm font-semibold" }, [
									createTextVNode(" User ("),
									createBaseVNode("code", { class: "text-xs" }, "user.*"),
									createTextVNode(") ")
								], -1)),
								createBaseVNode("div", _hoisted_4, [
									createVNode(unref(Label_default), { for: "dk-user-name" }, {
										default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode("Name", -1)])]),
										_: 1
									}),
									createVNode(unref(Input_default), {
										id: "dk-user-name",
										name: "user.name",
										placeholder: "John Doe"
									}),
									createVNode(InputError_default, { message: errors["user.name"] }, null, 8, ["message"])
								]),
								createBaseVNode("div", _hoisted_5, [
									createVNode(unref(Label_default), { for: "dk-user-email" }, {
										default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode("Email", -1)])]),
										_: 1
									}),
									createVNode(unref(Input_default), {
										id: "dk-user-email",
										type: "text",
										name: "user.email",
										placeholder: "john@example.com"
									}),
									createVNode(InputError_default, { message: errors["user.email"] }, null, 8, ["message"])
								])
							]),
							createBaseVNode("fieldset", _hoisted_6, [
								_cache[7] || (_cache[7] = createBaseVNode("legend", { class: "px-2 text-sm font-semibold" }, [
									createTextVNode(" Address ("),
									createBaseVNode("code", { class: "text-xs" }, "address.*"),
									createTextVNode(") ")
								], -1)),
								createBaseVNode("div", _hoisted_7, [
									createVNode(unref(Label_default), { for: "dk-address-street" }, {
										default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode("Street", -1)])]),
										_: 1
									}),
									createVNode(unref(Input_default), {
										id: "dk-address-street",
										name: "address.street",
										placeholder: "123 Main St"
									}),
									createVNode(InputError_default, { message: errors["address.street"] }, null, 8, ["message"])
								]),
								createBaseVNode("div", _hoisted_8, [
									createVNode(unref(Label_default), { for: "dk-address-city" }, {
										default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode("City", -1)])]),
										_: 1
									}),
									createVNode(unref(Input_default), {
										id: "dk-address-city",
										name: "address.city",
										placeholder: "Springfield"
									}),
									createVNode(InputError_default, { message: errors["address.city"] }, null, 8, ["message"])
								])
							]),
							createBaseVNode("fieldset", _hoisted_9, [
								_cache[9] || (_cache[9] = createBaseVNode("legend", { class: "px-2 text-sm font-semibold" }, [
									createTextVNode(" Tags ("),
									createBaseVNode("code", { class: "text-xs" }, "tags[]"),
									createTextVNode(") ")
								], -1)),
								(openBlock(true), createElementBlock(Fragment, null, renderList(tags.value, (tag, index) => {
									return openBlock(), createElementBlock("div", {
										key: index,
										class: "flex items-center gap-2"
									}, [createVNode(unref(Input_default), {
										name: `tags[]`,
										modelValue: tags.value[index],
										"onUpdate:modelValue": ($event) => tags.value[index] = $event,
										placeholder: `Tag ${index + 1}`
									}, null, 8, [
										"modelValue",
										"onUpdate:modelValue",
										"placeholder"
									]), tags.value.length > 1 ? (openBlock(), createBlock(unref(Button_default), {
										key: 0,
										type: "button",
										variant: "ghost",
										size: "icon",
										onClick: ($event) => removeTag(index)
									}, {
										default: withCtx(() => [createVNode(unref(X), { class: "size-4" })]),
										_: 1
									}, 8, ["onClick"])) : createCommentVNode("", true)]);
								}), 128)),
								createVNode(unref(Button_default), {
									type: "button",
									variant: "outline",
									size: "sm",
									onClick: addTag
								}, {
									default: withCtx(() => [createVNode(unref(Plus), { class: "mr-1 size-4" }), _cache[8] || (_cache[8] = createTextVNode(" Add Tag ", -1))]),
									_: 1
								})
							]),
							createBaseVNode("div", _hoisted_10, [
								createVNode(unref(Button_default), {
									type: "submit",
									disabled: processing
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(processing ? "Submitting..." : "Submit"), 1)]),
									_: 2
								}, 1032, ["disabled"]),
								isDirty ? (openBlock(), createElementBlock("span", _hoisted_11, "Unsaved changes")) : createCommentVNode("", true),
								recentlySuccessful ? (openBlock(), createElementBlock("span", _hoisted_12, "Saved!")) : createCommentVNode("", true)
							])
						]),
						_: 1
					})]),
					_: 1
				}), createBaseVNode("div", _hoisted_13, [createVNode(FeatureCard_default, {
					"info-card": "",
					title: "Parsed Request Data",
					description: "The server echoes back the parsed data structure via flash."
				}, {
					default: withCtx(() => [parsedData.value ? (openBlock(), createBlock(CodeBlock_default, {
						key: 0,
						code: JSON.stringify(parsedData.value, null, 2)
					}, null, 8, ["code"])) : (openBlock(), createElementBlock("p", _hoisted_14, " Submit the form to see the parsed data structure. "))]),
					_: 1
				}), createVNode(FeatureCard_default, {
					"info-card": "",
					title: "How It Works"
				}, {
					default: withCtx(() => [createBaseVNode("div", _hoisted_15, [
						createVNode(CodeBlock_default, { title: "Nested Objects" }, {
							default: withCtx(() => [..._cache[10] || (_cache[10] = [createBaseVNode("textarea", null, "                                    <input name=\"user.name\" />\n                                    <input name=\"user.email\" />\n                                    // Submits: { user: { name: ..., email: ... } }\n                                ", -1)])]),
							_: 1
						}),
						createVNode(CodeBlock_default, { title: "Array Fields" }, {
							default: withCtx(() => [..._cache[11] || (_cache[11] = [createBaseVNode("textarea", null, "                                    <input name=\"tags[]\" />\n                                    <input name=\"tags[]\" />\n                                    // Submits: { tags: [\"a\", \"b\"] }\n                                ", -1)])]),
							_: 1
						}),
						createVNode(CodeBlock_default, { title: "Escaped Dots" }, {
							default: withCtx(() => [..._cache[12] || (_cache[12] = [createBaseVNode("textarea", null, "                                    <input name=\"config\\.version\" />\n                                    // Submits: { \"config.version\": \"1.0\" }\n                                ", -1)])]),
							_: 1
						})
					])]),
					_: 1
				})])])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { DottedKeys_default as default };

import { $t as openBlock, G as Fragment, Mn as withCtx, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, rr as ref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Label_default } from "./Label.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Input_default } from "./Input.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Layouts/Head.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = ["content"];
var _hoisted_2 = ["content"];
var _hoisted_3 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_4 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_5 = { class: "space-y-4" };
var _hoisted_6 = { class: "space-y-2" };
var _hoisted_7 = { class: "flex items-center gap-2" };
var _hoisted_8 = { class: "space-y-4" };
var _hoisted_9 = { class: "space-y-2" };
var _hoisted_10 = { class: "space-y-3" };
var _hoisted_11 = { class: "space-y-3" };
var _hoisted_12 = { class: "grid gap-3 sm:grid-cols-3" };
//#endregion
//#region resources/js/pages/Features/Layouts/Head.vue
var Head_default = /* @__PURE__ */ defineComponent({
	__name: "Head",
	setup(__props) {
		const breadcrumbs = [{ title: "Layouts & Head" }, { title: "Head Component" }];
		const dynamicTitle = ref("Head Component");
		const metaDescription = ref("Document head management with Inertia.js");
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: dynamicTitle.value }, {
				default: withCtx(() => [createBaseVNode("meta", {
					"head-key": "description",
					name: "description",
					content: metaDescription.value
				}, null, 8, _hoisted_1), createBaseVNode("meta", {
					"head-key": "og:title",
					property: "og:title",
					content: dynamicTitle.value
				}, null, 8, _hoisted_2)]),
				_: 1
			}, 8, ["title"]), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createVNode(FeatureHeader_default, {
					title: "Head Component",
					docs: "the-basics/title-and-meta#head-component",
					controller: "app/Http/Controllers/Feature/LayoutController.php#L25"
				}, {
					default: withCtx(() => [..._cache[2] || (_cache[2] = [
						createTextVNode(" Document ", -1),
						createBaseVNode("code", { class: "text-xs" }, "<head>", -1),
						createTextVNode(" management with ", -1),
						createBaseVNode("code", { class: "text-xs" }, "<Head>", -1),
						createTextVNode(" for titles, meta tags, and more. ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_4, [
					createVNode(FeatureCard_default, {
						title: "Dynamic Title",
						description: "Change the page title and watch the browser tab update in real time."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_5, [
							createBaseVNode("div", _hoisted_6, [createVNode(unref(Label_default), { for: "title" }, {
								default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode("Page Title", -1)])]),
								_: 1
							}), createVNode(unref(Input_default), {
								id: "title",
								modelValue: dynamicTitle.value,
								"onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => dynamicTitle.value = $event),
								placeholder: "Enter a title..."
							}, null, 8, ["modelValue"])]),
							createBaseVNode("div", _hoisted_7, [_cache[4] || (_cache[4] = createBaseVNode("span", { class: "text-sm font-medium" }, "Current:", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
								default: withCtx(() => [createTextVNode(toDisplayString(dynamicTitle.value), 1)]),
								_: 1
							})]),
							_cache[5] || (_cache[5] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " Check the browser tab. The title updates reactively. ", -1))
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Meta Tags",
						description: "Manage meta description, OG tags, and more."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_8, [createBaseVNode("div", _hoisted_9, [createVNode(unref(Label_default), { for: "description" }, {
							default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode("Meta Description", -1)])]),
							_: 1
						}), createVNode(unref(Input_default), {
							id: "description",
							modelValue: metaDescription.value,
							"onUpdate:modelValue": _cache[1] || (_cache[1] = ($event) => metaDescription.value = $event),
							placeholder: "Enter a description..."
						}, null, 8, ["modelValue"])]), createVNode(CodeBlock_default, { title: "Current meta tags:" }, {
							default: withCtx(() => [..._cache[7] || (_cache[7] = [createBaseVNode("textarea", null, "<meta name=\"description\"\n  content=\"${metaDescription}\" />\n<meta property=\"og:title\"\n  content=\"${dynamicTitle}\" />\n                            ", -1)])]),
							_: 1
						})])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Title Template"
					}, {
						description: withCtx(() => [..._cache[8] || (_cache[8] = [
							createTextVNode(" Global title formatting via ", -1),
							createBaseVNode("code", { class: "text-xs" }, "createInertiaApp", -1),
							createTextVNode(". ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_10, [createVNode(CodeBlock_default, null, {
							default: withCtx(() => [..._cache[9] || (_cache[9] = [createBaseVNode("textarea", null, "                            createInertiaApp({\n                              title: (title) =>\n                                title ? `${title} - ${appName}` : appName,\n                            })\n                            ", -1)])]),
							_: 1
						}), _cache[10] || (_cache[10] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
							createTextVNode(" This app uses the template above. So "),
							createBaseVNode("code", null, "<Head title=\"Home\" />"),
							createTextVNode(" becomes "),
							createBaseVNode("code", null, "\"Home - Inertia Kitchen Sink\""),
							createTextVNode(" in the browser tab. ")
						], -1))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Tag Deduplication"
					}, {
						description: withCtx(() => [..._cache[11] || (_cache[11] = [
							createTextVNode(" Use ", -1),
							createBaseVNode("code", { class: "text-xs" }, "head-key", -1),
							createTextVNode(" to prevent duplicate tags. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_11, [
							createVNode(CodeBlock_default, { title: "Layout sets defaults:" }, {
								default: withCtx(() => [..._cache[12] || (_cache[12] = [createBaseVNode("textarea", null, "<Head>\n  <meta head-key=\"description\"\n    content=\"Default\" />\n</Head>\n                            ", -1)])]),
								_: 1
							}),
							createVNode(CodeBlock_default, { title: "Page overrides:" }, {
								default: withCtx(() => [..._cache[13] || (_cache[13] = [createBaseVNode("textarea", null, "<Head>\n  <meta head-key=\"description\"\n    content=\"Page-specific\" />\n</Head>\n                            ", -1)])]),
								_: 1
							}),
							_cache[14] || (_cache[14] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
								createTextVNode(" Only one tag with the same "),
								createBaseVNode("code", null, "head-key"),
								createTextVNode(" renders. The page-level tag wins. ")
							], -1))
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "API Reference"
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_12, [
							createVNode(CodeBlock_default, { title: "Title shorthand" }, {
								default: withCtx(() => [..._cache[15] || (_cache[15] = [createBaseVNode("textarea", null, "<Head title=\"My Page\" />\n                            ", -1)])]),
								_: 1
							}),
							createVNode(CodeBlock_default, { title: "Full control" }, {
								default: withCtx(() => [..._cache[16] || (_cache[16] = [createBaseVNode("textarea", null, "<Head>\n  <title>My Page</title>\n  <meta ... />\n</Head>\n                            ", -1)])]),
								_: 1
							}),
							createVNode(CodeBlock_default, {
								title: "Reset on unmount",
								code: "When a page unmounts,\nits Head tags are removed\nautomatically."
							})
						])]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { Head_default as default };

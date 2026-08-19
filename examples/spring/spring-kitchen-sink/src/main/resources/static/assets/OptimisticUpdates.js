import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, M as vModelCheckbox, Mn as withCtx, Pn as withDirectives, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, gr as normalizeClass, i as head_default, it as createCommentVNode, l as useForm, mt as defineComponent, nt as createBaseVNode, pr as unref, r as form_default, rr as ref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Heart } from "./heart.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Forms/OptimisticUpdates.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "flex flex-wrap gap-2" };
var _hoisted_3 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_4 = { class: "mb-4 flex items-center gap-2 rounded-md bg-muted p-3" };
var _hoisted_5 = {
	key: 0,
	class: "space-y-2"
};
var _hoisted_6 = { class: "flex size-8 items-center justify-center rounded-full bg-primary/10 text-xs font-medium text-primary" };
var _hoisted_7 = { class: "min-w-0 flex-1" };
var _hoisted_8 = { class: "text-sm font-medium" };
var _hoisted_9 = {
	key: 0,
	class: "truncate text-xs text-muted-foreground"
};
var _hoisted_10 = ["onClick"];
var _hoisted_11 = {
	key: 1,
	class: "space-y-2"
};
var _hoisted_12 = { class: "flex size-8 items-center justify-center rounded-full bg-primary/10 text-xs font-medium text-primary" };
var _hoisted_13 = { class: "min-w-0 flex-1" };
var _hoisted_14 = { class: "text-sm font-medium" };
var _hoisted_15 = {
	key: 0,
	class: "truncate text-xs text-muted-foreground"
};
var _hoisted_16 = ["value"];
var _hoisted_17 = {
	type: "submit",
	class: "rounded-md p-1.5 transition-colors hover:bg-accent"
};
var _hoisted_18 = {
	key: 2,
	class: "py-4 text-center text-sm text-muted-foreground"
};
var _hoisted_19 = { class: "space-y-6" };
var _hoisted_20 = { class: "space-y-3" };
var _hoisted_21 = {
	key: 0,
	class: "space-y-1"
};
var _hoisted_22 = {
	key: 1,
	class: "text-sm text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Features/Forms/OptimisticUpdates.vue
var OptimisticUpdates_default = /* @__PURE__ */ defineComponent({
	__name: "OptimisticUpdates",
	props: { contacts: {} },
	setup(__props) {
		const props = __props;
		const breadcrumbs = [{ title: "Forms" }, { title: "Optimistic Updates" }];
		const simulateError = ref(false);
		const activeTab = ref("router");
		const eventLog = ref([]);
		function log(message) {
			eventLog.value.unshift(`${(/* @__PURE__ */ new Date()).toLocaleTimeString()} - ${message}`);
			if (eventLog.value.length > 15) eventLog.value.pop();
		}
		function toggleFavoriteRouter(contact) {
			log(`[router.optimistic] Toggling ${contact.first_name}...`);
			router.optimistic((currentProps) => ({ contacts: currentProps.contacts.map((c) => c.id === contact.id ? {
				...c,
				is_favorite: !c.is_favorite
			} : c) })).post(`/features/forms/optimistic-toggle/${contact.id}`, { simulate_error: simulateError.value }, {
				preserveScroll: true,
				onSuccess: () => log(`[router.optimistic] Server confirmed`),
				onError: () => log(`[router.optimistic] Error! Auto-rolled back`)
			});
		}
		const favoriteForm = useForm({ simulate_error: false });
		function toggleFavoriteUseForm(contact) {
			log(`[useForm.optimistic] Toggling ${contact.first_name}...`);
			favoriteForm.simulate_error = simulateError.value;
			favoriteForm.optimistic((currentProps) => ({ contacts: currentProps.contacts.map((c) => c.id === contact.id ? {
				...c,
				is_favorite: !c.is_favorite
			} : c) })).post(`/features/forms/optimistic-toggle/${contact.id}`, {
				preserveScroll: true,
				onSuccess: () => log(`[useForm.optimistic] Server confirmed`),
				onError: () => log(`[useForm.optimistic] Error! Auto-rolled back`)
			});
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Optimistic Updates" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [
					createVNode(FeatureHeader_default, {
						title: "Optimistic Updates",
						docs: "the-basics/optimistic-updates",
						controller: "app/Http/Controllers/Feature/FormController.php#L81"
					}, {
						default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" Instant UI feedback with automatic rollback on error. Toggle a favorite and notice how the heart fills immediately while the request is still in flight. ", -1)])]),
						_: 1
					}),
					createBaseVNode("div", _hoisted_2, [
						createVNode(unref(Button_default), {
							variant: activeTab.value === "router" ? "default" : "outline",
							size: "sm",
							onClick: _cache[0] || (_cache[0] = ($event) => activeTab.value = "router")
						}, {
							default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode("router.optimistic()", -1)])]),
							_: 1
						}, 8, ["variant"]),
						createVNode(unref(Button_default), {
							variant: activeTab.value === "useForm" ? "default" : "outline",
							size: "sm",
							onClick: _cache[1] || (_cache[1] = ($event) => activeTab.value = "useForm")
						}, {
							default: withCtx(() => [..._cache[7] || (_cache[7] = [createTextVNode("useForm.optimistic()", -1)])]),
							_: 1
						}, 8, ["variant"]),
						createVNode(unref(Button_default), {
							variant: activeTab.value === "formComponent" ? "default" : "outline",
							size: "sm",
							onClick: _cache[2] || (_cache[2] = ($event) => activeTab.value = "formComponent")
						}, {
							default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode("Form :optimistic", -1)])]),
							_: 1
						}, 8, ["variant"])
					]),
					createBaseVNode("div", _hoisted_3, [createVNode(FeatureCard_default, { title: "Contacts" }, {
						description: withCtx(() => [activeTab.value === "router" ? (openBlock(), createElementBlock(Fragment, { key: 0 }, [_cache[9] || (_cache[9] = createBaseVNode("code", { class: "text-xs" }, "router.optimistic()", -1)), _cache[10] || (_cache[10] = createTextVNode(". Declarative prop updates with automatic rollback.", -1))], 64)) : activeTab.value === "useForm" ? (openBlock(), createElementBlock(Fragment, { key: 1 }, [_cache[11] || (_cache[11] = createBaseVNode("code", { class: "text-xs" }, "useForm().optimistic()", -1)), _cache[12] || (_cache[12] = createTextVNode(". Chained on the form helper.", -1))], 64)) : (openBlock(), createElementBlock(Fragment, { key: 2 }, [_cache[13] || (_cache[13] = createBaseVNode("code", { class: "text-xs" }, "<Form :optimistic>", -1)), _cache[14] || (_cache[14] = createTextVNode(". Optimistic updates on the Form component.", -1))], 64))]),
						default: withCtx(() => [
							createBaseVNode("div", _hoisted_4, [withDirectives(createBaseVNode("input", {
								id: "simulate-error",
								type: "checkbox",
								"onUpdate:modelValue": _cache[3] || (_cache[3] = ($event) => simulateError.value = $event),
								class: "size-4 rounded border"
							}, null, 512), [[vModelCheckbox, simulateError.value]]), _cache[15] || (_cache[15] = createBaseVNode("label", {
								for: "simulate-error",
								class: "text-sm"
							}, " Simulate error (to see rollback) ", -1))]),
							activeTab.value !== "formComponent" ? (openBlock(), createElementBlock("div", _hoisted_5, [(openBlock(true), createElementBlock(Fragment, null, renderList(props.contacts, (contact) => {
								return openBlock(), createElementBlock("div", {
									key: contact.id,
									class: "flex items-center gap-3 rounded-xl bg-muted/30 p-3 transition-colors"
								}, [
									createBaseVNode("div", _hoisted_6, toDisplayString(contact.first_name[0]) + toDisplayString(contact.last_name[0]), 1),
									createBaseVNode("div", _hoisted_7, [createBaseVNode("span", _hoisted_8, toDisplayString(contact.first_name) + " " + toDisplayString(contact.last_name), 1), contact.email ? (openBlock(), createElementBlock("div", _hoisted_9, toDisplayString(contact.email), 1)) : createCommentVNode("", true)]),
									createBaseVNode("button", {
										type: "button",
										class: "rounded-md p-1.5 transition-colors hover:bg-accent",
										onClick: ($event) => activeTab.value === "router" ? toggleFavoriteRouter(contact) : toggleFavoriteUseForm(contact)
									}, [createVNode(unref(Heart), { class: normalizeClass(["size-5 transition-colors", contact.is_favorite ? "fill-red-500 text-red-500" : "text-muted-foreground"]) }, null, 8, ["class"])], 8, _hoisted_10)
								]);
							}), 128))])) : (openBlock(), createElementBlock("div", _hoisted_11, [(openBlock(true), createElementBlock(Fragment, null, renderList(props.contacts, (contact) => {
								return openBlock(), createElementBlock("div", {
									key: contact.id,
									class: "flex items-center gap-3 rounded-xl bg-muted/30 p-3 transition-colors"
								}, [
									createBaseVNode("div", _hoisted_12, toDisplayString(contact.first_name[0]) + toDisplayString(contact.last_name[0]), 1),
									createBaseVNode("div", _hoisted_13, [createBaseVNode("span", _hoisted_14, toDisplayString(contact.first_name) + " " + toDisplayString(contact.last_name), 1), contact.email ? (openBlock(), createElementBlock("div", _hoisted_15, toDisplayString(contact.email), 1)) : createCommentVNode("", true)]),
									createVNode(unref(form_default), {
										action: `/features/forms/optimistic-toggle/${contact.id}`,
										method: "post",
										optimistic: (currentProps) => ({ contacts: currentProps.contacts.map((c) => c.id === contact.id ? {
											...c,
											is_favorite: !c.is_favorite
										} : c) }),
										"preserve-scroll": "",
										class: "contents"
									}, {
										default: withCtx(() => [createBaseVNode("input", {
											type: "hidden",
											name: "simulate_error",
											value: simulateError.value ? "1" : "0"
										}, null, 8, _hoisted_16), createBaseVNode("button", _hoisted_17, [createVNode(unref(Heart), { class: normalizeClass(["size-5 transition-colors", contact.is_favorite ? "fill-red-500 text-red-500" : "text-muted-foreground"]) }, null, 8, ["class"])])]),
										_: 2
									}, 1032, ["action", "optimistic"])
								]);
							}), 128))])),
							!props.contacts.length ? (openBlock(), createElementBlock("p", _hoisted_18, " No contacts found. Seed the database first. ")) : createCommentVNode("", true)
						]),
						_: 1
					}), createBaseVNode("div", _hoisted_19, [createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Approach Comparison"
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_20, [
							createBaseVNode("div", { class: normalizeClass(["rounded-md p-3 text-xs", activeTab.value === "router" ? "bg-muted ring-2 ring-primary" : "bg-muted"]) }, [createVNode(CodeBlock_default, {
								title: "router.optimistic()",
								code: "router.optimistic((props) => ({\n  contacts: /* updated */\n})).post(url, data)"
							})], 2),
							createBaseVNode("div", { class: normalizeClass(["rounded-md p-3 text-xs", activeTab.value === "useForm" ? "bg-muted ring-2 ring-primary" : "bg-muted"]) }, [createVNode(CodeBlock_default, {
								title: "useForm.optimistic()",
								code: "form.optimistic((props) => ({\n  contacts: /* updated */\n})).post(url)"
							})], 2),
							createBaseVNode("div", { class: normalizeClass(["rounded-md p-3 text-xs", activeTab.value === "formComponent" ? "bg-muted ring-2 ring-primary" : "bg-muted"]) }, [createVNode(CodeBlock_default, { title: "<Form :optimistic>" }, {
								default: withCtx(() => [..._cache[16] || (_cache[16] = [createBaseVNode("textarea", null, "<Form :optimistic=\"(props, data) => ({\n  contacts: /* updated */\n})\" />\n                                    ", -1)])]),
								_: 1
							})], 2)
						])]),
						_: 1
					}), createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Event Log"
					}, {
						"header-action": withCtx(() => [createVNode(unref(Button_default), {
							variant: "ghost",
							size: "sm",
							onClick: _cache[4] || (_cache[4] = ($event) => eventLog.value = [])
						}, {
							default: withCtx(() => [..._cache[17] || (_cache[17] = [createTextVNode("Clear", -1)])]),
							_: 1
						})]),
						default: withCtx(() => [eventLog.value.length ? (openBlock(), createElementBlock("div", _hoisted_21, [(openBlock(true), createElementBlock(Fragment, null, renderList(eventLog.value, (entry, index) => {
							return openBlock(), createElementBlock("div", {
								key: index,
								class: normalizeClass(["rounded bg-muted px-3 py-1.5 font-mono text-xs", {
									"text-red-600 dark:text-red-400": entry.includes("Error"),
									"text-green-600 dark:text-green-400": entry.includes("confirmed")
								}])
							}, toDisplayString(entry), 3);
						}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_22, " Toggle a favorite to see events here. "))]),
						_: 1
					})])])
				])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { OptimisticUpdates_default as default };

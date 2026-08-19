import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Navigation/InstantVisits.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-3" };
var _hoisted_4 = { class: "flex items-center justify-between" };
var _hoisted_5 = { class: "flex items-center justify-between" };
var _hoisted_6 = { class: "space-y-4" };
var _hoisted_7 = { class: "space-y-4" };
var _hoisted_8 = { class: "space-y-4" };
var _hoisted_9 = { class: "space-y-4" };
var _hoisted_10 = { class: "grid gap-3 sm:grid-cols-3" };
var targetUrl = "/features/navigation/instant-visit-target?delay=2";
//#endregion
//#region resources/js/pages/Features/Navigation/InstantVisits.vue
var InstantVisits_default = /* @__PURE__ */ defineComponent({
	__name: "InstantVisits",
	props: {
		sourceTimestamp: {},
		message: {}
	},
	setup(__props) {
		const breadcrumbs = [{ title: "Navigation" }, { title: "Instant Visits" }];
		function visitBasic() {
			router.visit(targetUrl, { component: "Features/Navigation/InstantVisitTarget" });
		}
		function visitWithPlaceholderProps() {
			router.visit(targetUrl, {
				component: "Features/Navigation/InstantVisitTarget",
				pageProps: (_currentProps, sharedProps) => ({
					...sharedProps,
					greeting: "Loading from server...",
					serverTimestamp: "Fetching...",
					items: []
				})
			});
		}
		function visitWithCallbackProps() {
			router.visit(targetUrl, {
				component: "Features/Navigation/InstantVisitTarget",
				pageProps: (currentProps, sharedProps) => ({
					...sharedProps,
					greeting: `Navigating from source (was: "${currentProps.message}")`,
					serverTimestamp: "Waiting for server..."
				})
			});
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Instant Visits" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Instant Visits",
					docs: "the-basics/manual-visits#client-side-visits",
					controller: "app/Http/Controllers/Feature/NavigationController.php#L79"
				}, {
					default: withCtx(() => [..._cache[0] || (_cache[0] = [
						createTextVNode(" Navigate to a component instantly before the server responds using ", -1),
						createBaseVNode("code", { class: "text-xs" }, "component", -1),
						createTextVNode(" and ", -1),
						createBaseVNode("code", { class: "text-xs" }, "pageProps", -1),
						createTextVNode(" options. ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Current Page State",
						description: "Props on this page (used by the callback demo)."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [_cache[1] || (_cache[1] = createBaseVNode("span", { class: "text-sm font-medium" }, "message", -1)), createVNode(unref(Badge_default), { variant: "outline" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.message), 1)]),
							_: 1
						})]), createBaseVNode("div", _hoisted_5, [_cache[2] || (_cache[2] = createBaseVNode("span", { class: "text-sm font-medium" }, "sourceTimestamp", -1)), createVNode(unref(Badge_default), {
							variant: "outline",
							class: "font-mono text-xs"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.sourceTimestamp), 1)]),
							_: 1
						})])])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "How It Works"
					}, {
						default: withCtx(() => [..._cache[3] || (_cache[3] = [createBaseVNode("div", { class: "space-y-3 text-xs" }, [
							createBaseVNode("div", { class: "rounded-md border border-black/10 p-2 dark:border-white/10" }, [createBaseVNode("p", { class: "font-semibold" }, "1. Instant swap"), createBaseVNode("p", { class: "text-muted-foreground" }, " The target component renders immediately with placeholder or shared props. ")]),
							createBaseVNode("div", { class: "rounded-md border border-black/10 p-2 dark:border-white/10" }, [createBaseVNode("p", { class: "font-semibold" }, "2. Server request"), createBaseVNode("p", { class: "text-muted-foreground" }, " The actual HTTP request fires in the background. ")]),
							createBaseVNode("div", { class: "rounded-md border border-black/10 p-2 dark:border-white/10" }, [createBaseVNode("p", { class: "font-semibold" }, "3. Props update"), createBaseVNode("p", { class: "text-muted-foreground" }, " When the server responds, real props silently replace the placeholders. Redirects also work correctly. ")])
						], -1)])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "Basic Instant Visit" }, {
						description: withCtx(() => [..._cache[4] || (_cache[4] = [
							createTextVNode(" Provide a ", -1),
							createBaseVNode("code", { class: "text-xs" }, "component", -1),
							createTextVNode(" name. The page swaps immediately with shared props. The target component should handle missing page-specific props gracefully (e.g. optional chaining). Server response replaces props when ready. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_6, [createVNode(unref(Button_default), { onClick: visitBasic }, {
							default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" Visit Target (2s delay) ", -1)])]),
							_: 1
						}), createVNode(CodeBlock_default, { code: "\n                            router.visit(targetUrl, {\n                              component: 'Features/.../Target',\n                            })\n                        " })])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "With Placeholder Props" }, {
						description: withCtx(() => [..._cache[6] || (_cache[6] = [
							createTextVNode(" Use ", -1),
							createBaseVNode("code", { class: "text-xs" }, "pageProps", -1),
							createTextVNode(" callback to provide placeholder props. When ", -1),
							createBaseVNode("code", { class: "text-xs" }, "pageProps", -1),
							createTextVNode(" is provided, shared props are not automatically carried over, so spread them yourself. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_7, [createVNode(unref(Button_default), { onClick: visitWithPlaceholderProps }, {
							default: withCtx(() => [..._cache[7] || (_cache[7] = [createTextVNode(" Visit with Placeholders ", -1)])]),
							_: 1
						}), createVNode(CodeBlock_default, { code: "\n                            router.visit(targetUrl, {\n                              component: 'Features/.../Target',\n                              pageProps: (_, sharedProps) => ({\n                                ...sharedProps,\n                                greeting: 'Loading from server...',\n                                items: [],\n                              }),\n                            })\n                        " })])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "Callback Props" }, {
						description: withCtx(() => [..._cache[8] || (_cache[8] = [
							createTextVNode(" Pass ", -1),
							createBaseVNode("code", { class: "text-xs" }, "pageProps", -1),
							createTextVNode(" as a function. Receives current page props and shared props as arguments. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_8, [createVNode(unref(Button_default), { onClick: visitWithCallbackProps }, {
							default: withCtx(() => [..._cache[9] || (_cache[9] = [createTextVNode(" Visit with Callback ", -1)])]),
							_: 1
						}), createVNode(CodeBlock_default, null, {
							default: withCtx(() => [..._cache[10] || (_cache[10] = [createBaseVNode("textarea", null, "                            router.visit(targetUrl, {\n                              component: \"Features/.../Target\",\n                              pageProps: (currentProps, sharedProps) => ({\n                                ...sharedProps,\n                                greeting: `Navigating from source (was: \"${currentProps.message}\")`,\n                              }),\n                            })\n                            ", -1)])]),
							_: 1
						})])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "Link Component" }, {
						description: withCtx(() => [..._cache[11] || (_cache[11] = [
							createTextVNode(" Use the ", -1),
							createBaseVNode("code", { class: "text-xs" }, "component", -1),
							createTextVNode(" prop on ", -1),
							createBaseVNode("code", { class: "text-xs" }, "<Link>", -1),
							createTextVNode(" for declarative instant visits. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_9, [createVNode(unref(link_default), {
							href: targetUrl,
							component: "Features/Navigation/InstantVisitTarget",
							class: "inline-flex h-9 items-center justify-center rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground shadow-xs hover:bg-primary/90"
						}, {
							default: withCtx(() => [..._cache[12] || (_cache[12] = [createTextVNode(" Link with component ", -1)])]),
							_: 1
						}), createVNode(CodeBlock_default, null, {
							default: withCtx(() => [..._cache[13] || (_cache[13] = [createBaseVNode("textarea", null, "                            <Link\n                              :href=\"targetUrl\"\n                              component=\"Features/.../Target\"\n                            >\n                              Link with component\n                            </Link>\n                            ", -1)])]),
							_: 1
						})])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "API Reference"
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_10, [
							createVNode(CodeBlock_default, {
								title: "router.visit()",
								code: "\n                            router.visit(url, {\n                              component: 'Page/Name',\n                              pageProps: { ... },\n                            })\n                        "
							}),
							createVNode(CodeBlock_default, { title: "<Link>" }, {
								default: withCtx(() => [..._cache[14] || (_cache[14] = [createBaseVNode("textarea", null, "                            <Link\n                              href=\"/target\"\n                              component=\"Page/Name\"\n                              :page-props=\"{ ... }\"\n                            />\n                            ", -1)])]),
								_: 1
							}),
							createVNode(CodeBlock_default, {
								title: "pageProps callback",
								code: "\n                            pageProps: (current, shared) => ({\n                              ...shared,\n                              custom: 'value',\n                            })\n                        "
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
export { InstantVisits_default as default };

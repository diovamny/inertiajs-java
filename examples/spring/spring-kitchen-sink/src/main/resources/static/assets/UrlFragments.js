import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, at as createElementBlock, dt as createTextVNode, f as usePage, ft as createVNode, i as head_default, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, rt as createBlock, tt as computed, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/Navigation/UrlFragments.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "space-y-3" };
var _hoisted_3 = { class: "flex items-center gap-2" };
var _hoisted_4 = { class: "flex items-center gap-2" };
var _hoisted_5 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_6 = { class: "space-y-4" };
var _hoisted_7 = { class: "flex flex-wrap gap-2" };
var _hoisted_8 = { class: "space-y-4" };
var _hoisted_9 = { class: "flex flex-wrap gap-2" };
var _hoisted_10 = { class: "grid gap-3 sm:grid-cols-3" };
//#endregion
//#region resources/js/pages/Features/Navigation/UrlFragments.vue
var UrlFragments_default = /* @__PURE__ */ defineComponent({
	__name: "UrlFragments",
	props: {
		timestamp: {},
		redirectedFrom: {}
	},
	setup(__props) {
		const page = usePage();
		const currentUrl = computed(() => page.url);
		const breadcrumbs = [{ title: "Navigation" }, { title: "URL Fragments" }];
		function hashRedirectGet() {
			router.get("/features/navigation/url-fragments/redirect-hash");
		}
		function hashRedirectPost() {
			router.post("/features/navigation/url-fragments/redirect-hash");
		}
		function preserveFragmentVisit() {
			router.visit("/features/navigation/url-fragments/preserve-redirect#my-fragment");
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "URL Fragments" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [
					createVNode(FeatureHeader_default, {
						title: "URL Fragments",
						docs: "the-basics/redirects#preserving-fragments",
						controller: "app/Http/Controllers/Feature/NavigationController.php#L147"
					}, {
						default: withCtx(() => [..._cache[0] || (_cache[0] = [createTextVNode(" Hash fragment support in redirects. Server-directed fragments and client-side fragment preservation. ", -1)])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						title: "Current URL",
						description: "The current page URL including any hash fragment."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_2, [createBaseVNode("div", _hoisted_3, [createVNode(unref(Badge_default), {
							variant: "outline",
							class: "font-mono text-xs"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(currentUrl.value), 1)]),
							_: 1
						}), __props.redirectedFrom ? (openBlock(), createBlock(unref(Badge_default), {
							key: 0,
							variant: "default",
							class: "text-xs"
						}, {
							default: withCtx(() => [createTextVNode("Redirected via " + toDisplayString(__props.redirectedFrom), 1)]),
							_: 1
						})) : createCommentVNode("", true)]), createBaseVNode("div", _hoisted_4, [_cache[1] || (_cache[1] = createBaseVNode("span", { class: "text-sm text-muted-foreground" }, "Timestamp:", -1)), createVNode(unref(Badge_default), {
							variant: "secondary",
							class: "font-mono text-xs"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.timestamp), 1)]),
							_: 1
						})])])]),
						_: 1
					}),
					createBaseVNode("div", _hoisted_5, [
						createVNode(FeatureCard_default, {
							title: "Hash Fragment Redirect",
							description: "Server redirects to a URL with a hash fragment. The middleware detects the fragment and ensures Inertia navigates to the correct URL including the hash."
						}, {
							default: withCtx(() => [createBaseVNode("div", _hoisted_6, [
								createBaseVNode("div", _hoisted_7, [createVNode(unref(Button_default), { onClick: hashRedirectGet }, {
									default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode(" GET Redirect with #hash ", -1)])]),
									_: 1
								}), createVNode(unref(Button_default), {
									variant: "outline",
									onClick: hashRedirectPost
								}, {
									default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" POST Redirect with #hash ", -1)])]),
									_: 1
								})]),
								_cache[4] || (_cache[4] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
									createTextVNode(" The server redirects to "),
									createBaseVNode("code", { class: "font-mono" }, "/url-fragments#server-section"),
									createTextVNode(". Check the URL bar after clicking. ")
								], -1)),
								createVNode(CodeBlock_default, {
									title: "Server",
									code: "return redirect('/page#section');\n// Middleware detects the hash fragment\n// Ensures the client navigates to the\n// URL with the fragment intact"
								})
							])]),
							_: 1
						}),
						createVNode(FeatureCard_default, { title: "Preserve Fragment" }, {
							description: withCtx(() => [..._cache[5] || (_cache[5] = [
								createTextVNode(" Navigate with a hash fragment through a server redirect. ", -1),
								createBaseVNode("code", { class: "text-xs" }, "preserveFragment()", -1),
								createTextVNode(" keeps the client-side fragment intact on the final URL. ", -1)
							])]),
							default: withCtx(() => [createBaseVNode("div", _hoisted_8, [
								createBaseVNode("div", _hoisted_9, [createVNode(unref(Button_default), { onClick: preserveFragmentVisit }, {
									default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" Visit with #my-fragment ", -1)])]),
									_: 1
								}), createVNode(unref(link_default), {
									href: "/features/navigation/url-fragments/preserve-redirect#link-fragment",
									class: "inline-flex h-9 items-center justify-center rounded-md border border-black/10 bg-background px-4 text-sm font-medium hover:bg-accent hover:text-accent-foreground dark:border-white/10"
								}, {
									default: withCtx(() => [..._cache[7] || (_cache[7] = [createTextVNode(" Link with #link-fragment ", -1)])]),
									_: 1
								})]),
								_cache[8] || (_cache[8] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " The server redirects to a different URL, but the original hash fragment survives. ", -1)),
								createVNode(CodeBlock_default, {
									title: "Server",
									code: "// Redirect source\nreturn redirect('/target')\n    ->preserveFragment();\n\n// Target page responds normally\n// Client applies #fragment from original URL"
								})
							])]),
							_: 1
						}),
						createVNode(FeatureCard_default, {
							"info-card": "",
							id: "server-section",
							title: "Server Section"
						}, {
							description: withCtx(() => [..._cache[9] || (_cache[9] = [
								createTextVNode(" This card has ", -1),
								createBaseVNode("code", { class: "text-xs" }, "id=\"server-section\"", -1),
								createTextVNode(". The hash redirect scrolls here. ", -1)
							])]),
							default: withCtx(() => [_cache[10] || (_cache[10] = createBaseVNode("p", { class: "text-sm text-muted-foreground" }, [
								createTextVNode(" If you arrived via the hash redirect buttons above, the URL should contain "),
								createBaseVNode("code", { class: "font-mono text-xs" }, "#server-section"),
								createTextVNode(" and the browser scrolled to this card. ")
							], -1))]),
							_: 1
						}),
						createVNode(FeatureCard_default, {
							"info-card": "",
							title: "Key Differences"
						}, {
							default: withCtx(() => [..._cache[11] || (_cache[11] = [createBaseVNode("div", { class: "space-y-3 text-xs" }, [createBaseVNode("div", { class: "rounded-md border border-black/10 p-2 dark:border-white/10" }, [createBaseVNode("p", { class: "font-semibold" }, "Hash Fragment Redirect"), createBaseVNode("p", { class: "text-muted-foreground" }, [
								createTextVNode(" The "),
								createBaseVNode("em", null, "server"),
								createTextVNode(" decides the hash fragment. Redirect response includes "),
								createBaseVNode("code", null, "#section"),
								createTextVNode(" in the URL. Middleware converts to SPA visit. ")
							])]), createBaseVNode("div", { class: "rounded-md border-2 border-primary p-2" }, [createBaseVNode("p", { class: "font-semibold" }, "preserveFragment"), createBaseVNode("p", { class: "text-muted-foreground" }, [
								createTextVNode(" The "),
								createBaseVNode("em", null, "client"),
								createTextVNode(" supplies the hash fragment. The server calls "),
								createBaseVNode("code", null, "->preserveFragment()"),
								createTextVNode(" on the redirect to keep the original fragment on the final URL. ")
							])])], -1)])]),
							_: 1
						}),
						createVNode(FeatureCard_default, {
							"info-card": "",
							class: "lg:col-span-2",
							title: "API Reference"
						}, {
							default: withCtx(() => [createBaseVNode("div", _hoisted_10, [
								createVNode(CodeBlock_default, {
									title: "Hash Redirect (server)",
									code: "// Standard Laravel redirect\nreturn redirect('/page#hash');\n\n// Middleware auto-detects hash\n// and navigates to the URL\n// with the fragment intact"
								}),
								createVNode(CodeBlock_default, {
									title: "preserveFragment (server)",
									code: "return redirect('/target')\n    ->preserveFragment();\n\n// Stores flag in session\n// Target page includes\n// preserveFragment: true"
								}),
								createVNode(CodeBlock_default, {
									title: "Client usage",
									code: "// Hash redirect. Automatic\nrouter.get('/redirect-with-hash')\n\n// preserveFragment\nrouter.visit('/redirect#frag')\n// #frag survives the redirect"
								})
							])]),
							_: 1
						})
					])
				])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { UrlFragments_default as default };

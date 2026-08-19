import { $t as openBlock, Mn as withCtx, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, o as link_default, pr as unref, tt as computed, yr as toDisplayString } from "./dist.js";
import { t as AppLogoIcon_default } from "./AppLogoIcon.js";
//#region resources/js/pages/ErrorPage.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex min-h-svh flex-col items-center justify-center gap-6 bg-background p-6 md:p-10" };
var _hoisted_2 = { class: "flex flex-col items-center gap-6 text-center" };
var _hoisted_3 = { class: "mb-1 flex h-9 w-9 items-center justify-center rounded-md" };
var _hoisted_4 = { class: "space-y-2" };
var _hoisted_5 = { class: "text-7xl font-bold text-foreground" };
var _hoisted_6 = { class: "text-xl font-medium text-foreground" };
var _hoisted_7 = { class: "text-sm text-muted-foreground" };
var _hoisted_8 = { class: "flex items-center gap-3" };
//#endregion
//#region resources/js/pages/ErrorPage.vue
var ErrorPage_default = /* @__PURE__ */ defineComponent({
	__name: "ErrorPage",
	props: { status: {} },
	setup(__props) {
		const props = __props;
		const errors = {
			403: {
				title: "Forbidden",
				description: "You don't have permission to access this page."
			},
			404: {
				title: "Not Found",
				description: "The page you're looking for doesn't exist."
			},
			419: {
				title: "Page Expired",
				description: "Your session has expired. Please refresh and try again."
			},
			429: {
				title: "Too Many Requests",
				description: "You've made too many requests. Please wait a moment and try again."
			},
			500: {
				title: "Server Error",
				description: "Something went wrong on our end. Please try again later."
			},
			503: {
				title: "Service Unavailable",
				description: "We're currently performing maintenance. Please check back soon."
			}
		};
		const goBack = () => window.history.back();
		const error = computed(() => errors[props.status] ?? {
			title: "Error",
			description: "An unexpected error occurred."
		});
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock("div", _hoisted_1, [createVNode(unref(head_default), { title: `${__props.status} - ${error.value.title}` }, null, 8, ["title"]), createBaseVNode("div", _hoisted_2, [
				createVNode(unref(link_default), {
					href: "/",
					class: "flex flex-col items-center gap-2 font-medium"
				}, {
					default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createVNode(AppLogoIcon_default, { class: "size-9 fill-current text-[var(--foreground)] dark:text-white" })])]),
					_: 1
				}),
				createBaseVNode("div", _hoisted_4, [
					createBaseVNode("p", _hoisted_5, toDisplayString(__props.status), 1),
					createBaseVNode("h1", _hoisted_6, toDisplayString(error.value.title), 1),
					createBaseVNode("p", _hoisted_7, toDisplayString(error.value.description), 1)
				]),
				createBaseVNode("div", _hoisted_8, [createBaseVNode("button", {
					onClick: goBack,
					class: "inline-flex items-center justify-center rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground shadow-xs transition-colors hover:bg-primary/90"
				}, " Go Back "), createVNode(unref(link_default), {
					href: "/",
					class: "inline-flex items-center justify-center rounded-md border border-input bg-background px-4 py-2 text-sm font-medium text-foreground shadow-xs transition-colors hover:bg-accent hover:text-accent-foreground"
				}, {
					default: withCtx(() => [..._cache[0] || (_cache[0] = [createTextVNode(" Go Home ", -1)])]),
					_: 1
				})])
			])]);
		};
	}
});
//#endregion
export { ErrorPage_default as default };

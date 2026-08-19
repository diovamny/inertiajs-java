import { $t as openBlock, G as Fragment, Mn as withCtx, an as renderList, at as createElementBlock, dt as createTextVNode, f as usePage, ft as createVNode, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, on as renderSlot, pr as unref, rt as createBlock, yr as toDisplayString, zt as mergeProps } from "./dist.js";
import { i as Card_default, n as CardHeader_default, r as CardContent_default, t as CardTitle_default } from "./CardTitle.js";
import { t as CardDescription_default } from "./CardDescription.js";
//#region resources/js/components/FeatureCard.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1$2 = {
	key: 0,
	class: "flex items-center justify-between"
};
var _hoisted_2$2 = {
	key: 0,
	class: "shrink-0 rounded bg-indigo-500/10 px-2 py-1 text-[10px] leading-none font-semibold text-indigo-500"
};
var _hoisted_3 = {
	key: 0,
	class: "shrink-0 rounded bg-indigo-500/10 px-2 py-1 text-[10px] leading-none font-semibold text-indigo-500"
};
//#endregion
//#region resources/js/components/FeatureCard.vue
var FeatureCard_default = /* @__PURE__ */ defineComponent({
	inheritAttrs: false,
	__name: "FeatureCard",
	props: {
		title: {},
		description: {},
		infoCard: { type: Boolean },
		badge: {}
	},
	setup(__props) {
		return (_ctx, _cache) => {
			return openBlock(), createBlock(unref(Card_default), mergeProps(_ctx.$attrs, { class: { "info-card": __props.infoCard } }), {
				default: withCtx(() => [createVNode(unref(CardHeader_default), null, {
					default: withCtx(() => [_ctx.$slots["header-action"] ? (openBlock(), createElementBlock("div", _hoisted_1$2, [createBaseVNode("div", null, [createVNode(unref(CardTitle_default), { class: "flex items-center gap-2" }, {
						default: withCtx(() => [createTextVNode(toDisplayString(__props.title) + " ", 1), __props.badge ? (openBlock(), createElementBlock("span", _hoisted_2$2, toDisplayString(__props.badge), 1)) : createCommentVNode("", true)]),
						_: 1
					}), __props.description || _ctx.$slots.description ? (openBlock(), createBlock(unref(CardDescription_default), { key: 0 }, {
						default: withCtx(() => [renderSlot(_ctx.$slots, "description", {}, () => [createTextVNode(toDisplayString(__props.description), 1)])]),
						_: 3
					})) : createCommentVNode("", true)]), renderSlot(_ctx.$slots, "header-action")])) : (openBlock(), createElementBlock(Fragment, { key: 1 }, [createVNode(unref(CardTitle_default), { class: "flex items-center gap-2" }, {
						default: withCtx(() => [createTextVNode(toDisplayString(__props.title) + " ", 1), __props.badge ? (openBlock(), createElementBlock("span", _hoisted_3, toDisplayString(__props.badge), 1)) : createCommentVNode("", true)]),
						_: 1
					}), __props.description || _ctx.$slots.description ? (openBlock(), createBlock(unref(CardDescription_default), { key: 0 }, {
						default: withCtx(() => [renderSlot(_ctx.$slots, "description", {}, () => [createTextVNode(toDisplayString(__props.description), 1)])]),
						_: 3
					})) : createCommentVNode("", true)], 64))]),
					_: 3
				}), createVNode(unref(CardContent_default), null, {
					default: withCtx(() => [renderSlot(_ctx.$slots, "default")]),
					_: 3
				})]),
				_: 3
			}, 16, ["class"]);
		};
	}
});
//#endregion
//#region resources/js/components/SourceLinks.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1$1 = { class: "mt-1 flex flex-wrap items-center gap-x-3 gap-y-1" };
var _hoisted_2$1 = ["href", "target"];
//#endregion
//#region resources/js/components/SourceLinks.vue
var SourceLinks_default = /* @__PURE__ */ defineComponent({
	__name: "SourceLinks",
	props: {
		docs: {},
		controller: {}
	},
	setup(__props) {
		const props = __props;
		const componentPath = `resources/js/pages/${usePage().component}.vue`;
		const isLocal = typeof __PROJECT_ROOT__ !== "undefined";
		function url(path) {
			if (isLocal) {
				const [file, line] = path.split("#L");
				return `vscode://file/${__PROJECT_ROOT__}/${file}${line ? `:${line}` : ""}`;
			}
			return `https://github.com/inertiajs/demo-v3/blob/main/${path}`;
		}
		const links = [
			props.docs ? {
				label: "Docs",
				href: `https://inertiajs.com/docs/v3/${props.docs}`,
				external: true
			} : null,
			{
				label: "Vue page",
				href: url(componentPath),
				external: !isLocal
			},
			props.controller ? {
				label: "Controller",
				href: url(props.controller),
				external: !isLocal
			} : null
		].filter(Boolean);
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock("div", _hoisted_1$1, [(openBlock(true), createElementBlock(Fragment, null, renderList(unref(links), (link) => {
				return openBlock(), createElementBlock("a", {
					key: link.label,
					href: link.href,
					target: link.external ? "_blank" : void 0,
					class: "inline-flex items-center gap-1 text-sm text-muted-foreground underline decoration-muted-foreground/50 hover:text-foreground hover:decoration-foreground"
				}, toDisplayString(link.label), 9, _hoisted_2$1);
			}), 128))]);
		};
	}
});
//#endregion
//#region resources/js/components/FeatureHeader.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "text-2xl font-semibold tracking-tight" };
var _hoisted_2 = { class: "text-muted-foreground" };
//#endregion
//#region resources/js/components/FeatureHeader.vue
var FeatureHeader_default = /* @__PURE__ */ defineComponent({
	__name: "FeatureHeader",
	props: {
		title: {},
		docs: {},
		controller: {}
	},
	setup(__props) {
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock("div", null, [
				createBaseVNode("h1", _hoisted_1, toDisplayString(__props.title), 1),
				createBaseVNode("p", _hoisted_2, [renderSlot(_ctx.$slots, "default")]),
				createVNode(SourceLinks_default, {
					docs: __props.docs,
					controller: __props.controller
				}, null, 8, ["docs", "controller"])
			]);
		};
	}
});
//#endregion
export { FeatureCard_default as n, FeatureHeader_default as t };

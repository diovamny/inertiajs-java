import { $t as openBlock, G as Fragment, Mn as withCtx, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, it as createCommentVNode, mt as defineComponent, n as deferred_default, nt as createBaseVNode, o as link_default, p as usePoll, pr as unref, rt as createBlock, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { i as Card_default, n as CardHeader_default, r as CardContent_default, t as CardTitle_default } from "./CardTitle.js";
import { t as CardDescription_default } from "./CardDescription.js";
import { t as Skeleton_default } from "./Skeleton.js";
//#region resources/js/pages/Crm/Dashboard.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-4 md:grid-cols-3" };
var _hoisted_3 = { class: "text-3xl font-semibold tracking-tight" };
var _hoisted_4 = { class: "text-3xl font-semibold tracking-tight" };
var _hoisted_5 = { class: "text-3xl font-semibold tracking-tight" };
var _hoisted_6 = {
	key: 0,
	class: "py-4 text-center text-sm text-muted-foreground"
};
var _hoisted_7 = {
	key: 1,
	class: "space-y-2"
};
var _hoisted_8 = { class: "flex-1 space-y-1" };
var _hoisted_9 = { class: "flex items-center gap-2" };
var _hoisted_10 = { class: "text-sm font-medium" };
var _hoisted_11 = { class: "line-clamp-2 text-sm text-muted-foreground" };
var _hoisted_12 = { class: "text-xs whitespace-nowrap text-muted-foreground" };
//#endregion
//#region resources/js/pages/Crm/Dashboard.vue
var Dashboard_default = /* @__PURE__ */ defineComponent({
	__name: "Dashboard",
	props: {
		recentActivity: {},
		totalContacts: {},
		totalOrganizations: {},
		recentNotesCount: {}
	},
	setup(__props) {
		const breadcrumbs = [{
			title: "Dashboard",
			href: "/dashboard"
		}];
		usePoll(3e4, { only: ["recentActivity"] });
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Dashboard" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createBaseVNode("div", _hoisted_2, [
					createVNode(unref(Card_default), null, {
						default: withCtx(() => [createVNode(unref(CardHeader_default), null, {
							default: withCtx(() => [createVNode(unref(CardTitle_default), { class: "text-sm font-medium" }, {
								default: withCtx(() => [..._cache[0] || (_cache[0] = [createTextVNode("Total Contacts", -1)])]),
								_: 1
							})]),
							_: 1
						}), createVNode(unref(CardContent_default), null, {
							default: withCtx(() => [createVNode(unref(deferred_default), { data: "totalContacts" }, {
								fallback: withCtx(() => [createVNode(unref(Skeleton_default), { class: "h-9 w-20" })]),
								default: withCtx(() => [createBaseVNode("div", _hoisted_3, toDisplayString(__props.totalContacts), 1)]),
								_: 1
							})]),
							_: 1
						})]),
						_: 1
					}),
					createVNode(unref(Card_default), null, {
						default: withCtx(() => [createVNode(unref(CardHeader_default), null, {
							default: withCtx(() => [createVNode(unref(CardTitle_default), { class: "text-sm font-medium" }, {
								default: withCtx(() => [..._cache[1] || (_cache[1] = [createTextVNode("Organizations", -1)])]),
								_: 1
							})]),
							_: 1
						}), createVNode(unref(CardContent_default), null, {
							default: withCtx(() => [createVNode(unref(deferred_default), { data: "totalOrganizations" }, {
								fallback: withCtx(() => [createVNode(unref(Skeleton_default), { class: "h-9 w-20" })]),
								default: withCtx(() => [createBaseVNode("div", _hoisted_4, toDisplayString(__props.totalOrganizations), 1)]),
								_: 1
							})]),
							_: 1
						})]),
						_: 1
					}),
					createVNode(unref(Card_default), null, {
						default: withCtx(() => [createVNode(unref(CardHeader_default), null, {
							default: withCtx(() => [createVNode(unref(CardTitle_default), { class: "text-sm font-medium" }, {
								default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode("Notes This Week", -1)])]),
								_: 1
							})]),
							_: 1
						}), createVNode(unref(CardContent_default), null, {
							default: withCtx(() => [createVNode(unref(deferred_default), { data: "recentNotesCount" }, {
								fallback: withCtx(() => [createVNode(unref(Skeleton_default), { class: "h-9 w-20" })]),
								default: withCtx(() => [createBaseVNode("div", _hoisted_5, toDisplayString(__props.recentNotesCount), 1)]),
								_: 1
							})]),
							_: 1
						})]),
						_: 1
					})
				]), createVNode(unref(Card_default), null, {
					default: withCtx(() => [createVNode(unref(CardHeader_default), null, {
						default: withCtx(() => [createVNode(unref(CardTitle_default), null, {
							default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode("Recent Activity", -1)])]),
							_: 1
						}), createVNode(unref(CardDescription_default), null, {
							default: withCtx(() => [..._cache[4] || (_cache[4] = [createTextVNode("Latest notes added across all contacts", -1)])]),
							_: 1
						})]),
						_: 1
					}), createVNode(unref(CardContent_default), null, {
						default: withCtx(() => [__props.recentActivity.length === 0 ? (openBlock(), createElementBlock("div", _hoisted_6, " No recent activity. ")) : (openBlock(), createElementBlock("div", _hoisted_7, [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.recentActivity, (note) => {
							return openBlock(), createElementBlock("div", {
								key: note.id,
								class: "flex items-start gap-4 rounded-lg bg-muted/30 px-4 py-3"
							}, [createBaseVNode("div", _hoisted_8, [createBaseVNode("div", _hoisted_9, [
								createBaseVNode("span", _hoisted_10, toDisplayString(note.user?.name), 1),
								_cache[5] || (_cache[5] = createBaseVNode("span", { class: "text-xs text-muted-foreground" }, "added a note on", -1)),
								note.contact ? (openBlock(), createBlock(unref(link_default), {
									key: 0,
									href: "/contacts/" + note.contact.id,
									class: "text-sm font-medium text-primary hover:underline"
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(note.contact.first_name) + " " + toDisplayString(note.contact.last_name), 1)]),
									_: 2
								}, 1032, ["href"])) : createCommentVNode("", true)
							]), createBaseVNode("p", _hoisted_11, toDisplayString(note.body), 1)]), createBaseVNode("time", _hoisted_12, toDisplayString(new Date(note.created_at ?? "").toLocaleDateString()), 1)]);
						}), 128))]))]),
						_: 1
					})]),
					_: 1
				})])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { Dashboard_default as default };

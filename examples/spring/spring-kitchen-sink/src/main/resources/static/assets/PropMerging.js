import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, gr as normalizeClass, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, tt as computed, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/DataLoading/PropMerging.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-3" };
var _hoisted_4 = { class: "flex flex-wrap gap-2" };
var _hoisted_5 = {
	key: 0,
	class: "space-y-2"
};
var _hoisted_6 = { class: "text-sm" };
var _hoisted_7 = {
	key: 1,
	class: "text-sm text-muted-foreground"
};
var _hoisted_8 = { class: "space-y-3" };
var _hoisted_9 = { class: "flex flex-wrap gap-2" };
var _hoisted_10 = {
	key: 0,
	class: "space-y-2"
};
var _hoisted_11 = { class: "text-sm" };
var _hoisted_12 = {
	key: 1,
	class: "text-sm text-muted-foreground"
};
var _hoisted_13 = { class: "space-y-3" };
var _hoisted_14 = { class: "flex flex-wrap gap-2" };
var _hoisted_15 = {
	key: 0,
	class: "space-y-2"
};
var _hoisted_16 = { class: "text-sm font-medium" };
var _hoisted_17 = { class: "ml-2 text-xs text-muted-foreground" };
var _hoisted_18 = {
	key: 1,
	class: "text-sm text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Features/DataLoading/PropMerging.vue
var PropMerging_default = /* @__PURE__ */ defineComponent({
	__name: "PropMerging",
	props: {
		notifications: {},
		activities: {},
		contacts: {},
		timestamp: {}
	},
	setup(__props) {
		const props = __props;
		const breadcrumbs = [{ title: "Data Loading" }, { title: "Prop Merging" }];
		function addNotification() {
			router.reload({ only: ["notifications", "timestamp"] });
		}
		function resetNotifications() {
			router.reload({
				reset: ["notifications"],
				only: ["notifications", "timestamp"]
			});
		}
		function addActivity() {
			router.reload({ only: ["activities", "timestamp"] });
		}
		function resetActivities() {
			router.reload({
				reset: ["activities"],
				only: ["activities", "timestamp"]
			});
		}
		function fetchNextContact() {
			router.reload({ only: ["contacts", "timestamp"] });
		}
		function resetContacts() {
			router.reload({
				reset: ["contacts"],
				only: ["contacts", "timestamp"]
			});
		}
		const latestContactTimestamp = computed(() => {
			if (!props.contacts.length) return null;
			return props.contacts.reduce((latest, contact) => contact.updated > latest ? contact.updated : latest, props.contacts[0].updated);
		});
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Prop Merging" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Prop Merging",
					docs: "data-props/merging-props",
					controller: "app/Http/Controllers/Feature/DataLoadingController.php#L113"
				}, {
					default: withCtx(() => [..._cache[0] || (_cache[0] = [
						createTextVNode(" Server-side merge strategies with ", -1),
						createBaseVNode("code", { class: "text-xs" }, "Inertia::merge()", -1),
						createTextVNode(". New items are appended instead of replacing during partial reloads. ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Notifications"
					}, {
						"header-action": withCtx(() => [createVNode(unref(Badge_default), { variant: "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.notifications.length) + " total", 1)]),
							_: 1
						})]),
						description: withCtx(() => [..._cache[1] || (_cache[1] = [createBaseVNode("code", { class: "text-xs" }, "Inertia::merge()", -1), createTextVNode(" appends new items to the existing array on each partial reload. ", -1)])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [createBaseVNode("div", _hoisted_4, [createVNode(unref(Button_default), {
							size: "sm",
							onClick: addNotification
						}, {
							default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode(" Add Notification ", -1)])]),
							_: 1
						}), createVNode(unref(Button_default), {
							size: "sm",
							variant: "outline",
							onClick: resetNotifications
						}, {
							default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode(" Reset ", -1)])]),
							_: 1
						})]), __props.notifications.length ? (openBlock(), createElementBlock("div", _hoisted_5, [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.notifications, (notification) => {
							return openBlock(), createElementBlock("div", {
								key: notification.id,
								class: "flex items-center justify-between rounded border border-black/10 px-3 py-2 dark:border-white/10"
							}, [createBaseVNode("span", _hoisted_6, toDisplayString(notification.message), 1), createVNode(unref(Badge_default), {
								variant: notification.type === "success" ? "default" : notification.type === "warning" ? "destructive" : "secondary",
								class: "text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(notification.type), 1)]),
								_: 2
							}, 1032, ["variant"])]);
						}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_7, " Click \"Add Notification\" to start appending. "))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "Prepend"
					}, {
						"header-action": withCtx(() => [createVNode(unref(Badge_default), { variant: "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.activities.length) + " total", 1)]),
							_: 1
						})]),
						description: withCtx(() => [..._cache[4] || (_cache[4] = [createBaseVNode("code", { class: "text-xs" }, "Inertia::merge([...])->prepend()", -1), createTextVNode(" adds new items to the beginning of the array. ", -1)])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_8, [createBaseVNode("div", _hoisted_9, [createVNode(unref(Button_default), {
							size: "sm",
							onClick: addActivity
						}, {
							default: withCtx(() => [..._cache[5] || (_cache[5] = [createTextVNode(" Log Activity ", -1)])]),
							_: 1
						}), createVNode(unref(Button_default), {
							size: "sm",
							variant: "outline",
							onClick: resetActivities
						}, {
							default: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" Reset ", -1)])]),
							_: 1
						})]), __props.activities.length ? (openBlock(), createElementBlock("div", _hoisted_10, [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.activities, (activity) => {
							return openBlock(), createElementBlock("div", {
								key: activity.id,
								class: "flex items-center justify-between rounded border border-black/10 px-3 py-2 dark:border-white/10"
							}, [createBaseVNode("span", _hoisted_11, toDisplayString(activity.action) + " " + toDisplayString(activity.subject), 1), createVNode(unref(Badge_default), {
								variant: "outline",
								class: "font-mono text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(activity.time), 1)]),
								_: 2
							}, 1024)]);
						}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_12, " Click \"Log Activity\" to start prepending. "))])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						title: "matchOn (Dedup by ID)"
					}, {
						"header-action": withCtx(() => [createVNode(unref(Badge_default), { variant: "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.contacts.length) + " / 5 contacts", 1)]),
							_: 1
						})]),
						description: withCtx(() => [..._cache[7] || (_cache[7] = [createBaseVNode("code", { class: "text-xs" }, "Inertia::merge([...])->matchOn('id')", -1), createTextVNode(". New items are appended and existing items are updated in place, all in a single merge. ", -1)])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_13, [
							createBaseVNode("div", _hoisted_14, [createVNode(unref(Button_default), {
								size: "sm",
								onClick: fetchNextContact
							}, {
								default: withCtx(() => [..._cache[8] || (_cache[8] = [createTextVNode(" Fetch Next Contact ", -1)])]),
								_: 1
							}), createVNode(unref(Button_default), {
								size: "sm",
								variant: "outline",
								onClick: resetContacts
							}, {
								default: withCtx(() => [..._cache[9] || (_cache[9] = [createTextVNode(" Reset ", -1)])]),
								_: 1
							})]),
							__props.contacts.length ? (openBlock(), createElementBlock("div", _hoisted_15, [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.contacts, (contact) => {
								return openBlock(), createElementBlock("div", {
									key: contact.id,
									class: normalizeClass(["flex items-center justify-between rounded px-3 py-2", contact.updated === latestContactTimestamp.value ? "border border-blue-200 bg-blue-50/50 dark:border-blue-800 dark:bg-blue-950/50" : "border border-black/10 dark:border-white/10"])
								}, [createBaseVNode("div", null, [createBaseVNode("span", _hoisted_16, toDisplayString(contact.name), 1), createBaseVNode("span", _hoisted_17, "ID: " + toDisplayString(contact.id), 1)]), createVNode(unref(Badge_default), {
									variant: "outline",
									class: "font-mono text-xs"
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(contact.updated), 1)]),
									_: 2
								}, 1024)], 2);
							}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_18, " Click \"Fetch Next Contact\" to start loading. ")),
							_cache[10] || (_cache[10] = createBaseVNode("div", { class: "rounded-lg border border-black/5 bg-neutral-50/80 p-3 font-mono text-xs dark:border-white/5 dark:bg-neutral-900/80" }, [createBaseVNode("p", null, [
								createTextVNode(" Each fetch returns all known contacts with fresh timestamps plus one new contact. "),
								createBaseVNode("code", null, "matchOn('id')"),
								createTextVNode(" handles both at once: new IDs are appended, existing IDs are updated in place (blue highlight). After 5 fetches the pool is full, so every fetch only updates. ")
							])], -1))
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
export { PropMerging_default as default };

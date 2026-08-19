import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, pr as unref, rr as ref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/DataLoading/PartialReloads.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "space-y-1" };
var _hoisted_5 = { class: "flex gap-3" };
var _hoisted_6 = { class: "flex items-center gap-4" };
var _hoisted_7 = { class: "font-mono text-sm" };
var _hoisted_8 = { class: "font-mono text-sm" };
var _hoisted_9 = { class: "space-y-4" };
var _hoisted_10 = { class: "space-y-2" };
var _hoisted_11 = { class: "flex flex-wrap gap-2" };
var _hoisted_12 = { class: "space-y-2" };
var _hoisted_13 = { class: "flex flex-wrap gap-2" };
var _hoisted_14 = { class: "space-y-2" };
var _hoisted_15 = {
	key: 0,
	class: "space-y-1"
};
var _hoisted_16 = {
	key: 1,
	class: "text-xs text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Features/DataLoading/PartialReloads.vue
var PartialReloads_default = /* @__PURE__ */ defineComponent({
	__name: "PartialReloads",
	props: {
		users: {},
		stats: {},
		timestamp: {},
		randomNumber: {}
	},
	setup(__props) {
		const breadcrumbs = [{ title: "Data Loading" }, { title: "Partial Reloads" }];
		const eventLog = ref([]);
		function log(message) {
			eventLog.value.unshift(`${(/* @__PURE__ */ new Date()).toLocaleTimeString()} - ${message}`);
			if (eventLog.value.length > 10) eventLog.value.pop();
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Partial Reloads" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Partial Reloads",
					docs: "data-props/partial-reloads",
					controller: "app/Http/Controllers/Feature/DataLoadingController.php#L39"
				}, {
					default: withCtx(() => [..._cache[7] || (_cache[7] = [
						createTextVNode(" Selectively reload specific props using ", -1),
						createBaseVNode("code", { class: "text-xs" }, "only", -1),
						createTextVNode(" and ", -1),
						createBaseVNode("code", { class: "text-xs" }, "except", -1),
						createTextVNode(". ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [
					createVNode(FeatureCard_default, {
						title: "Current Props",
						description: "Each prop updates independently when reloaded."
					}, {
						default: withCtx(() => [createBaseVNode("div", _hoisted_3, [
							createBaseVNode("div", null, [_cache[8] || (_cache[8] = createBaseVNode("h4", { class: "mb-1 text-sm font-semibold" }, "Users", -1)), createBaseVNode("div", _hoisted_4, [(openBlock(true), createElementBlock(Fragment, null, renderList(__props.users, (user) => {
								return openBlock(), createElementBlock("div", {
									key: user.id,
									class: "flex items-center justify-between rounded bg-muted/50 px-2 py-1 text-sm"
								}, [createBaseVNode("span", null, toDisplayString(user.name), 1), createVNode(unref(Badge_default), {
									variant: "outline",
									class: "text-xs"
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(user.role), 1)]),
									_: 2
								}, 1024)]);
							}), 128))])]),
							createBaseVNode("div", null, [_cache[9] || (_cache[9] = createBaseVNode("h4", { class: "mb-1 text-sm font-semibold" }, "Stats", -1)), createBaseVNode("div", _hoisted_5, [createVNode(unref(Badge_default), { variant: "secondary" }, {
								default: withCtx(() => [createTextVNode("Total: " + toDisplayString(__props.stats.total), 1)]),
								_: 1
							}), createVNode(unref(Badge_default), { variant: "secondary" }, {
								default: withCtx(() => [createTextVNode("Favorites: " + toDisplayString(__props.stats.favorites), 1)]),
								_: 1
							})])]),
							createBaseVNode("div", _hoisted_6, [createBaseVNode("div", null, [_cache[10] || (_cache[10] = createBaseVNode("span", { class: "text-xs text-muted-foreground" }, "Timestamp", -1)), createBaseVNode("p", _hoisted_7, toDisplayString(__props.timestamp), 1)]), createBaseVNode("div", null, [_cache[11] || (_cache[11] = createBaseVNode("span", { class: "text-xs text-muted-foreground" }, "Random", -1)), createBaseVNode("p", _hoisted_8, toDisplayString(__props.randomNumber), 1)])])
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, { title: "Reload Controls" }, {
						description: withCtx(() => [..._cache[12] || (_cache[12] = [
							createTextVNode(" Use ", -1),
							createBaseVNode("code", { class: "text-xs" }, "only", -1),
							createTextVNode(" and ", -1),
							createBaseVNode("code", { class: "text-xs" }, "except", -1),
							createTextVNode(" to target specific props. ", -1)
						])]),
						default: withCtx(() => [createBaseVNode("div", _hoisted_9, [
							createBaseVNode("div", _hoisted_10, [_cache[16] || (_cache[16] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " router.reload({ only: [...] }) ", -1)), createBaseVNode("div", _hoisted_11, [
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[0] || (_cache[0] = ($event) => unref(router).reload({
										only: ["timestamp", "randomNumber"],
										onSuccess: () => log("Reloaded: timestamp + randomNumber")
									}))
								}, {
									default: withCtx(() => [..._cache[13] || (_cache[13] = [createTextVNode(" only: timestamp + random ", -1)])]),
									_: 1
								}),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[1] || (_cache[1] = ($event) => unref(router).reload({
										only: ["stats"],
										onSuccess: () => log("Reloaded: stats")
									}))
								}, {
									default: withCtx(() => [..._cache[14] || (_cache[14] = [createTextVNode(" only: stats ", -1)])]),
									_: 1
								}),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[2] || (_cache[2] = ($event) => unref(router).reload({
										only: ["users"],
										onSuccess: () => log("Reloaded: users")
									}))
								}, {
									default: withCtx(() => [..._cache[15] || (_cache[15] = [createTextVNode(" only: users ", -1)])]),
									_: 1
								})
							])]),
							createBaseVNode("div", _hoisted_12, [_cache[19] || (_cache[19] = createBaseVNode("h4", { class: "text-sm font-semibold" }, " router.reload({ except: [...] }) ", -1)), createBaseVNode("div", _hoisted_13, [createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[3] || (_cache[3] = ($event) => unref(router).reload({
									except: ["users"],
									onSuccess: () => log("Reloaded all except: users")
								}))
							}, {
								default: withCtx(() => [..._cache[17] || (_cache[17] = [createTextVNode(" except: users ", -1)])]),
								_: 1
							}), createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[4] || (_cache[4] = ($event) => unref(router).reload({
									except: ["stats", "users"],
									onSuccess: () => log("Reloaded all except: stats + users")
								}))
							}, {
								default: withCtx(() => [..._cache[18] || (_cache[18] = [createTextVNode(" except: stats + users ", -1)])]),
								_: 1
							})])]),
							createBaseVNode("div", _hoisted_14, [_cache[21] || (_cache[21] = createBaseVNode("h4", { class: "text-sm font-semibold" }, "Full Reload", -1)), createVNode(unref(Button_default), { onClick: _cache[5] || (_cache[5] = ($event) => unref(router).reload({ onSuccess: () => log("Full reload") })) }, {
								default: withCtx(() => [..._cache[20] || (_cache[20] = [createTextVNode(" Reload All Props ", -1)])]),
								_: 1
							})])
						])]),
						_: 1
					}),
					createVNode(FeatureCard_default, {
						"info-card": "",
						class: "lg:col-span-2",
						title: "Event Log"
					}, {
						"header-action": withCtx(() => [createVNode(unref(Button_default), {
							variant: "ghost",
							size: "sm",
							onClick: _cache[6] || (_cache[6] = ($event) => eventLog.value = [])
						}, {
							default: withCtx(() => [..._cache[22] || (_cache[22] = [createTextVNode("Clear", -1)])]),
							_: 1
						})]),
						default: withCtx(() => [eventLog.value.length ? (openBlock(), createElementBlock("div", _hoisted_15, [(openBlock(true), createElementBlock(Fragment, null, renderList(eventLog.value, (entry, i) => {
							return openBlock(), createElementBlock("div", {
								key: i,
								class: "rounded bg-muted px-2 py-1 font-mono text-xs"
							}, toDisplayString(entry), 1);
						}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_16, " Click buttons above to see reload events. "))]),
						_: 1
					})
				])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { PartialReloads_default as default };

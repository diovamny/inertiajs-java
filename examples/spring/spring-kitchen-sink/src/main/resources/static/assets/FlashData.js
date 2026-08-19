import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, Dn as watch, G as Fragment, Mn as withCtx, Sr as router, Zt as onUnmounted, an as renderList, at as createElementBlock, dt as createTextVNode, f as usePage, ft as createVNode, gr as normalizeClass, i as head_default, it as createCommentVNode, mt as defineComponent, nt as createBaseVNode, pr as unref, rr as ref, rt as createBlock, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as CodeBlock_default } from "./CodeBlock.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/State/FlashData.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "flex items-center gap-2" };
var _hoisted_3 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_4 = { class: "space-y-4" };
var _hoisted_5 = { class: "flex flex-wrap gap-2" };
var _hoisted_6 = { class: "space-y-4" };
var _hoisted_7 = { class: "flex flex-wrap gap-2" };
var _hoisted_8 = { class: "space-y-4" };
var _hoisted_9 = { class: "flex flex-wrap gap-2" };
var _hoisted_10 = { class: "space-y-4" };
var _hoisted_11 = { class: "flex items-center gap-2" };
var _hoisted_12 = {
	key: 0,
	class: "space-y-1"
};
var _hoisted_13 = {
	key: 1,
	class: "text-xs text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Features/State/FlashData.vue
var FlashData_default = /* @__PURE__ */ defineComponent({
	__name: "FlashData",
	setup(__props) {
		const breadcrumbs = [{ title: "State Management" }, { title: "Flash Data" }];
		const page = usePage();
		const eventLog = ref([]);
		const flashListenerActive = ref(false);
		let removeFlashListener = null;
		function log(message) {
			eventLog.value.unshift(`${(/* @__PURE__ */ new Date()).toLocaleTimeString()} - ${message}`);
			if (eventLog.value.length > 15) eventLog.value.pop();
		}
		watch(() => page.flash, (flash) => {
			if (flash.message) log(`Flash received: "${flash.message}" (type: ${flash.type || "default"})`);
		}, { deep: true });
		function registerFlashListener() {
			if (removeFlashListener) return;
			removeFlashListener = router.on("flash", (event) => {
				const flash = event.detail.flash;
				log(`[router.on('flash')] keys: ${Object.keys(flash).join(", ")}`);
			});
			flashListenerActive.value = true;
			log("Flash event listener registered");
		}
		function unregisterFlashListener() {
			if (!removeFlashListener) return;
			removeFlashListener();
			removeFlashListener = null;
			flashListenerActive.value = false;
			log("Flash event listener removed");
		}
		onUnmounted(() => {
			removeFlashListener?.();
		});
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Flash Data" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [
					createVNode(FeatureHeader_default, {
						title: "Flash Data",
						docs: "data-props/flash-data",
						controller: "app/Http/Controllers/Feature/StateController.php#L16"
					}, {
						default: withCtx(() => [..._cache[8] || (_cache[8] = [
							createTextVNode(" One-time messages with ", -1),
							createBaseVNode("code", { class: "text-xs" }, "Inertia::flash()", -1),
							createTextVNode(" (server) and ", -1),
							createBaseVNode("code", { class: "text-xs" }, "router.flash()", -1),
							createTextVNode(" (client). ", -1)
						])]),
						_: 1
					}),
					unref(page).flash.message ? (openBlock(), createElementBlock("div", {
						key: 0,
						class: normalizeClass(["rounded-lg border p-3 text-sm", {
							"border-green-200 bg-green-50 text-green-800 dark:border-green-800 dark:bg-green-950 dark:text-green-200": unref(page).flash.type === "success",
							"border-red-200 bg-red-50 text-red-800 dark:border-red-800 dark:bg-red-950 dark:text-red-200": unref(page).flash.type === "error",
							"border-yellow-200 bg-yellow-50 text-yellow-800 dark:border-yellow-800 dark:bg-yellow-950 dark:text-yellow-200": unref(page).flash.type === "warning",
							"border-blue-200 bg-blue-50 text-blue-800 dark:border-blue-800 dark:bg-blue-950 dark:text-blue-200": !unref(page).flash.type
						}])
					}, [createBaseVNode("div", _hoisted_2, [createBaseVNode("span", null, toDisplayString(unref(page).flash.message), 1), createVNode(unref(Badge_default), {
						variant: "outline",
						class: "text-xs"
					}, {
						default: withCtx(() => [createTextVNode(toDisplayString(unref(page).flash.type || "default"), 1)]),
						_: 1
					})])], 2)) : createCommentVNode("", true),
					createBaseVNode("div", _hoisted_3, [
						createVNode(FeatureCard_default, { title: "Server-Side Flash" }, {
							description: withCtx(() => [..._cache[9] || (_cache[9] = [createBaseVNode("code", { class: "text-xs" }, "Inertia::flash('key', 'value')->back()", -1), createTextVNode(". Sends flash with a server request. ", -1)])]),
							default: withCtx(() => [createBaseVNode("div", _hoisted_4, [createBaseVNode("div", _hoisted_5, [
								createVNode(unref(Button_default), {
									size: "sm",
									onClick: _cache[0] || (_cache[0] = ($event) => unref(router).post("/features/state/flash-data", {}, { preserveScroll: true }))
								}, {
									default: withCtx(() => [..._cache[10] || (_cache[10] = [createTextVNode(" Success Flash ", -1)])]),
									_: 1
								}),
								createVNode(unref(Button_default), {
									variant: "destructive",
									size: "sm",
									onClick: _cache[1] || (_cache[1] = ($event) => unref(router).post("/features/state/flash-data/error", {}, { preserveScroll: true }))
								}, {
									default: withCtx(() => [..._cache[11] || (_cache[11] = [createTextVNode(" Error Flash ", -1)])]),
									_: 1
								}),
								createVNode(unref(Button_default), {
									variant: "outline",
									size: "sm",
									onClick: _cache[2] || (_cache[2] = ($event) => unref(router).post("/features/state/flash-data/warning", {}, { preserveScroll: true }))
								}, {
									default: withCtx(() => [..._cache[12] || (_cache[12] = [createTextVNode(" Warning Flash ", -1)])]),
									_: 1
								})
							]), _cache[13] || (_cache[13] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
								createTextVNode(" Each button sends a POST, the server responds with "),
								createBaseVNode("code", null, "Inertia::flash()"),
								createTextVNode(" and a redirect back. ")
							], -1))])]),
							_: 1
						}),
						createVNode(FeatureCard_default, { title: "Client-Side Flash" }, {
							description: withCtx(() => [..._cache[14] || (_cache[14] = [createBaseVNode("code", { class: "text-xs" }, "router.flash('key', 'value')", -1), createTextVNode(". Sets flash without a server request. ", -1)])]),
							default: withCtx(() => [createBaseVNode("div", _hoisted_6, [createBaseVNode("div", _hoisted_7, [createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[3] || (_cache[3] = ($event) => unref(router).flash({
									message: "Client-side flash!",
									type: "success"
								}))
							}, {
								default: withCtx(() => [..._cache[15] || (_cache[15] = [createTextVNode(" Client Flash (object) ", -1)])]),
								_: 1
							}), createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[4] || (_cache[4] = ($event) => unref(router).flash("message", "Hello from the client!"))
							}, {
								default: withCtx(() => [..._cache[16] || (_cache[16] = [createTextVNode(" Client Flash (key/value) ", -1)])]),
								_: 1
							})]), _cache[17] || (_cache[17] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
								createTextVNode(" No server request. Flash data is set directly on the client and available via "),
								createBaseVNode("code", null, "usePage().flash"),
								createTextVNode(". ")
							], -1))])]),
							_: 1
						}),
						createVNode(FeatureCard_default, { title: "Callback Flash" }, {
							description: withCtx(() => [..._cache[18] || (_cache[18] = [createBaseVNode("code", { class: "text-xs" }, "router.flash((current) => ({ ...current, key: value }))", -1), createTextVNode(". Merge with existing flash. ", -1)])]),
							default: withCtx(() => [createBaseVNode("div", _hoisted_8, [createBaseVNode("div", _hoisted_9, [createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[5] || (_cache[5] = ($event) => unref(router).flash((current) => ({
									...current,
									counter: (current.counter || 0) + 1
								})))
							}, {
								default: withCtx(() => [..._cache[19] || (_cache[19] = [createTextVNode(" Increment Counter ", -1)])]),
								_: 1
							}), createVNode(unref(Button_default), {
								variant: "outline",
								size: "sm",
								onClick: _cache[6] || (_cache[6] = ($event) => unref(router).flash((current) => ({
									...current,
									message: "Merged!",
									type: "success"
								})))
							}, {
								default: withCtx(() => [..._cache[20] || (_cache[20] = [createTextVNode(" Merge Message ", -1)])]),
								_: 1
							})]), _cache[21] || (_cache[21] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, " The callback receives current flash data and returns the new flash state, enabling incremental updates. ", -1))])]),
							_: 1
						}),
						createVNode(FeatureCard_default, { title: "Flash Event Listener" }, {
							description: withCtx(() => [..._cache[22] || (_cache[22] = [createBaseVNode("code", { class: "text-xs" }, "router.on('flash', callback)", -1), createTextVNode(". Fires whenever flash data is received. ", -1)])]),
							default: withCtx(() => [createBaseVNode("div", _hoisted_10, [createBaseVNode("div", _hoisted_11, [createVNode(unref(Badge_default), {
								variant: flashListenerActive.value ? "default" : "secondary",
								class: "text-xs"
							}, {
								default: withCtx(() => [createTextVNode(toDisplayString(flashListenerActive.value ? "Active" : "Inactive"), 1)]),
								_: 1
							}, 8, ["variant"]), !flashListenerActive.value ? (openBlock(), createBlock(unref(Button_default), {
								key: 0,
								size: "sm",
								onClick: registerFlashListener
							}, {
								default: withCtx(() => [..._cache[23] || (_cache[23] = [createTextVNode("Register", -1)])]),
								_: 1
							})) : (openBlock(), createBlock(unref(Button_default), {
								key: 1,
								variant: "outline",
								size: "sm",
								onClick: unregisterFlashListener
							}, {
								default: withCtx(() => [..._cache[24] || (_cache[24] = [createTextVNode("Remove", -1)])]),
								_: 1
							}))]), _cache[25] || (_cache[25] = createBaseVNode("p", { class: "text-xs text-muted-foreground" }, [
								createTextVNode(" Register the listener, then trigger any flash. The event log will show entries from both the "),
								createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "watch"),
								createTextVNode(" and the "),
								createBaseVNode("code", { class: "rounded bg-muted px-1 py-0.5 text-xs" }, "router.on('flash')"),
								createTextVNode(" listener. ")
							], -1))])]),
							_: 1
						}),
						createVNode(FeatureCard_default, {
							"info-card": "",
							title: "Current Flash State"
						}, {
							description: withCtx(() => [..._cache[26] || (_cache[26] = [createBaseVNode("code", { class: "text-xs" }, "usePage().flash", -1), createTextVNode(". Reactive access to current flash data. ", -1)])]),
							default: withCtx(() => [createVNode(CodeBlock_default, { code: JSON.stringify(unref(page).flash, null, 2) }, null, 8, ["code"]), _cache[27] || (_cache[27] = createBaseVNode("p", { class: "mt-2 text-xs text-muted-foreground" }, " Flash data is one-time. It clears after being sent to the client and is not persisted in browser history state. ", -1))]),
							_: 1
						}),
						createVNode(FeatureCard_default, {
							"info-card": "",
							title: "Event Log"
						}, {
							"header-action": withCtx(() => [createVNode(unref(Button_default), {
								variant: "ghost",
								size: "sm",
								onClick: _cache[7] || (_cache[7] = ($event) => eventLog.value = [])
							}, {
								default: withCtx(() => [..._cache[28] || (_cache[28] = [createTextVNode("Clear", -1)])]),
								_: 1
							})]),
							default: withCtx(() => [eventLog.value.length ? (openBlock(), createElementBlock("div", _hoisted_12, [(openBlock(true), createElementBlock(Fragment, null, renderList(eventLog.value, (entry, i) => {
								return openBlock(), createElementBlock("div", {
									key: i,
									class: "rounded bg-muted px-2 py-1 font-mono text-xs"
								}, toDisplayString(entry), 1);
							}), 128))])) : (openBlock(), createElementBlock("p", _hoisted_13, " Trigger a flash to see events. "))]),
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
export { FlashData_default as default };

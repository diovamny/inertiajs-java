import { s as Button_default } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Zt as onUnmounted, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, mt as defineComponent, nt as createBaseVNode, p as usePoll, pr as unref, rr as ref, yr as toDisplayString } from "./dist.js";
import { t as AppLayout_default } from "./AppLayout.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region resources/js/pages/Features/DataLoading/Polling.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-4" };
var _hoisted_4 = { class: "flex items-center gap-3" };
var _hoisted_5 = { class: "space-y-2" };
var _hoisted_6 = { class: "flex flex-wrap gap-2" };
var _hoisted_7 = { class: "text-xs text-muted-foreground" };
var _hoisted_8 = { class: "grid grid-cols-2 gap-3 text-sm" };
var _hoisted_9 = { class: "space-y-4" };
var _hoisted_10 = { class: "space-y-3" };
var _hoisted_11 = { class: "flex items-center justify-between" };
var _hoisted_12 = { class: "flex items-center justify-between" };
var _hoisted_13 = { class: "flex items-center justify-between" };
//#endregion
//#region resources/js/pages/Features/DataLoading/Polling.vue
var Polling_default = /* @__PURE__ */ defineComponent({
	__name: "Polling",
	props: {
		currentTime: {},
		randomNumber: {},
		contactCount: {}
	},
	setup(__props) {
		const breadcrumbs = [{ title: "Data Loading" }, { title: "Polling" }];
		const pollMode = ref("overlap");
		const pollCount = ref(0);
		const inFlight = ref(0);
		const isPolling = ref(false);
		const requestOptions = {
			onStart: () => inFlight.value++,
			onFinish: () => {
				inFlight.value = Math.max(0, inFlight.value - 1);
				pollCount.value++;
			}
		};
		const polls = {
			overlap: usePoll(2e3, requestOptions, {
				autoStart: false,
				mode: "overlap"
			}),
			cancel: usePoll(2e3, requestOptions, {
				autoStart: false,
				mode: "cancel"
			}),
			rest: usePoll(2e3, requestOptions, {
				autoStart: false,
				mode: "rest"
			})
		};
		function stopAll() {
			Object.values(polls).forEach((p) => p.stop());
		}
		function togglePolling() {
			if (isPolling.value) {
				stopAll();
				isPolling.value = false;
			} else {
				polls[pollMode.value].start();
				isPolling.value = true;
			}
		}
		function setMode(mode) {
			if (pollMode.value === mode) return;
			const wasPolling = isPolling.value;
			if (wasPolling) stopAll();
			pollMode.value = mode;
			if (wasPolling) polls[mode].start();
		}
		onUnmounted(() => stopAll());
		const modes = [
			{
				value: "overlap",
				label: "overlap",
				description: "Default. Requests fire every interval, may stack up."
			},
			{
				value: "cancel",
				label: "cancel",
				description: "Aborts the in-flight request before starting the next."
			},
			{
				value: "rest",
				label: "rest",
				description: "Waits the interval after each response. No overlap."
			}
		];
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "Polling" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "Polling",
					docs: "data-props/polling",
					controller: "app/Http/Controllers/Feature/DataLoadingController.php#L114"
				}, {
					default: withCtx(() => [..._cache[0] || (_cache[0] = [
						createTextVNode(" Periodic data refreshing with ", -1),
						createBaseVNode("code", { class: "text-xs" }, "usePoll()", -1),
						createTextVNode(". The ", -1),
						createBaseVNode("code", { class: "text-xs" }, "mode", -1),
						createTextVNode(" option controls how concurrent requests are handled when responses take longer than the interval. ", -1)
					])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [createVNode(FeatureCard_default, { title: "Polling Controls" }, {
					description: withCtx(() => [..._cache[1] || (_cache[1] = [createTextVNode(" Server sleeps 500-3000ms per request so you can see the modes diverge. ", -1)])]),
					default: withCtx(() => [createBaseVNode("div", _hoisted_3, [
						createBaseVNode("div", _hoisted_4, [createVNode(unref(Button_default), {
							onClick: togglePolling,
							variant: isPolling.value ? "destructive" : "default"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(isPolling.value ? "Stop Polling" : "Start Polling"), 1)]),
							_: 1
						}, 8, ["variant"]), createVNode(unref(Badge_default), { variant: isPolling.value ? "default" : "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(isPolling.value ? "Active" : "Stopped"), 1)]),
							_: 1
						}, 8, ["variant"])]),
						createBaseVNode("div", _hoisted_5, [
							_cache[2] || (_cache[2] = createBaseVNode("p", { class: "flex items-center gap-2 text-sm font-medium" }, [createTextVNode(" Mode "), createBaseVNode("span", { class: "shrink-0 rounded bg-indigo-500/10 px-2 py-1 text-[10px] leading-none font-semibold text-indigo-500" }, "v3.2")], -1)),
							createBaseVNode("div", _hoisted_6, [(openBlock(), createElementBlock(Fragment, null, renderList(modes, (m) => {
								return createVNode(unref(Button_default), {
									key: m.value,
									size: "sm",
									variant: pollMode.value === m.value ? "default" : "outline",
									onClick: ($event) => setMode(m.value)
								}, {
									default: withCtx(() => [createTextVNode(toDisplayString(m.label), 1)]),
									_: 2
								}, 1032, ["variant", "onClick"]);
							}), 64))]),
							createBaseVNode("p", _hoisted_7, toDisplayString(modes.find((m) => m.value === pollMode.value)?.description), 1)
						]),
						createBaseVNode("div", _hoisted_8, [createBaseVNode("div", null, [_cache[3] || (_cache[3] = createTextVNode(" Poll count: ", -1)), createBaseVNode("strong", null, toDisplayString(pollCount.value), 1)]), createBaseVNode("div", null, [_cache[4] || (_cache[4] = createTextVNode(" In flight: ", -1)), createBaseVNode("strong", null, toDisplayString(inFlight.value), 1)])]),
						_cache[5] || (_cache[5] = createBaseVNode("div", { class: "rounded-lg border border-black/5 bg-neutral-50/80 p-3 font-mono text-xs dark:border-white/5 dark:bg-neutral-900/80" }, [createBaseVNode("p", null, [
							createBaseVNode("strong", null, "Tip:"),
							createTextVNode(" With "),
							createBaseVNode("code", null, "overlap"),
							createTextVNode(" and a slow server, watch "),
							createBaseVNode("em", null, "In flight"),
							createTextVNode(" climb past 1. "),
							createBaseVNode("code", null, "cancel"),
							createTextVNode(" keeps it at 1 by aborting, "),
							createBaseVNode("code", null, "rest"),
							createTextVNode(" keeps it at 1 by waiting the interval after each response. ")
						])], -1))
					])]),
					_: 1
				}), createVNode(FeatureCard_default, {
					"info-card": "",
					title: "Live Data",
					description: "These values refresh from the server on each poll cycle."
				}, {
					default: withCtx(() => [createBaseVNode("div", _hoisted_9, [createBaseVNode("div", _hoisted_10, [
						createBaseVNode("div", _hoisted_11, [_cache[6] || (_cache[6] = createBaseVNode("span", { class: "text-sm font-medium" }, "Server Time", -1)), createVNode(unref(Badge_default), {
							variant: "outline",
							class: "font-mono text-xs"
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.currentTime), 1)]),
							_: 1
						})]),
						createBaseVNode("div", _hoisted_12, [_cache[7] || (_cache[7] = createBaseVNode("span", { class: "text-sm font-medium" }, "Random Number", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.randomNumber), 1)]),
							_: 1
						})]),
						createBaseVNode("div", _hoisted_13, [_cache[8] || (_cache[8] = createBaseVNode("span", { class: "text-sm font-medium" }, "Contact Count", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(__props.contactCount), 1)]),
							_: 1
						})])
					])])]),
					_: 1
				})])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { Polling_default as default };

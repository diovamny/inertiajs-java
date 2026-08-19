const __vite__mapDeps=(i,m=__vite__mapDeps,d=(m.f||(m.f=["assets/Login.js","assets/createLucideIcon.js","assets/dist.js","assets/InputError.js","assets/Label.js","assets/check.js","assets/Input.js","assets/AppLogoIcon.js","assets/Create.js","assets/AppLayout.js","assets/useAppearance.js","assets/CardTitle.js","assets/Edit.js","assets/Index.js","assets/heart.js","assets/plus.js","assets/search.js","assets/badge.js","assets/Show.js","assets/CardDescription.js","assets/Skeleton.js","assets/Dashboard.js","assets/ErrorPage.js","assets/DeferredProps.js","assets/FeatureHeader.js","assets/FeatureHeader.css","assets/InfiniteScroll.js","assets/CodeBlock.js","assets/OnceProps.js","assets/OptionalProps.js","assets/PartialReloads.js","assets/Polling.js","assets/PropMerging.js","assets/WhenVisible.js","assets/HttpExceptions.js","assets/NetworkErrors.js","assets/GlobalEvents.js","assets/LocationEvent.js","assets/OnceEvents.js","assets/Progress.js","assets/VisitCallbacks.js","assets/DottedKeys.js","assets/FileUploads.js","assets/FormComponent.js","assets/OptimisticUpdates.js","assets/Precognition.js","assets/UseForm.js","assets/UseFormContext.js","assets/Validation.js","assets/UseHttp.js","assets/Head.js","assets/LayoutProps.js","assets/NestedLayouts.js","assets/PersistentLayouts.js","assets/PersistentDemoLayout.js","assets/PersistentLayoutsPageTwo.js","assets/AsyncRequests.js","assets/HistoryManagement.js","assets/InstantVisitTarget.js","assets/InstantVisits.js","assets/Links.js","assets/ManualVisits.js","assets/PreserveScroll.js","assets/PreserveState.js","assets/Redirects.js","assets/ScrollManagement.js","assets/UrlFragments.js","assets/ViewTransitions.js","assets/CacheManagement.js","assets/LinkPrefetch.js","assets/ManualPrefetch.js","assets/StaleWhileRevalidate.js","assets/FlashData.js","assets/Remember.js","assets/SharedProps.js","assets/Index2.js","assets/Show2.js"])))=>i.map(i=>d[i]);
import { t as createInertiaApp, xr as config } from "./dist.js";
import { t as initializeTheme } from "./useAppearance.js";
//#region \0vite/preload-helper.js
var scriptRel = "modulepreload";
var assetsURL = function(dep) {
	return "/" + dep;
};
var seen = {};
var __vitePreload = function preload(baseModule, deps, importerUrl) {
	let promise = Promise.resolve();
	if (deps && deps.length > 0) {
		const links = document.getElementsByTagName("link");
		const cspNonceMeta = document.querySelector("meta[property=csp-nonce]");
		const cspNonce = cspNonceMeta?.nonce || cspNonceMeta?.getAttribute("nonce");
		function allSettled(promises) {
			return Promise.all(promises.map((p) => Promise.resolve(p).then((value) => ({
				status: "fulfilled",
				value
			}), (reason) => ({
				status: "rejected",
				reason
			}))));
		}
		function importMetaResolve(specifier) {
			if (import.meta.resolve) return import.meta.resolve(specifier);
			return new URL(
				specifier,
				/** #__KEEP__ */
				import.meta.url
			).href;
		}
		promise = allSettled(deps.map((dep) => {
			dep = assetsURL(dep, importerUrl);
			dep = importMetaResolve(dep);
			if (dep in seen) return;
			seen[dep] = true;
			const isCss = dep.endsWith(".css");
			for (let i = links.length - 1; i >= 0; i--) {
				const link = links[i];
				if (link.href === dep && (!isCss || link.rel === "stylesheet")) return;
			}
			const link = document.createElement("link");
			link.rel = isCss ? "stylesheet" : scriptRel;
			if (!isCss) link.as = "script";
			link.crossOrigin = "";
			link.href = dep;
			if (cspNonce) link.setAttribute("nonce", cspNonce);
			document.head.appendChild(link);
			if (isCss) return new Promise((res, rej) => {
				link.addEventListener("load", res);
				link.addEventListener("error", () => rej(/* @__PURE__ */ new Error(`Unable to preload CSS for ${dep}`)));
			});
		}));
	}
	function handlePreloadError(err) {
		const e = new Event("vite:preloadError", { cancelable: true });
		e.payload = err;
		window.dispatchEvent(e);
		if (!e.defaultPrevented) throw err;
	}
	return promise.then((res) => {
		for (const item of res || []) {
			if (item.status !== "rejected") continue;
			handlePreloadError(item.reason);
		}
		return baseModule().catch(handlePreloadError);
	});
};
//#endregion
//#region resources/js/app.ts
var appName = "Inertia Kitchen Sink";
config.set("form.forceIndicesArrayFormatInFormData", false);
createInertiaApp({
	resolve: async (name, page) => {
		const pages = /* #__PURE__ */ Object.assign({
			"./pages/Auth/Login.vue": () => __vitePreload(() => import("./Login.js"), __vite__mapDeps([0,1,2,3,4,5,6,7])),
			"./pages/Contacts/Create.vue": () => __vitePreload(() => import("./Create.js"), __vite__mapDeps([8,1,2,3,9,10,7,4,6,11])),
			"./pages/Contacts/Edit.vue": () => __vitePreload(() => import("./Edit.js"), __vite__mapDeps([12,1,2,3,9,10,7,4,6,11])),
			"./pages/Contacts/Index.vue": () => __vitePreload(() => import("./Index.js"), __vite__mapDeps([13,1,2,9,10,7,14,15,16,6,17])),
			"./pages/Contacts/Show.vue": () => __vitePreload(() => import("./Show.js"), __vite__mapDeps([18,1,2,9,10,7,14,11,19,20,17])),
			"./pages/Crm/Dashboard.vue": () => __vitePreload(() => import("./Dashboard.js"), __vite__mapDeps([21,2,9,1,10,7,11,19,20])),
			"./pages/ErrorPage.vue": () => __vitePreload(() => import("./ErrorPage.js"), __vite__mapDeps([22,2,7])),
			"./pages/Features/DataLoading/DeferredProps.vue": () => __vitePreload(() => import("./DeferredProps.js"), __vite__mapDeps([23,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/DataLoading/InfiniteScroll.vue": () => __vitePreload(() => import("./InfiniteScroll.js"), __vite__mapDeps([26,1,2,9,10,7,27,5,17,24,11,19,25])),
			"./pages/Features/DataLoading/OnceProps.vue": () => __vitePreload(() => import("./OnceProps.js"), __vite__mapDeps([28,1,2,9,10,7,27,5,17,24,11,19,25])),
			"./pages/Features/DataLoading/OptionalProps.vue": () => __vitePreload(() => import("./OptionalProps.js"), __vite__mapDeps([29,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/DataLoading/PartialReloads.vue": () => __vitePreload(() => import("./PartialReloads.js"), __vite__mapDeps([30,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/DataLoading/Polling.vue": () => __vitePreload(() => import("./Polling.js"), __vite__mapDeps([31,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/DataLoading/PropMerging.vue": () => __vitePreload(() => import("./PropMerging.js"), __vite__mapDeps([32,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/DataLoading/WhenVisible.vue": () => __vitePreload(() => import("./WhenVisible.js"), __vite__mapDeps([33,2,9,1,10,7,17,24,11,19,25])),
			"./pages/Features/Errors/HttpExceptions.vue": () => __vitePreload(() => import("./HttpExceptions.js"), __vite__mapDeps([34,1,2,9,10,7,27,5,17,24,11,19,25])),
			"./pages/Features/Errors/NetworkErrors.vue": () => __vitePreload(() => import("./NetworkErrors.js"), __vite__mapDeps([35,1,2,9,10,7,27,5,17,24,11,19,25])),
			"./pages/Features/Events/GlobalEvents.vue": () => __vitePreload(() => import("./GlobalEvents.js"), __vite__mapDeps([36,1,2,9,10,7,27,5,17,24,11,19,25])),
			"./pages/Features/Events/LocationEvent.vue": () => __vitePreload(() => import("./LocationEvent.js"), __vite__mapDeps([37,1,2,9,10,7,27,5,24,11,19,25])),
			"./pages/Features/Events/OnceEvents.vue": () => __vitePreload(() => import("./OnceEvents.js"), __vite__mapDeps([38,1,2,9,10,7,27,5,17,24,11,19,25])),
			"./pages/Features/Events/Progress.vue": () => __vitePreload(() => import("./Progress.js"), __vite__mapDeps([39,1,2,9,10,7,27,5,17,24,11,19,25])),
			"./pages/Features/Events/VisitCallbacks.vue": () => __vitePreload(() => import("./VisitCallbacks.js"), __vite__mapDeps([40,1,2,9,10,7,27,5,24,11,19,25])),
			"./pages/Features/Forms/DottedKeys.vue": () => __vitePreload(() => import("./DottedKeys.js"), __vite__mapDeps([41,1,2,3,9,10,7,4,27,5,15,6,24,11,19,25])),
			"./pages/Features/Forms/FileUploads.vue": () => __vitePreload(() => import("./FileUploads.js"), __vite__mapDeps([42,1,2,3,9,10,7,4,17,24,11,19,25])),
			"./pages/Features/Forms/FormComponent.vue": () => __vitePreload(() => import("./FormComponent.js"), __vite__mapDeps([43,1,2,3,9,10,7,4,27,5,6,17,24,11,19,25])),
			"./pages/Features/Forms/OptimisticUpdates.vue": () => __vitePreload(() => import("./OptimisticUpdates.js"), __vite__mapDeps([44,1,2,9,10,7,27,5,14,24,11,19,25])),
			"./pages/Features/Forms/Precognition.vue": () => __vitePreload(() => import("./Precognition.js"), __vite__mapDeps([45,1,2,3,9,10,7,4,27,5,6,17,24,11,19,25])),
			"./pages/Features/Forms/UseForm.vue": () => __vitePreload(() => import("./UseForm.js"), __vite__mapDeps([46,1,2,3,9,10,7,4,27,5,6,17,24,11,19,25])),
			"./pages/Features/Forms/UseFormContext.vue": () => __vitePreload(() => import("./UseFormContext.js"), __vite__mapDeps([47,1,2,3,9,10,7,4,27,5,6,17,24,11,19,25])),
			"./pages/Features/Forms/Validation.vue": () => __vitePreload(() => import("./Validation.js"), __vite__mapDeps([48,1,2,3,9,10,7,4,27,5,6,17,24,11,19,25])),
			"./pages/Features/Http/UseHttp.vue": () => __vitePreload(() => import("./UseHttp.js"), __vite__mapDeps([49,1,2,9,10,7,4,27,5,6,17,24,11,19,25])),
			"./pages/Features/Layouts/Head.vue": () => __vitePreload(() => import("./Head.js"), __vite__mapDeps([50,2,9,1,10,7,4,27,5,6,17,24,11,19,25])),
			"./pages/Features/Layouts/LayoutProps.vue": () => __vitePreload(() => import("./LayoutProps.js"), __vite__mapDeps([51,1,2,9,10,7,4,27,5,6,24,11,19,25])),
			"./pages/Features/Layouts/NestedLayouts.vue": () => __vitePreload(() => import("./NestedLayouts.js"), __vite__mapDeps([52,2,9,1,10,7,27,5,24,11,19,25])),
			"./pages/Features/Layouts/PersistentLayouts.vue": () => __vitePreload(() => import("./PersistentLayouts.js"), __vite__mapDeps([53,1,2,27,5,54,9,10,7,17,24,11,19,25])),
			"./pages/Features/Layouts/PersistentLayoutsPageTwo.vue": () => __vitePreload(() => import("./PersistentLayoutsPageTwo.js"), __vite__mapDeps([55,2,4,1,54,9,10,7,17,24,11,19,25,6])),
			"./pages/Features/Navigation/AsyncRequests.vue": () => __vitePreload(() => import("./AsyncRequests.js"), __vite__mapDeps([56,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/Navigation/HistoryManagement.vue": () => __vitePreload(() => import("./HistoryManagement.js"), __vite__mapDeps([57,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/Navigation/InstantVisitTarget.vue": () => __vitePreload(() => import("./InstantVisitTarget.js"), __vite__mapDeps([58,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/Navigation/InstantVisits.vue": () => __vitePreload(() => import("./InstantVisits.js"), __vite__mapDeps([59,1,2,9,10,7,27,5,17,24,11,19,25])),
			"./pages/Features/Navigation/Links.vue": () => __vitePreload(() => import("./Links.js"), __vite__mapDeps([60,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/Navigation/ManualVisits.vue": () => __vitePreload(() => import("./ManualVisits.js"), __vite__mapDeps([61,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/Navigation/PreserveScroll.vue": () => __vitePreload(() => import("./PreserveScroll.js"), __vite__mapDeps([62,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/Navigation/PreserveState.vue": () => __vitePreload(() => import("./PreserveState.js"), __vite__mapDeps([63,1,2,9,10,7,6,17,24,11,19,25])),
			"./pages/Features/Navigation/Redirects.vue": () => __vitePreload(() => import("./Redirects.js"), __vite__mapDeps([64,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/Navigation/ScrollManagement.vue": () => __vitePreload(() => import("./ScrollManagement.js"), __vite__mapDeps([65,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/Navigation/UrlFragments.vue": () => __vitePreload(() => import("./UrlFragments.js"), __vite__mapDeps([66,1,2,9,10,7,27,5,17,24,11,19,25])),
			"./pages/Features/Navigation/ViewTransitions.vue": () => __vitePreload(() => import("./ViewTransitions.js"), __vite__mapDeps([67,1,2,9,10,7,24,11,19,25])),
			"./pages/Features/Prefetching/CacheManagement.vue": () => __vitePreload(() => import("./CacheManagement.js"), __vite__mapDeps([68,1,2,9,10,7,17,24,11,19,25])),
			"./pages/Features/Prefetching/LinkPrefetch.vue": () => __vitePreload(() => import("./LinkPrefetch.js"), __vite__mapDeps([69,2,9,1,10,7,24,11,19,25])),
			"./pages/Features/Prefetching/ManualPrefetch.vue": () => __vitePreload(() => import("./ManualPrefetch.js"), __vite__mapDeps([70,1,2,9,10,7,24,11,19,25])),
			"./pages/Features/Prefetching/StaleWhileRevalidate.vue": () => __vitePreload(() => import("./StaleWhileRevalidate.js"), __vite__mapDeps([71,2,9,1,10,7,24,11,19,25])),
			"./pages/Features/State/FlashData.vue": () => __vitePreload(() => import("./FlashData.js"), __vite__mapDeps([72,1,2,9,10,7,27,5,17,24,11,19,25])),
			"./pages/Features/State/Remember.vue": () => __vitePreload(() => import("./Remember.js"), __vite__mapDeps([73,1,2,9,10,7,4,6,17,24,11,19,25])),
			"./pages/Features/State/SharedProps.vue": () => __vitePreload(() => import("./SharedProps.js"), __vite__mapDeps([74,2,9,1,10,7,27,5,17,24,11,19,25])),
			"./pages/Organizations/Index.vue": () => __vitePreload(() => import("./Index2.js"), __vite__mapDeps([75,2,9,1,10,7,16,6,17])),
			"./pages/Organizations/Show.vue": () => __vitePreload(() => import("./Show2.js"), __vite__mapDeps([76,1,2,9,10,7,6,11,17]))
		});
		const module = await (pages[`./pages/${name}.vue`] || pages[`./Pages/${name}.vue`])?.();
		if (!module) throw new Error(`Page not found: ${name}`);
		return module.default ?? module;
	},
	title: (title) => title ? `${title} - ${appName}` : appName,
	defaults: { visitOptions: (href, options) => ({
		preserveScroll: options?.preserveScroll ?? "errors",
		...options
	}) }
});
initializeTheme();
//#endregion

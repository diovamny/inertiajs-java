import { s as Button_default, t as createLucideIcon } from "./createLucideIcon.js";
import { $t as openBlock, G as Fragment, Mn as withCtx, Sr as router, an as renderList, at as createElementBlock, dt as createTextVNode, ft as createVNode, i as head_default, it as createCommentVNode, l as useForm, mt as defineComponent, nt as createBaseVNode, pr as unref, rt as createBlock, vr as normalizeStyle, yr as toDisplayString, z as withModifiers } from "./dist.js";
import { t as InputError_default } from "./InputError.js";
import { r as X, t as AppLayout_default } from "./AppLayout.js";
import { t as Label_default } from "./Label.js";
import { t as Badge_default } from "./badge.js";
import { n as FeatureCard_default, t as FeatureHeader_default } from "./FeatureHeader.js";
//#region node_modules/lucide-vue-next/dist/esm/icons/upload.js
/**
* @license lucide-vue-next v0.468.0 - ISC
*
* This source code is licensed under the ISC license.
* See the LICENSE file in the root directory of this source tree.
*/
var Upload = createLucideIcon("UploadIcon", [
	["path", {
		d: "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4",
		key: "ih7n3h"
	}],
	["polyline", {
		points: "17 8 12 3 7 8",
		key: "t8dd8p"
	}],
	["line", {
		x1: "12",
		x2: "12",
		y1: "3",
		y2: "15",
		key: "widbto"
	}]
]);
//#endregion
//#region resources/js/pages/Features/Forms/FileUploads.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = { class: "flex h-full flex-1 flex-col gap-6 p-4" };
var _hoisted_2 = { class: "grid gap-6 lg:grid-cols-2" };
var _hoisted_3 = { class: "space-y-2" };
var _hoisted_4 = { class: "flex items-center gap-3" };
var _hoisted_5 = {
	for: "photo",
	class: "flex cursor-pointer items-center gap-2 rounded-md border border-dashed border-black/10 px-4 py-3 text-sm text-muted-foreground transition-colors hover:border-primary hover:text-primary dark:border-white/10"
};
var _hoisted_6 = {
	key: 0,
	class: "text-xs text-muted-foreground"
};
var _hoisted_7 = { class: "space-y-2" };
var _hoisted_8 = {
	for: "files",
	class: "flex cursor-pointer items-center gap-2 rounded-md border border-dashed border-black/10 px-4 py-3 text-sm text-muted-foreground transition-colors hover:border-primary hover:text-primary dark:border-white/10"
};
var _hoisted_9 = {
	key: 0,
	class: "space-y-1 pt-1"
};
var _hoisted_10 = { class: "truncate" };
var _hoisted_11 = { class: "flex items-center gap-2" };
var _hoisted_12 = { class: "text-xs text-muted-foreground" };
var _hoisted_13 = ["onClick"];
var _hoisted_14 = {
	key: 0,
	class: "space-y-1"
};
var _hoisted_15 = { class: "flex items-center justify-between text-sm" };
var _hoisted_16 = { class: "w-full rounded-full bg-secondary" };
var _hoisted_17 = { class: "space-y-6" };
var _hoisted_18 = { class: "space-y-3" };
var _hoisted_19 = { class: "flex items-center justify-between" };
var _hoisted_20 = { class: "flex items-center justify-between" };
var _hoisted_21 = { class: "flex items-center justify-between" };
var _hoisted_22 = { class: "flex items-center justify-between" };
var _hoisted_23 = { class: "flex items-center justify-between" };
var _hoisted_24 = {
	key: 0,
	class: "space-y-2 text-sm"
};
var _hoisted_25 = { key: 0 };
var _hoisted_26 = { key: 1 };
var _hoisted_27 = {
	key: 1,
	class: "text-sm text-muted-foreground"
};
//#endregion
//#region resources/js/pages/Features/Forms/FileUploads.vue
var FileUploads_default = /* @__PURE__ */ defineComponent({
	__name: "FileUploads",
	setup(__props) {
		const breadcrumbs = [{ title: "Forms" }, { title: "File Uploads" }];
		const form = useForm({
			photo: null,
			files: []
		});
		function onPhotoChange(event) {
			const target = event.target;
			form.photo = target.files?.[0] ?? null;
		}
		function onFilesChange(event) {
			const target = event.target;
			form.files = Array.from(target.files ?? []);
		}
		function removeFile(index) {
			form.files.splice(index, 1);
			if (form.files.length <= 5) form.clearErrors("files");
		}
		function submit() {
			const formData = new FormData();
			if (form.photo) formData.append("photo", form.photo);
			form.files.forEach((file) => {
				formData.append("files", file);
			});
			router.post("/features/forms/file-uploads", formData, {
				forceFormData: true,
				preserveScroll: true,
				onStart: () => {
					form.processing = true;
				},
				onProgress: (event) => {
					form.progress = event ?? null;
				},
				onSuccess: () => {
					form.processing = false;
					form.reset();
				},
				onError: (errors) => {
					form.processing = false;
					form.errors = errors;
				},
				onFinish: () => {
					form.processing = false;
				}
			});
		}
		function formatFileSize(bytes) {
			if (bytes < 1024) return bytes + " B";
			if (bytes < 1048576) return (bytes / 1024).toFixed(1) + " KB";
			return (bytes / 1048576).toFixed(1) + " MB";
		}
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createVNode(unref(head_default), { title: "File Uploads" }), createVNode(AppLayout_default, { breadcrumbs }, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_1, [createVNode(FeatureHeader_default, {
					title: "File Uploads",
					docs: "the-basics/file-uploads",
					controller: "app/Http/Controllers/Feature/FormController.php#L42"
				}, {
					default: withCtx(() => [..._cache[1] || (_cache[1] = [createTextVNode(" File upload handling with progress tracking using useForm. ", -1)])]),
					_: 1
				}), createBaseVNode("div", _hoisted_2, [createVNode(FeatureCard_default, {
					title: "Upload Files",
					description: "Single photo upload and multiple file uploads with progress tracking."
				}, {
					default: withCtx(() => [createBaseVNode("form", {
						class: "space-y-6",
						onSubmit: withModifiers(submit, ["prevent"])
					}, [
						createBaseVNode("div", _hoisted_3, [
							createVNode(unref(Label_default), { for: "photo" }, {
								default: withCtx(() => [..._cache[2] || (_cache[2] = [createTextVNode("Photo (Single Image)", -1)])]),
								_: 1
							}),
							createBaseVNode("div", _hoisted_4, [
								createBaseVNode("label", _hoisted_5, [createVNode(unref(Upload), { class: "size-4" }), createTextVNode(" " + toDisplayString(unref(form).photo ? unref(form).photo.name : "Choose image..."), 1)]),
								createBaseVNode("input", {
									id: "photo",
									type: "file",
									accept: "image/*",
									class: "hidden",
									onChange: onPhotoChange
								}, null, 32),
								unref(form).photo ? (openBlock(), createBlock(unref(Button_default), {
									key: 0,
									variant: "ghost",
									size: "sm",
									onClick: _cache[0] || (_cache[0] = ($event) => unref(form).photo = null)
								}, {
									default: withCtx(() => [createVNode(unref(X), { class: "size-4" })]),
									_: 1
								})) : createCommentVNode("", true)
							]),
							unref(form).photo ? (openBlock(), createElementBlock("p", _hoisted_6, toDisplayString(formatFileSize(unref(form).photo.size)), 1)) : createCommentVNode("", true),
							createVNode(InputError_default, { message: unref(form).errors.photo }, null, 8, ["message"])
						]),
						createBaseVNode("div", _hoisted_7, [
							createVNode(unref(Label_default), { for: "files" }, {
								default: withCtx(() => [..._cache[3] || (_cache[3] = [createTextVNode("Documents (Multiple Files)", -1)])]),
								_: 1
							}),
							createBaseVNode("div", null, [createBaseVNode("label", _hoisted_8, [createVNode(unref(Upload), { class: "size-4" }), _cache[4] || (_cache[4] = createTextVNode(" Choose files... (max 5) ", -1))]), createBaseVNode("input", {
								id: "files",
								type: "file",
								multiple: "",
								class: "hidden",
								onChange: onFilesChange
							}, null, 32)]),
							unref(form).files.length ? (openBlock(), createElementBlock("div", _hoisted_9, [(openBlock(true), createElementBlock(Fragment, null, renderList(unref(form).files, (file, index) => {
								return openBlock(), createElementBlock("div", {
									key: index,
									class: "flex items-center justify-between rounded bg-muted px-3 py-1.5 text-sm"
								}, [createBaseVNode("span", _hoisted_10, toDisplayString(file.name), 1), createBaseVNode("div", _hoisted_11, [createBaseVNode("span", _hoisted_12, toDisplayString(formatFileSize(file.size)), 1), createBaseVNode("button", {
									type: "button",
									class: "text-muted-foreground hover:text-foreground",
									onClick: ($event) => removeFile(index)
								}, [createVNode(unref(X), { class: "size-3" })], 8, _hoisted_13)])]);
							}), 128))])) : createCommentVNode("", true),
							createVNode(InputError_default, { message: unref(form).errors.files }, null, 8, ["message"])
						]),
						unref(form).progress ? (openBlock(), createElementBlock("div", _hoisted_14, [createBaseVNode("div", _hoisted_15, [_cache[5] || (_cache[5] = createBaseVNode("span", null, "Uploading...", -1)), createBaseVNode("span", null, toDisplayString(unref(form).progress.percentage) + "%", 1)]), createBaseVNode("div", _hoisted_16, [createBaseVNode("div", {
							class: "h-3 rounded-full bg-primary transition-all",
							style: normalizeStyle({ width: `${unref(form).progress.percentage}%` })
						}, null, 4)])])) : createCommentVNode("", true),
						createVNode(unref(Button_default), {
							type: "submit",
							disabled: unref(form).processing || !unref(form).photo && unref(form).files.length === 0
						}, {
							default: withCtx(() => [createTextVNode(toDisplayString(unref(form).processing ? "Uploading..." : "Upload"), 1)]),
							_: 1
						}, 8, ["disabled"])
					], 32)]),
					_: 1
				}), createBaseVNode("div", _hoisted_17, [createVNode(FeatureCard_default, {
					"info-card": "",
					title: "Upload State"
				}, {
					description: withCtx(() => [..._cache[6] || (_cache[6] = [createTextVNode(" Tip: If you're on a fast network, throttle it in DevTools (Network → Slow 3G) to see the progress percentage in action. ", -1)])]),
					default: withCtx(() => [createBaseVNode("div", _hoisted_18, [
						createBaseVNode("div", _hoisted_19, [_cache[7] || (_cache[7] = createBaseVNode("span", { class: "text-sm font-medium" }, "processing", -1)), createVNode(unref(Badge_default), { variant: unref(form).processing ? "default" : "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(unref(form).processing), 1)]),
							_: 1
						}, 8, ["variant"])]),
						createBaseVNode("div", _hoisted_20, [_cache[8] || (_cache[8] = createBaseVNode("span", { class: "text-sm font-medium" }, "progress", -1)), createVNode(unref(Badge_default), { variant: "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(unref(form).progress ? `${unref(form).progress.percentage}%` : "null"), 1)]),
							_: 1
						})]),
						createBaseVNode("div", _hoisted_21, [_cache[9] || (_cache[9] = createBaseVNode("span", { class: "text-sm font-medium" }, "isDirty", -1)), createVNode(unref(Badge_default), { variant: unref(form).isDirty ? "default" : "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(unref(form).isDirty), 1)]),
							_: 1
						}, 8, ["variant"])]),
						createBaseVNode("div", _hoisted_22, [_cache[10] || (_cache[10] = createBaseVNode("span", { class: "text-sm font-medium" }, "hasErrors", -1)), createVNode(unref(Badge_default), { variant: unref(form).hasErrors ? "destructive" : "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(unref(form).hasErrors), 1)]),
							_: 1
						}, 8, ["variant"])]),
						createBaseVNode("div", _hoisted_23, [_cache[11] || (_cache[11] = createBaseVNode("span", { class: "text-sm font-medium" }, "wasSuccessful", -1)), createVNode(unref(Badge_default), { variant: unref(form).wasSuccessful ? "default" : "secondary" }, {
							default: withCtx(() => [createTextVNode(toDisplayString(unref(form).wasSuccessful), 1)]),
							_: 1
						}, 8, ["variant"])])
					])]),
					_: 1
				}), createVNode(FeatureCard_default, {
					"info-card": "",
					title: "Selected Files"
				}, {
					default: withCtx(() => [unref(form).photo || unref(form).files.length ? (openBlock(), createElementBlock("div", _hoisted_24, [unref(form).photo ? (openBlock(), createElementBlock("div", _hoisted_25, [_cache[12] || (_cache[12] = createBaseVNode("span", { class: "font-medium" }, "Photo: ", -1)), createTextVNode(toDisplayString(unref(form).photo.name) + " (" + toDisplayString(formatFileSize(unref(form).photo.size)) + ") ", 1)])) : createCommentVNode("", true), unref(form).files.length ? (openBlock(), createElementBlock("div", _hoisted_26, [_cache[13] || (_cache[13] = createBaseVNode("span", { class: "font-medium" }, "Files: ", -1)), createTextVNode(toDisplayString(unref(form).files.length) + " selected ", 1)])) : createCommentVNode("", true)])) : (openBlock(), createElementBlock("p", _hoisted_27, " No files selected. "))]),
					_: 1
				})])])])]),
				_: 1
			})], 64);
		};
	}
});
//#endregion
export { FileUploads_default as default };

import { a as useVModel, c as cn } from "./createLucideIcon.js";
import { $t as openBlock, I as vModelText, Pn as withDirectives, Yn as isRef, at as createElementBlock, gr as normalizeClass, mt as defineComponent, pr as unref } from "./dist.js";
//#endregion
//#region resources/js/components/ui/input/Input.vue
var Input_default = /* @__PURE__ */ defineComponent({
	__name: "Input",
	props: {
		defaultValue: {},
		modelValue: {},
		class: { type: [
			Boolean,
			null,
			String,
			Object,
			Array
		] }
	},
	emits: ["update:modelValue"],
	setup(__props, { emit: __emit }) {
		const props = __props;
		const modelValue = useVModel(props, "modelValue", __emit, {
			passive: true,
			defaultValue: props.defaultValue
		});
		return (_ctx, _cache) => {
			return withDirectives((openBlock(), createElementBlock("input", {
				"onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => isRef(modelValue) ? modelValue.value = $event : null),
				autocomplete: "one-time-code",
				"data-slot": "input",
				class: normalizeClass(unref(cn)("file:text-foreground placeholder:text-muted-foreground selection:bg-primary selection:text-primary-foreground dark:bg-input/30 border-input/60 h-9 w-full min-w-0 rounded-md border bg-transparent px-3 py-1 text-base transition-[color,box-shadow] outline-none file:inline-flex file:h-7 file:border-0 file:bg-transparent file:text-sm file:font-medium disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 md:text-sm", "focus-visible:border-ring focus-visible:ring-ring/50 focus-visible:ring-[3px]", "aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive", props.class))
			}, null, 2)), [[vModelText, unref(modelValue)]]);
		};
	}
});
//#endregion
export { Input_default as t };

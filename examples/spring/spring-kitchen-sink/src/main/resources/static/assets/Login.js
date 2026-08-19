import { C as useForwardExpose, O as unrefElement, U as createContext, V as isNullish, _ as usePrimitiveElement, b as useId, c as cn, d as injectRovingFocusGroupContext, f as focusFirst, g as useCollection, h as VisuallyHidden_default, j as useVModel, m as wrapArray, o as reactiveOmit, p as getFocusIntent, s as Button_default, t as createLucideIcon, v as Primitive, x as useForwardPropsEmits, y as Presence_default } from "./createLucideIcon.js";
import { $t as openBlock, Bt as nextTick, Dn as watch, G as Fragment, Mn as withCtx, R as withKeys, St as getCurrentInstance, Zt as onUnmounted, _r as normalizeProps, an as renderList, at as createElementBlock, dr as toValue, dt as createTextVNode, ft as createVNode, gr as normalizeClass, i as head_default, it as createCommentVNode, ln as resolveDynamicComponent, mt as defineComponent, nt as createBaseVNode, o as link_default, on as renderSlot, pr as unref, qt as onMounted, r as form_default, rt as createBlock, tt as computed, vn as useAttrs, wt as guardReactiveProps, yr as toDisplayString, z as withModifiers, zt as mergeProps } from "./dist.js";
import { t as InputError_default } from "./InputError.js";
import { t as Label_default } from "./Label.js";
import { t as Check } from "./check.js";
import { t as Input_default } from "./Input.js";
import { t as AppLogoIcon_default } from "./AppLogoIcon.js";
//#region node_modules/ohash/dist/shared/ohash.D__AXeF1.mjs
function serialize(o) {
	return typeof o == "string" ? `'${o}'` : new c().serialize(o);
}
var c = /*@__PURE__*/ function() {
	class o {
		#t = /* @__PURE__ */ new Map();
		compare(t, r) {
			const e = typeof t, n = typeof r;
			return e === "string" && n === "string" ? t.localeCompare(r) : e === "number" && n === "number" ? t - r : String.prototype.localeCompare.call(this.serialize(t, true), this.serialize(r, true));
		}
		serialize(t, r) {
			if (t === null) return "null";
			switch (typeof t) {
				case "string": return r ? t : `'${t}'`;
				case "bigint": return `${t}n`;
				case "object": return this.$object(t);
				case "function": return this.$function(t);
			}
			return String(t);
		}
		serializeObject(t) {
			const r = Object.prototype.toString.call(t);
			if (r !== "[object Object]") return this.serializeBuiltInType(r.length < 10 ? `unknown:${r}` : r.slice(8, -1), t);
			const e = t.constructor, n = e === Object || e === void 0 ? "" : e.name;
			if (n !== "" && globalThis[n] === e) return this.serializeBuiltInType(n, t);
			if (typeof t.toJSON == "function") {
				const i = t.toJSON();
				return n + (i !== null && typeof i == "object" ? this.$object(i) : `(${this.serialize(i)})`);
			}
			return this.serializeObjectEntries(n, Object.entries(t));
		}
		serializeBuiltInType(t, r) {
			const e = this["$" + t];
			if (e) return e.call(this, r);
			if (typeof r?.entries == "function") return this.serializeObjectEntries(t, r.entries());
			throw new Error(`Cannot serialize ${t}`);
		}
		serializeObjectEntries(t, r) {
			const e = Array.from(r).sort((i, a) => this.compare(i[0], a[0]));
			let n = `${t}{`;
			for (let i = 0; i < e.length; i++) {
				const [a, l] = e[i];
				n += `${this.serialize(a, true)}:${this.serialize(l)}`, i < e.length - 1 && (n += ",");
			}
			return n + "}";
		}
		$object(t) {
			let r = this.#t.get(t);
			return r === void 0 && (this.#t.set(t, `#${this.#t.size}`), r = this.serializeObject(t), this.#t.set(t, r)), r;
		}
		$function(t) {
			const r = Function.prototype.toString.call(t);
			return r.slice(-15) === "[native code] }" ? `${t.name || ""}()[native]` : `${t.name}(${t.length})${r.replace(/\s*\n\s*/g, "")}`;
		}
		$Array(t) {
			let r = "[";
			for (let e = 0; e < t.length; e++) r += this.serialize(t[e]), e < t.length - 1 && (r += ",");
			return r + "]";
		}
		$Date(t) {
			try {
				return `Date(${t.toISOString()})`;
			} catch {
				return "Date(null)";
			}
		}
		$ArrayBuffer(t) {
			return `ArrayBuffer[${new Uint8Array(t).join(",")}]`;
		}
		$Set(t) {
			return `Set${this.$Array(Array.from(t).sort((r, e) => this.compare(r, e)))}`;
		}
		$Map(t) {
			return this.serializeObjectEntries("Map", t.entries());
		}
	}
	for (const s of [
		"Error",
		"RegExp",
		"URL"
	]) o.prototype["$" + s] = function(t) {
		return `${s}(${t})`;
	};
	for (const s of [
		"Int8Array",
		"Uint8Array",
		"Uint8ClampedArray",
		"Int16Array",
		"Uint16Array",
		"Int32Array",
		"Uint32Array",
		"Float32Array",
		"Float64Array"
	]) o.prototype["$" + s] = function(t) {
		return `${s}[${t.join(",")}]`;
	};
	for (const s of ["BigInt64Array", "BigUint64Array"]) o.prototype["$" + s] = function(t) {
		return `${s}[${t.join("n,")}${t.length > 0 ? "n" : ""}]`;
	};
	return o;
}();
function isEqual(object1, object2) {
	if (object1 === object2) return true;
	if (serialize(object1) === serialize(object2)) return true;
	return false;
}
//#endregion
//#region node_modules/reka-ui/dist/shared/isValueEqualOrExist.js
/**
* The function `isValueEqualOrExist` checks if a value is equal to or exists in another value or
* array.
* @param {T | T[] | undefined} base - It represents the base value that you want to compare with the `current` value.
* @param {T | T[] | undefined} current - The `current` parameter represents the current value that you want to compare with the `base` value or values.
* @returns The `isValueEqualOrExist` function returns a boolean value. It checks if the `base` value
* is equal to the `current` value or if the `current` value exists within the `base` value. The
* function handles cases where `base` can be a single value, an array of values, or undefined.
*/
function isValueEqualOrExist(base, current) {
	if (isNullish(base)) return false;
	if (Array.isArray(base)) return base.some((val) => isEqual(val, current));
	else return isEqual(base, current);
}
//#endregion
//#region node_modules/reka-ui/dist/shared/useFormControl.js
function useFormControl(el) {
	return computed(() => toValue(el) ? Boolean(unrefElement(el)?.closest("form")) : true);
}
//#endregion
//#region node_modules/reka-ui/dist/shared/useForwardScopeId.js
/**
* Returns the parent component's `<style scoped>` id (e.g. `data-v-xxxxxxx`) as a
* bindable attribute object, so it can be manually forwarded onto the chosen root
* element of a multi-root component.
*
* Vue only auto-applies the parent's scope id to a **single-root** component's root.
* When a component renders multiple root nodes (e.g. an interactive control plus a
* sibling hidden form input), that fallthrough is dropped and the parent's scoped
* styles can no longer reach the component. Spread the returned object onto the
* element that should stay styleable by the parent.
*
* @example
* ```ts
* const scopeIdAttrs = useForwardScopeId()
* // <Primitive v-bind="{ ...$attrs, ...scopeIdAttrs }" />
* ```
*/
function useForwardScopeId() {
	const scopeId = (getCurrentInstance()?.vnode)?.scopeId;
	return scopeId ? { [scopeId]: "" } : {};
}
//#endregion
//#region node_modules/reka-ui/dist/VisuallyHidden/VisuallyHiddenInputBubble.js
var VisuallyHiddenInputBubble_default = /* @__PURE__ */ defineComponent({
	inheritAttrs: false,
	__name: "VisuallyHiddenInputBubble",
	props: {
		name: {
			type: String,
			required: true
		},
		value: {
			type: null,
			required: true
		},
		checked: {
			type: Boolean,
			required: false,
			default: void 0
		},
		required: {
			type: Boolean,
			required: false
		},
		disabled: {
			type: Boolean,
			required: false
		},
		feature: {
			type: String,
			required: false,
			default: "fully-hidden"
		}
	},
	setup(__props) {
		const props = __props;
		const { primitiveElement, currentElement } = usePrimitiveElement();
		const valueState = computed(() => props.checked ?? props.value);
		watch(valueState, (cur, prev) => {
			if (!currentElement.value) return;
			const input = currentElement.value;
			const inputProto = window.HTMLInputElement.prototype;
			const setValue = Object.getOwnPropertyDescriptor(inputProto, "value").set;
			if (setValue && cur !== prev) {
				const inputEvent = new Event("input", { bubbles: true });
				const changeEvent = new Event("change", { bubbles: true });
				setValue.call(input, cur);
				input.dispatchEvent(inputEvent);
				input.dispatchEvent(changeEvent);
			}
		});
		return (_ctx, _cache) => {
			return openBlock(), createBlock(VisuallyHidden_default, mergeProps({
				ref_key: "primitiveElement",
				ref: primitiveElement
			}, {
				...props,
				..._ctx.$attrs
			}, { as: "input" }), null, 16);
		};
	}
});
//#endregion
//#region node_modules/reka-ui/dist/VisuallyHidden/VisuallyHiddenInput.js
var VisuallyHiddenInput_default = /* @__PURE__ */ defineComponent({
	inheritAttrs: false,
	__name: "VisuallyHiddenInput",
	props: {
		name: {
			type: String,
			required: true
		},
		value: {
			type: null,
			required: true
		},
		checked: {
			type: Boolean,
			required: false,
			default: void 0
		},
		required: {
			type: Boolean,
			required: false
		},
		disabled: {
			type: Boolean,
			required: false
		},
		feature: {
			type: String,
			required: false,
			default: "fully-hidden"
		}
	},
	setup(__props) {
		const props = __props;
		const isFormArrayEmptyAndRequired = computed(() => typeof props.value === "object" && Array.isArray(props.value) && props.value.length === 0 && props.required);
		const parsedValue = computed(() => {
			if (typeof props.value === "string" || typeof props.value === "number" || typeof props.value === "boolean" || props.value === null || props.value === void 0) return [{
				name: props.name,
				value: props.value
			}];
			else if (typeof props.value === "object" && Array.isArray(props.value)) return props.value.flatMap((obj, index) => {
				if (typeof obj === "object") return Object.entries(obj).map(([key, value]) => ({
					name: `${props.name}[${index}][${key}]`,
					value
				}));
				else return {
					name: `${props.name}[${index}]`,
					value: obj
				};
			});
			else if (props.value !== null && typeof props.value === "object" && !Array.isArray(props.value)) return Object.entries(props.value).map(([key, value]) => ({
				name: `${props.name}[${key}]`,
				value
			}));
			return [];
		});
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [createCommentVNode(" We render single input if it's required "), isFormArrayEmptyAndRequired.value ? (openBlock(), createBlock(VisuallyHiddenInputBubble_default, mergeProps({ key: _ctx.name }, {
				...props,
				..._ctx.$attrs
			}, {
				name: _ctx.name,
				value: _ctx.value
			}), null, 16, ["name", "value"])) : (openBlock(true), createElementBlock(Fragment, { key: 1 }, renderList(parsedValue.value, (parsed) => {
				return openBlock(), createBlock(VisuallyHiddenInputBubble_default, mergeProps({ key: parsed.name }, { ref_for: true }, {
					...props,
					..._ctx.$attrs
				}, {
					name: parsed.name,
					value: parsed.value
				}), null, 16, ["name", "value"]);
			}), 128))], 2112);
		};
	}
});
//#endregion
//#region node_modules/reka-ui/dist/RovingFocus/RovingFocusItem.js
var RovingFocusItem_default = /* @__PURE__ */ defineComponent({
	__name: "RovingFocusItem",
	props: {
		tabStopId: {
			type: String,
			required: false
		},
		focusable: {
			type: Boolean,
			required: false,
			default: true
		},
		active: {
			type: Boolean,
			required: false
		},
		allowShiftKey: {
			type: Boolean,
			required: false
		},
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false,
			default: "span"
		}
	},
	setup(__props) {
		const props = __props;
		const context = injectRovingFocusGroupContext();
		const randomId = useId();
		const id = computed(() => props.tabStopId || randomId);
		const isCurrentTabStop = computed(() => context.currentTabStopId.value === id.value);
		const { getItems, CollectionItem } = useCollection();
		onMounted(() => {
			if (props.focusable) context.onFocusableItemAdd();
		});
		onUnmounted(() => {
			if (props.focusable) context.onFocusableItemRemove();
		});
		watch(() => props.focusable, (newVal, oldVal) => {
			if (newVal === oldVal) return;
			if (newVal) context.onFocusableItemAdd();
			else context.onFocusableItemRemove();
		});
		function handleKeydown(event) {
			if (event.key === "Tab" && event.shiftKey) {
				context.onItemShiftTab();
				return;
			}
			if (event.target !== event.currentTarget) return;
			const focusIntent = getFocusIntent(event, context.orientation.value, context.dir.value);
			if (focusIntent !== void 0) {
				if (event.metaKey || event.ctrlKey || event.altKey || (props.allowShiftKey ? false : event.shiftKey)) return;
				event.preventDefault();
				let candidateNodes = [...getItems().map((i) => i.ref).filter((i) => i.dataset.disabled !== "")];
				if (focusIntent === "last") candidateNodes.reverse();
				else if (focusIntent === "prev" || focusIntent === "next") {
					if (focusIntent === "prev") candidateNodes.reverse();
					const currentIndex = candidateNodes.indexOf(event.currentTarget);
					candidateNodes = context.loop.value ? wrapArray(candidateNodes, currentIndex + 1) : candidateNodes.slice(currentIndex + 1);
				}
				nextTick(() => focusFirst(candidateNodes));
			}
		}
		return (_ctx, _cache) => {
			return openBlock(), createBlock(unref(CollectionItem), null, {
				default: withCtx(() => [createVNode(unref(Primitive), {
					tabindex: isCurrentTabStop.value ? 0 : -1,
					"data-orientation": unref(context).orientation.value,
					"data-active": _ctx.active ? "" : void 0,
					"data-disabled": !_ctx.focusable ? "" : void 0,
					as: _ctx.as,
					"as-child": _ctx.asChild,
					onMousedown: _cache[0] || (_cache[0] = (event) => {
						if (!_ctx.focusable) event.preventDefault();
						else unref(context).onItemFocus(id.value);
					}),
					onFocus: _cache[1] || (_cache[1] = ($event) => unref(context).onItemFocus(id.value)),
					onKeydown: handleKeydown
				}, {
					default: withCtx(() => [renderSlot(_ctx.$slots, "default")]),
					_: 3
				}, 8, [
					"tabindex",
					"data-orientation",
					"data-active",
					"data-disabled",
					"as",
					"as-child"
				])]),
				_: 3
			});
		};
	}
});
//#endregion
//#region node_modules/reka-ui/dist/Checkbox/CheckboxGroupRoot.js
var [injectCheckboxGroupRootContext, provideCheckboxGroupRootContext] = /*#__PURE__*/ createContext("CheckboxGroupRoot");
//#endregion
//#region node_modules/reka-ui/dist/Checkbox/utils.js
function isIndeterminate(checked) {
	return checked === "indeterminate";
}
function getState(checked) {
	return isIndeterminate(checked) ? "indeterminate" : checked ? "checked" : "unchecked";
}
//#endregion
//#region node_modules/reka-ui/dist/Checkbox/CheckboxRoot.js
var [injectCheckboxRootContext, provideCheckboxRootContext] = /*#__PURE__*/ createContext("CheckboxRoot");
var CheckboxRoot_default = /* @__PURE__ */ defineComponent({
	inheritAttrs: false,
	__name: "CheckboxRoot",
	props: {
		defaultValue: {
			type: null,
			required: false
		},
		modelValue: {
			type: null,
			required: false,
			default: void 0
		},
		disabled: {
			type: Boolean,
			required: false
		},
		value: {
			type: null,
			required: false,
			default: "on"
		},
		id: {
			type: String,
			required: false
		},
		trueValue: {
			type: null,
			required: false,
			default: () => true
		},
		falseValue: {
			type: null,
			required: false,
			default: () => false
		},
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false,
			default: "button"
		},
		name: {
			type: String,
			required: false
		},
		required: {
			type: Boolean,
			required: false
		}
	},
	emits: ["update:modelValue"],
	setup(__props, { emit: __emit }) {
		const props = __props;
		const emits = __emit;
		const { forwardRef, currentElement } = useForwardExpose();
		const checkboxGroupContext = injectCheckboxGroupRootContext(null);
		const modelValue = useVModel(props, "modelValue", emits, {
			defaultValue: props.defaultValue ?? props.falseValue,
			passive: props.modelValue === void 0
		});
		const disabled = computed(() => checkboxGroupContext?.disabled.value || props.disabled);
		const isChecked = computed(() => isEqual(modelValue.value, props.trueValue));
		const checkboxState = computed(() => {
			if (!isNullish(checkboxGroupContext?.modelValue.value)) return isValueEqualOrExist(checkboxGroupContext.modelValue.value, props.value);
			else {
				if (modelValue.value === "indeterminate") return "indeterminate";
				return isChecked.value;
			}
		});
		function handleClick() {
			if (!isNullish(checkboxGroupContext?.modelValue.value)) {
				const modelValueArray = [...checkboxGroupContext.modelValue.value || []];
				if (isValueEqualOrExist(modelValueArray, props.value)) {
					const index = modelValueArray.findIndex((i) => isEqual(i, props.value));
					modelValueArray.splice(index, 1);
				} else modelValueArray.push(props.value);
				checkboxGroupContext.modelValue.value = modelValueArray;
			} else if (modelValue.value === "indeterminate") modelValue.value = props.trueValue;
			else modelValue.value = isChecked.value ? props.falseValue : props.trueValue;
		}
		const isFormControl = useFormControl(currentElement);
		const scopeIdAttrs = useForwardScopeId();
		const attrs = useAttrs();
		const ariaLabel = computed(() => {
			if (attrs["aria-label"]) return void 0;
			return props.id && currentElement.value ? document.querySelector(`[for="${props.id}"]`)?.innerText : void 0;
		});
		provideCheckboxRootContext({
			disabled,
			state: checkboxState
		});
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock(Fragment, null, [(openBlock(), createBlock(resolveDynamicComponent(unref(checkboxGroupContext)?.rovingFocus.value ? unref(RovingFocusItem_default) : unref(Primitive)), mergeProps({
				..._ctx.$attrs,
				...unref(scopeIdAttrs)
			}, {
				id: _ctx.id,
				ref: unref(forwardRef),
				role: "checkbox",
				"as-child": _ctx.asChild,
				as: _ctx.as,
				type: _ctx.as === "button" ? "button" : void 0,
				"aria-checked": unref(isIndeterminate)(checkboxState.value) ? "mixed" : checkboxState.value,
				"aria-required": _ctx.required,
				"aria-label": _ctx.$attrs["aria-label"] || ariaLabel.value,
				"data-state": unref(getState)(checkboxState.value),
				"data-disabled": disabled.value ? "" : void 0,
				disabled: disabled.value,
				focusable: unref(checkboxGroupContext)?.rovingFocus.value ? !disabled.value : void 0,
				onKeydown: withKeys(withModifiers(() => {}, ["prevent"]), ["enter"]),
				onClick: handleClick
			}), {
				default: withCtx(() => [renderSlot(_ctx.$slots, "default", {
					modelValue: unref(modelValue),
					state: checkboxState.value
				})]),
				_: 3
			}, 16, [
				"id",
				"as-child",
				"as",
				"type",
				"aria-checked",
				"aria-required",
				"aria-label",
				"data-state",
				"data-disabled",
				"disabled",
				"focusable",
				"onKeydown"
			])), unref(isFormControl) && _ctx.name && !unref(checkboxGroupContext) ? (openBlock(), createBlock(unref(VisuallyHiddenInput_default), mergeProps({
				key: 0,
				type: "checkbox",
				checked: !!checkboxState.value,
				name: _ctx.name,
				value: _ctx.value,
				disabled: disabled.value,
				required: _ctx.required
			}, unref(scopeIdAttrs)), null, 16, [
				"checked",
				"name",
				"value",
				"disabled",
				"required"
			])) : createCommentVNode("v-if", true)], 64);
		};
	}
});
//#endregion
//#region node_modules/reka-ui/dist/Checkbox/CheckboxIndicator.js
var CheckboxIndicator_default = /* @__PURE__ */ defineComponent({
	__name: "CheckboxIndicator",
	props: {
		forceMount: {
			type: Boolean,
			required: false
		},
		asChild: {
			type: Boolean,
			required: false
		},
		as: {
			type: null,
			required: false,
			default: "span"
		}
	},
	setup(__props) {
		const { forwardRef } = useForwardExpose();
		const rootContext = injectCheckboxRootContext();
		return (_ctx, _cache) => {
			return openBlock(), createBlock(unref(Presence_default), { present: _ctx.forceMount || unref(isIndeterminate)(unref(rootContext).state.value) || unref(rootContext).state.value === true }, {
				default: withCtx(() => [createVNode(unref(Primitive), mergeProps({
					ref: unref(forwardRef),
					"data-state": unref(getState)(unref(rootContext).state.value),
					"data-disabled": unref(rootContext).disabled.value ? "" : void 0,
					style: { pointerEvents: "none" },
					"as-child": _ctx.asChild,
					as: _ctx.as
				}, _ctx.$attrs), {
					default: withCtx(() => [renderSlot(_ctx.$slots, "default")]),
					_: 3
				}, 16, [
					"data-state",
					"data-disabled",
					"as-child",
					"as"
				])]),
				_: 3
			}, 8, ["present"]);
		};
	}
});
//#endregion
//#region node_modules/lucide-vue-next/dist/esm/icons/loader-circle.js
/**
* @license lucide-vue-next v0.468.0 - ISC
*
* This source code is licensed under the ISC license.
* See the LICENSE file in the root directory of this source tree.
*/
var LoaderCircle = createLucideIcon("LoaderCircleIcon", [["path", {
	d: "M21 12a9 9 0 1 1-6.219-8.56",
	key: "13zald"
}]]);
//#endregion
//#region resources/js/components/ui/checkbox/Checkbox.vue
var Checkbox_default = /* @__PURE__ */ defineComponent({
	__name: "Checkbox",
	props: {
		defaultValue: {},
		modelValue: {},
		disabled: { type: Boolean },
		value: {},
		id: {},
		trueValue: {},
		falseValue: {},
		asChild: { type: Boolean },
		as: {},
		name: {},
		required: { type: Boolean },
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
		const emits = __emit;
		const delegatedProps = reactiveOmit(props, "class");
		const forwarded = useForwardPropsEmits(delegatedProps, emits);
		return (_ctx, _cache) => {
			return openBlock(), createBlock(unref(CheckboxRoot_default), mergeProps({ "data-slot": "checkbox" }, unref(forwarded), { class: unref(cn)("peer border-input/60 data-[state=checked]:bg-primary data-[state=checked]:text-primary-foreground data-[state=checked]:border-primary focus-visible:border-ring focus-visible:ring-ring/50 aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive size-4 shrink-0 rounded-[4px] border transition-shadow outline-none focus-visible:ring-[3px] disabled:cursor-not-allowed disabled:opacity-50", props.class) }), {
				default: withCtx((slotProps) => [createVNode(unref(CheckboxIndicator_default), {
					"data-slot": "checkbox-indicator",
					class: "grid place-content-center text-current transition-none"
				}, {
					default: withCtx(() => [renderSlot(_ctx.$slots, "default", normalizeProps(guardReactiveProps(slotProps)), () => [createVNode(unref(Check), { class: "size-3.5" })])]),
					_: 2
				}, 1024)]),
				_: 3
			}, 16, ["class"]);
		};
	}
});
//#endregion
//#region resources/js/components/ui/spinner/Spinner.vue
var Spinner_default = /* @__PURE__ */ defineComponent({
	__name: "Spinner",
	props: { class: { type: [
		Boolean,
		null,
		String,
		Object,
		Array
	] } },
	setup(__props) {
		const props = __props;
		return (_ctx, _cache) => {
			return openBlock(), createBlock(unref(LoaderCircle), {
				role: "status",
				"aria-label": "Loading",
				class: normalizeClass(unref(cn)("size-4 animate-spin", props.class))
			}, null, 8, ["class"]);
		};
	}
});
//#endregion
//#region resources/js/layouts/auth/AuthSimpleLayout.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1$1 = { class: "flex min-h-svh flex-col items-center justify-center gap-6 bg-background p-6 md:p-10" };
var _hoisted_2$1 = { class: "w-full max-w-sm" };
var _hoisted_3$1 = { class: "flex flex-col gap-8" };
var _hoisted_4$1 = { class: "flex flex-col items-center gap-4" };
var _hoisted_5$1 = { class: "mb-1 flex h-9 w-9 items-center justify-center rounded-md" };
var _hoisted_6 = { class: "sr-only" };
var _hoisted_7 = { class: "space-y-2 text-center" };
var _hoisted_8 = { class: "text-xl font-medium" };
var _hoisted_9 = { class: "text-center text-sm text-muted-foreground" };
//#endregion
//#region resources/js/layouts/auth/AuthSimpleLayout.vue
var AuthSimpleLayout_default = /* @__PURE__ */ defineComponent({
	__name: "AuthSimpleLayout",
	props: {
		title: {},
		description: {}
	},
	setup(__props) {
		return (_ctx, _cache) => {
			return openBlock(), createElementBlock("div", _hoisted_1$1, [createBaseVNode("div", _hoisted_2$1, [createBaseVNode("div", _hoisted_3$1, [createBaseVNode("div", _hoisted_4$1, [createVNode(unref(link_default), {
				href: "/",
				class: "flex flex-col items-center gap-2 font-medium"
			}, {
				default: withCtx(() => [createBaseVNode("div", _hoisted_5$1, [createVNode(AppLogoIcon_default, { class: "size-9 fill-current text-[var(--foreground)] dark:text-white" })]), createBaseVNode("span", _hoisted_6, toDisplayString(__props.title), 1)]),
				_: 1
			}), createBaseVNode("div", _hoisted_7, [createBaseVNode("h1", _hoisted_8, toDisplayString(__props.title), 1), createBaseVNode("p", _hoisted_9, toDisplayString(__props.description), 1)])]), renderSlot(_ctx.$slots, "default")])])]);
		};
	}
});
//#endregion
//#region resources/js/layouts/AuthLayout.vue
var AuthLayout_default = /* @__PURE__ */ defineComponent({
	__name: "AuthLayout",
	props: {
		title: {},
		description: {}
	},
	setup(__props) {
		return (_ctx, _cache) => {
			return openBlock(), createBlock(AuthSimpleLayout_default, {
				title: __props.title,
				description: __props.description
			}, {
				default: withCtx(() => [renderSlot(_ctx.$slots, "default")]),
				_: 3
			}, 8, ["title", "description"]);
		};
	}
});
//#endregion
//#region resources/js/pages/Auth/Login.vue?vue&type=script&setup=true&lang.ts
var _hoisted_1 = {
	key: 0,
	class: "mb-4 text-center text-sm font-medium text-green-600"
};
var _hoisted_2 = { class: "grid gap-6" };
var _hoisted_3 = { class: "grid gap-2" };
var _hoisted_4 = { class: "grid gap-2" };
var _hoisted_5 = { class: "flex items-center justify-between" };
//#endregion
//#region resources/js/pages/Auth/Login.vue
var Login_default = /* @__PURE__ */ defineComponent({
	__name: "Login",
	props: { status: {} },
	setup(__props) {
		return (_ctx, _cache) => {
			return openBlock(), createBlock(AuthLayout_default, {
				title: "Inertia.js Kitchen Sink",
				description: "Explore every Inertia feature in one demo app. Log in with the pre-filled credentials to get started."
			}, {
				default: withCtx(() => [
					createVNode(unref(head_default), { title: "Log in" }),
					__props.status ? (openBlock(), createElementBlock("div", _hoisted_1, toDisplayString(__props.status), 1)) : createCommentVNode("", true),
					createVNode(unref(form_default), {
						action: "/login",
						method: "post",
						"reset-on-success": ["password"],
						class: "flex flex-col gap-6"
					}, {
						default: withCtx(({ errors, processing }) => [createBaseVNode("div", _hoisted_2, [
							createBaseVNode("div", _hoisted_3, [
								createVNode(unref(Label_default), { for: "email" }, {
									default: withCtx(() => [..._cache[0] || (_cache[0] = [createTextVNode("Email address", -1)])]),
									_: 1
								}),
								createVNode(unref(Input_default), {
									id: "email",
									type: "email",
									name: "email",
									required: "",
									autofocus: "",
									tabindex: 1,
									autocomplete: "email",
									placeholder: "email@example.com",
									"default-value": "test@example.com"
								}),
								createVNode(InputError_default, { message: errors.email }, null, 8, ["message"])
							]),
							createBaseVNode("div", _hoisted_4, [
								createVNode(unref(Label_default), { for: "password" }, {
									default: withCtx(() => [..._cache[1] || (_cache[1] = [createTextVNode("Password", -1)])]),
									_: 1
								}),
								createVNode(unref(Input_default), {
									id: "password",
									type: "password",
									name: "password",
									required: "",
									tabindex: 2,
									autocomplete: "current-password",
									placeholder: "Password",
									"default-value": "password"
								}),
								createVNode(InputError_default, { message: errors.password }, null, 8, ["message"])
							]),
							createBaseVNode("div", _hoisted_5, [createVNode(unref(Label_default), {
								for: "remember",
								class: "flex items-center space-x-3"
							}, {
								default: withCtx(() => [createVNode(unref(Checkbox_default), {
									id: "remember",
									name: "remember",
									tabindex: 3
								}), _cache[2] || (_cache[2] = createBaseVNode("span", null, "Remember me", -1))]),
								_: 1
							})]),
							createVNode(unref(Button_default), {
								type: "submit",
								class: "mt-4 w-full",
								tabindex: 4,
								disabled: processing,
								"data-test": "login-button"
							}, {
								default: withCtx(() => [processing ? (openBlock(), createBlock(unref(Spinner_default), { key: 0 })) : createCommentVNode("", true), _cache[3] || (_cache[3] = createTextVNode(" Log in ", -1))]),
								_: 2
							}, 1032, ["disabled"])
						])]),
						_: 1
					})
				]),
				_: 1
			});
		};
	}
});
//#endregion
export { Login_default as default };

// Stub for SvelteKit's $app/state module
// In a non-SvelteKit environment, we provide a minimal implementation

export interface Page {
  url: URL;
  params: Record<string, string>;
  route: { id: string | null };
  status: number;
  error: Error | null;
  data: Record<string, unknown>;
  form: FormData | null;
}

let pageState: Page = {
  url: new URL('http://localhost'),
  params: {},
  route: { id: null },
  status: 200,
  error: null,
  data: {},
  form: null,
};

export const page = {
  get url() {
    return pageState.url;
  },
  get params() {
    return pageState.params;
  },
  get route() {
    return pageState.route;
  },
  get status() {
    return pageState.status;
  },
  get error() {
    return pageState.error;
  },
  get data() {
    return pageState.data;
  },
  get form() {
    return pageState.form;
  },
};

export function updatePage(newState: Partial<Page>) {
  pageState = { ...pageState, ...newState };
}
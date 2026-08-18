import type { Auth } from '@/types';

declare module '@inertiajs/core' {
    interface PageProps {
        auth?: Auth | null;
        name?: string;
        sidebarOpen?: boolean;
    }
}

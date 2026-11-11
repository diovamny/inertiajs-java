<script setup lang="ts">
import { Head, router } from '@inertiajs/vue3';
import FeatureCard from '@/components/FeatureCard.vue';
import FeatureHeader from '@/components/FeatureHeader.vue';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import AppLayout from '@/layouts/AppLayout.vue';
import { type BreadcrumbItem } from '@/types';

defineProps<{
    stockCount: number;
    channelName: string;
    channelType: string;
    throttleMs: number;
}>();

const breadcrumbs: BreadcrumbItem[] = [
    { title: 'Data Loading' },
    { title: 'Live Props' },
];

function simulateStockUpdate(): void {
    router.post('/api/simulate-stock-update', {}, { preserveScroll: true });
}
</script>

<template>
    <Head title="Live Props" />

    <AppLayout :breadcrumbs="breadcrumbs">
        <div class="flex h-full flex-1 flex-col gap-6 p-4">
            <FeatureHeader
                title="Live Props"
                docs="data-props/live-props"
                controller="controller/feature/LivePropsController"
            >
                <code>Inertia::live()</code> props carry broadcast metadata so
                the official client can refresh them when an event fires. The
                resolver runs on the initial visit and on partial reloads that
                select the prop — never for foreign keys.
            </FeatureHeader>

            <div class="grid gap-6 lg:grid-cols-2">
                <FeatureCard
                    title="Inventory stock"
                    description="Live prop stockCount, throttled to 2000 ms."
                >
                    <div class="flex flex-wrap items-center gap-3">
                        <Badge
                            data-testid="live-stock-count"
                            variant="secondary"
                        >
                            {{ stockCount }}
                        </Badge>
                        <Button
                            data-testid="simulate-stock-update"
                            type="button"
                            @click="simulateStockUpdate"
                        >
                            Simulate stock update
                        </Button>
                    </div>
                    <p class="mt-4 text-sm text-muted-foreground">
                        In production the Inertia client subscribes to the
                        <code>{{ channelName }}</code> channel and
                        <code>StockUpdated</code> broadcast. This demo exposes
                        the simulate endpoint to mutate the in-memory value; the
                        redirect re-renders the page with the fresh resolver
                        output.
                    </p>
                </FeatureCard>

                <FeatureCard
                    title="Wire metadata"
                    description="What the server emits in page.liveProps.stockCount."
                >
                    <dl class="grid gap-2 text-sm">
                        <div class="flex justify-between gap-4">
                            <dt class="text-muted-foreground">channel</dt>
                            <dd data-testid="live-channel-name">
                                {{ channelName }}
                            </dd>
                        </div>
                        <div class="flex justify-between gap-4">
                            <dt class="text-muted-foreground">type</dt>
                            <dd data-testid="live-channel-type">
                                {{ channelType }}
                            </dd>
                        </div>
                        <div class="flex justify-between gap-4">
                            <dt class="text-muted-foreground">event</dt>
                            <dd>StockUpdated</dd>
                        </div>
                        <div class="flex justify-between gap-4">
                            <dt class="text-muted-foreground">throttle</dt>
                            <dd data-testid="live-throttle">
                                {{ throttleMs }} ms
                            </dd>
                        </div>
                    </dl>
                </FeatureCard>
            </div>
        </div>
    </AppLayout>
</template>

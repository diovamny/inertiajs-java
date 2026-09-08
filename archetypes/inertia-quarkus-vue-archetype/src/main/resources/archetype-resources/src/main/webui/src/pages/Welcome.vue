<script setup lang="ts">
defineProps<{ appName: string; framework: string; frameworkVersion: string; inertiaUrl: string }>()
</script>
<template>
  <main class="welcome">
    <img class="logo" src="/brand/inertia.svg" alt="Inertia.js" />
    <h1>I <span role="img" aria-label="love">♥</span> Inertia.js</h1>
    <p>{{ appName }} powered by {{ framework }} {{ frameworkVersion }}</p>
    <p><a :href="inertiaUrl" target="_blank" rel="noreferrer">Visit Inertia.js</a></p>
    <div class="powered">
      <img src="/brand/quarkus.svg" alt="Quarkus" />
      <span>Powered by Quarkus</span>
    </div>
    <div class="powered">
      <img src="/brand/vue.svg" alt="Vue.js" />
      <span>Built with Vue.js</span>
    </div>
    <div class="powered">
      <img src="/brand/java.svg" alt="Java" />
      <span>Running on Java</span>
    </div>
  </main>
</template>

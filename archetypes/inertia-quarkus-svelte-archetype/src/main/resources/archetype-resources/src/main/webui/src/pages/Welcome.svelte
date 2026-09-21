<script lang="ts">
  let {
    appName,
    framework,
    frameworkVersion,
    inertiaUrl,
  }: {
    appName: string
    framework: string
    frameworkVersion: string
    inertiaUrl: string
  } = $props()
</script>

<main class="welcome">
  <img class="logo" src="/brand/inertia.svg" alt="Inertia.js" />
  <h1>I <span role="img" aria-label="love">&#10084;</span> Inertia.js</h1>
  <p>{appName} powered by {framework} {frameworkVersion}</p>
  <p><a href={inertiaUrl} target="_blank" rel="noreferrer">Visit Inertia.js</a></p>
  <div class="powered">
    <img src="/brand/quarkus.svg" alt="Quarkus" />
    <span>Powered by Quarkus</span>
  </div>
  <div class="powered">
    <img src="/brand/svelte.svg" alt="Svelte" />
    <span>Built with Svelte</span>
  </div>
  <div class="powered">
    <img src="/brand/java.svg" alt="Java" />
    <span>Running on Java</span>
  </div>
</main>

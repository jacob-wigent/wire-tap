/** @type {import("@sveltejs/vite-plugin-svelte").SvelteConfig} */

const config = {
  compilerOptions: {
    runes: true // Forces runes mode on ALL files, including dependencies
  }
};

export default config;

<script lang="ts">
  import Tile from './lib/Tile.svelte';
</script>

<section>
  <Tile></Tile>
</section>

<style>
  section {
    width: 100vw;
    height: 100vh;
    background-image: 
      linear-gradient(to right, #000000 1px, transparent 1px),
      linear-gradient(to bottom, #000000 1px, transparent 1px);
    background-size: 128px 128px;
  }
</style>
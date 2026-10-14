<script lang="ts">
    import type { Snippet } from 'svelte';

    interface Props {
        gridTileWidth?: number;
        gridTileHeight?: number;
        children?: Snippet;
    }

    let { gridTileWidth = 1, gridTileHeight = 1, children }: Props = $props();

    const GRID_SIZE = 128;

    const clamp = (value: number, min: number, max: number) => Math.min(Math.max(value, min), max);

    let staticGridCoordX = $state(0);
    let staticGridCoordY = $state(0);
    let dragPixelCoordX = $state(0);
    let dragPixelCoordY = $state(0);
    let isDragging = $state(false);
	
	// Rendered tile coordinates
    let pixelCoordX = $derived(isDragging ? dragPixelCoordX : staticGridCoordX * GRID_SIZE);
    let pixelCoordY = $derived(isDragging ? dragPixelCoordY : staticGridCoordY * GRID_SIZE);
	
	// Rendered preview tile coordinates
	let snappedPixelCoordX = $derived(Math.round(dragPixelCoordX / GRID_SIZE) * GRID_SIZE);
    let snappedPixelCoordY = $derived(Math.round(dragPixelCoordY / GRID_SIZE) * GRID_SIZE);

    function handleGrab() {
		// Initialize dragging coordinates to current pixel coordinates
        dragPixelCoordX = pixelCoordX;
        dragPixelCoordY = pixelCoordY;
        isDragging = true;
    }

    function handleDrag(e: MouseEvent) {
        if (!isDragging) return;

        const maxX = window.innerWidth - gridTileWidth * GRID_SIZE;
        const maxY = window.innerHeight - gridTileHeight * GRID_SIZE;

        dragPixelCoordX = clamp(dragPixelCoordX + e.movementX, 0, maxX);
        dragPixelCoordY = clamp(dragPixelCoordY + e.movementY, 0, maxY);
    }

    function handleDrop() {
        if (!isDragging) return;

        staticGridCoordX = Math.round(dragPixelCoordX / GRID_SIZE);
        staticGridCoordY = Math.round(dragPixelCoordY / GRID_SIZE);
		
        isDragging = false;
    }
</script>

<div
    class="tile"
    onmousedown={handleGrab}
    style:left={`${pixelCoordX}px`}
    style:top={`${pixelCoordY}px`}
	style:width={`${gridTileWidth * GRID_SIZE}px`}
	style:height={`${gridTileHeight * GRID_SIZE}px`}
>
    {@render children?.()}
</div>

{#if isDragging}
    <div
        class="tile preview"
        style:left={`${snappedPixelCoordX}px`}
        style:top={`${snappedPixelCoordY}px`}
		style:width={`${gridTileWidth * GRID_SIZE}px`}
		style:height={`${gridTileHeight * GRID_SIZE}px`}
    ></div>
{/if}

<svelte:window onmouseup={handleDrop} onmousemove={handleDrag} />

<style>
    .tile {
        position: absolute;
        background-color: lightblue;
        corner-shape: bevel;
        border-radius: 8px;
        padding: 8px;
    }

    .preview {
        opacity: 0.35;
        pointer-events: none;
    }

	.tile:active {
    	cursor: grabbing;
    	transition: none; /* Disable smoothing while dragging */
		z-index: 1;
 	}
</style>
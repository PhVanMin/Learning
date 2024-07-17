#include "chunk.h"
#include "memory.h"
#include <stdio.h>

void initChunk(Chunk *chunk) {
  printf("Initializing chunk...\n");
  chunk->count = 0;
  chunk->capacity = 0;
  chunk->code = NULL;
}

void writeChunk(Chunk *chunk, uint8_t byte) {
  printf("Writing chunk...\n");
  if (chunk->capacity == chunk->count) {
    int oldCapacity = chunk->capacity;
    chunk->capacity = GROW_CAP(oldCapacity);
    chunk->code =
        GROW_ARRAY(uint8_t, chunk->code, oldCapacity, chunk->capacity);
  }

  chunk->code[chunk->count] = byte;
  chunk->count++;
}

void freeChunk(Chunk *chunk) {
  printf("Freeing chunk...\n");
  FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
  initChunk(chunk);
}

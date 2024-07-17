#ifndef mpp_memory_h
#define mpp_memory_h

#include "common.h"

#define GROW_CAP(capacity) ((capacity) < 8 ? 8 : (capacity * 2))
#define GROW_ARRAY(type, array, oldCount, newCount) \
  (type*)reallocate(array, sizeof(type) * (oldCount), sizeof(type) * (newCount))
#define FREE_ARRAY(type, array, capacity) reallocate(array, sizeof(type) * (capacity), 0)

void* reallocate(void* array, size_t oldSize, size_t newSize);

#endif

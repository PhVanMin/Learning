#ifndef MYALGO_H
#define MYALGO_H

#include "utils.h"

typedef struct {
    char* key;
    int value;
} hash_item;

typedef struct {
    hash_item** array;
    int length;
    int size;
} hash_table;

void countingSort(int* arr, int n);
void quickSort(int* arr, int left, int right);
void mergeSort(int* arr, int left, int right);
void radixSort(int* arr, int n);
void heapSort(int* arr, int n);

void initHashTable(hash_table* table);
void ht_free(hash_table *ht);
void ht_delete(hash_table* table, char* key);
void ht_add(hash_table* table, char* key, int value);
hash_item* ht_getItem(hash_table* table, char* key);

#endif

#include "myAlgo.h"
#include <math.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#define HASH_VALUE 163
#define SCALE_UP 2
#define SCALE_DOWN 0.5
#define INI_TABLE_SIZE 50
#define MIN_LOAD_PER 0.1
#define MAX_LOAD_PER 0.7

static int hash(char *key, int size) {
    int len = strlen(key);
    long sum = 0;
    for (int i = 0; i < len; i++) {
        sum += (long)pow(HASH_VALUE, len - i) * key[i];
        sum = sum % size;
    }

    return (int)sum;
}

static int ht_getItemIndex(hash_item **arr, char *key, int size) {
    int index = hash(key, size);

    for (int i = index; i < size; i++) {
        if (arr[i] == NULL)
            continue;
        if (strcmp(arr[i]->key, key) == 0) {
            return i;
        }
    }

    for (int i = 0; i < index; i++) {
        if (arr[i] == NULL)
            continue;
        if (strcmp(arr[i]->key, key) == 0) {
            return i;
        }
    }
    return -1;
}

hash_item *ht_getItem(hash_table *table, char *key) {
    int index = ht_getItemIndex(table->array, key, table->size);
    if (index != -1) {
        return table->array[index];
    }
    return NULL;
}

void ht_free(hash_table *ht) {
    int count = 0;
    for (int i = 0; i < ht->size; i++) {
        if (ht->array[i] != NULL) {
            count++;
            free(ht->array[i]->key);
            free(ht->array[i]);
        }
    }

    printf("Freed %d items!\n", count);
    free(ht->array);
    free(ht);
}

static int ht_getAvailableIndex(hash_item **arr, hash_item *item, int size) {
    int index = hash(item->key, size);

    for (int i = index; i < size; i++) {
        if (arr[i] == NULL) {
            return i;
        }
    }

    for (int i = 0; i < index; i++) {
        if (arr[i] == NULL) {
            return i;
        }
    }

    return -1;
}

static void ht_resizeArray(hash_table *table, float multiplier) {
    int old_size = table->size;
    table->size = (int)old_size * multiplier;
    hash_item **new_array = calloc((size_t)table->size, sizeof(hash_item *));
    for (int i = 0; i < old_size; i++) {
        if (table->array[i] != NULL) {
            int index = ht_getAvailableIndex(new_array, table->array[i], table->size);
            new_array[index] = table->array[i];
        }
    }

    free(table->array);
    table->array = new_array;
}

void ht_add(hash_table *table, char *key, int value) {
    table->length++;

    float load = table->length * 1.0 / table->size;
    if (load >= MAX_LOAD_PER) {
        printf("Downsize to %d\n", (int)(table->size * SCALE_UP));
        ht_resizeArray(table, SCALE_UP);
    }

    hash_item *item = malloc(sizeof(hash_item));
    item->key = key;
    item->value = value;

    int index = ht_getAvailableIndex(table->array, item, table->size);
    table->array[index] = item;
}

void ht_delete(hash_table *table, char *key) {
    int i = ht_getItemIndex(table->array, key, table->size);
    if (i != -1) {
        free(table->array[i]->key);
        free(table->array[i]);
        table->array[i] = NULL;
        table->length--;

        float load = table->length * 1.0 / table->size;
        if ((load <= MIN_LOAD_PER) &&
                (table->size * SCALE_DOWN >= INI_TABLE_SIZE)) {
            printf("Downsize to %d\n", (int)(table->size * SCALE_DOWN));
            ht_resizeArray(table, SCALE_DOWN);
        }
    }
}

void initHashTable(hash_table *table) {
    table->length = 0;
    table->size = INI_TABLE_SIZE;
    table->array = calloc((size_t)table->size, sizeof(hash_item *));
}

void radixSort(int *arr, int n) {
    // int max = 0;

    // for (int i = 0; i < n; i++) {
    //     if (max < arr[i]) max = arr[i];
    // }

    printf("radix sort");
}

int partition(int *arr, int left, int right) {
    int pivot = arr[right];
    int low = left;
    int high = right;

    while (low < high) {
        while (arr[low] < pivot && low < right)
            low++;
        while (arr[high] >= pivot && high > left)
            high--;
        if (low < high)
            swap(&arr[low], &arr[high]);
    }

    swap(&arr[right], &arr[low]);
    return low;
}

void quickSort(int *arr, int left, int right) {
    if (left < right) {
        int pivot = partition(arr, left, right);
        quickSort(arr, left, pivot - 1);
        quickSort(arr, pivot + 1, right);
    }
}

void merge(int *arr, int left, int middle, int right) {
    int left_n = middle - left + 1;
    int right_n = right - middle;
    int lefta[left_n], righta[right_n];

    for (int i = 0; i < left_n; i++) {
        lefta[i] = arr[left + i];
    }

    for (int j = 0; j < right_n; j++) {
        righta[j] = arr[middle + 1 + j];
    }

    int i = 0, j = 0;
    while (i < left_n && j < right_n) {
        if (lefta[i] < righta[j]) {
            arr[left + i + j] = lefta[i];
            i++;
        } else {
            arr[left + i + j] = righta[j];
            j++;
        }
    }

    while (i < left_n) {
        arr[left + i + j] = lefta[i];
        i++;
    }
    while (j < right_n) {
        arr[left + i + j] = righta[j];
        j++;
    }
}

void mergeSort(int *arr, int left, int right) {
    if (left < right) {
        int middle = (left + right) / 2;
        mergeSort(arr, left, middle);
        mergeSort(arr, middle + 1, right);
        merge(arr, left, middle, right);
    }
}

void heapify(int *arr, int i, int n) {
    int largest = i;
    int left = i * 2 + 1;
    int right = i * 2 + 2;

    if (left < n && arr[largest] < arr[left])
        largest = left;
    if (right < n && arr[largest] < arr[right])
        largest = right;

    if (largest != i) {
        swap(&arr[i], &arr[largest]);
        heapify(arr, largest, n);
    }
}

void heapSort(int *arr, int n) {
    for (int i = n / 2 - 1; i >= 0; i--) {
        heapify(arr, i, n);
    }

    for (int i = n - 1; i > 0; i--) {
        swap(&arr[i], &arr[0]);
        heapify(arr, 0, i);
    }
}

void countingSort(int *arr, int n) {
    int max = 0;
    for (int i = 0; i < n; i++) {
        if (max < arr[i])
            max = arr[i];
    }

    int carr[max + 1];
    for (int i = 0; i < max + 1; i++) {
        carr[i] = 0;
    }

    for (int i = 0; i < n; i++) {
        carr[arr[i]]++;
    }

    int i = 0, j = 0;
    while (i < n) {
        while (carr[j] == 0)
            j++;
        arr[i] = j;
        carr[j]--;
        i++;
    }
}

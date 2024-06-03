#include "myAlgo.h"
#include <stdio.h>
#include <time.h>
//  #include "utils.h"

int main() {
    // int n = 10;
    // int arr[n];
    // generateArr(arr, n);

    // printArr(arr, n);
    // quickSort(arr, 0, n - 1);
    // printArr(arr, n);

    hash_table *ht = malloc((size_t)sizeof(hash_table));
    initHashTable(ht);

    for (int i = 65; i < 110; i++) {
        char *p = malloc((size_t)sizeof(char) * 2);
        p[0] = i;
        p[1] = '\0';
        ht_add(ht, p, i);
    }

    char* key = "1";
    hash_item* item = ht_getItem(ht, key);
    if (item != NULL) {
        printf("Item found (%s: %d)\n", item->key, item->value);
    } else {
        printf("Not found\n");
    }


    int size = ht->size;
    for (int i = 0; i < size; i++) {
        if (ht->array[i] != NULL) {
            ht_delete(ht, ht->array[i]->key);
            if (ht->size != size) {
                size = ht->size;
                i = -1;
            }
        }
    }
    
    ht_free(ht);
}

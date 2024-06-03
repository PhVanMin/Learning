#include "utils.h"

void swap(int* a, int* b) {
    int c = *a;
    *a = *b;
    *b = c;
}

void printArr(int *arr, int n) {
    for (int i = 0; i < n; i++) {
        printf("%d ", arr[i]);
    }

    printf("\n");
}

void generateArr(int* arr, int n) { 
    srand(time(0));

    for (int i = 0; i < n; i++) {
        arr[i] = rand() % (100);
    }
}

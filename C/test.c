#include <stdio.h>
int main() {
  int a = 1;
  { int a = a + 1; printf("%d", a); }
}

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

char *copyString(char s[]) {
  char *s2;
  s2 = (char *)malloc(20);

  strcpy(s2, s);
  return (char *)s2;
}

int main() {
  char **c = malloc(10 * (size_t)sizeof(char *));
  for (int i = 65; i < 75; i++) {
    char *str = malloc(2);
    str[0] = i;
    str[1] = '\0';
    c[i - 65] = copyString(str);
    printf("%d %s\n", i - 65, c[i - 65]);
  }

  for (int i = 0; i < 10; i++) {
    printf("%d %s\n", i, c[i]);
  }

  free(c);
}

#include "lsh.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

static int lsh_cd(char **args) {
  if (args[1] == NULL) {
    fprintf(stderr, "lsh: expected argument to \"cd\"\n");
  } else {
    if (chdir(args[1]) != 0) {
      perror("lsh");
    }
  }
  printf("Changing to %s\n", args[1]);
  return 1;
}

static int lsh_exit(char **args) { return 0; }

static char *builtin_str[] = {"cd", "exit"};

static int (*builtin_func[])(char **) = {&lsh_cd, &lsh_exit};

static int lsh_num_builtins() { return sizeof(builtin_str) / sizeof(char *); }

static char *lsh_read_line() {
  char *line = NULL;
  size_t bufsize = 0;

  if (getline(&line, &bufsize, stdin) == -1) {
    if (feof(stdin)) {
      exit(EXIT_SUCCESS);
    } else {
      perror("Readline");
      exit(EXIT_FAILURE);
    }
  }

  return line;
}

static char **lsh_split_line(char *line) {
  char **tokens = malloc(sizeof(char *) * LSH_TOK_BUFSIZE);
  if (tokens == NULL) {
    fprintf(stderr, "lsh: allocation error\n");
    exit(EXIT_FAILURE);
  }

  int pos = 0;
  char *token = strtok(line, LSH_TOKEN_DELIM);
  while (token != NULL) {
    tokens[pos] = token;
    pos++;
    token = strtok(NULL, LSH_TOKEN_DELIM);
  }
  tokens[pos] = NULL;
  return tokens;
}

static int lsh_launch(char **args) {
  pid_t pid, wpid;
  int status;

  pid = fork();
  if (pid == 0) {
    // this is child process
    if (execvp(args[0], args) == -1) {
      perror("lsh");
    }
    exit(EXIT_FAILURE);
  } else if (pid < 0) {
    // error forking
    perror("lsh");
  } else {
    // parent process
    do {
      // wait for child to finish
      wpid = waitpid(pid, &status, WUNTRACED);
    } while (!WIFEXITED(status) && !WIFSIGNALED(status));
  }

  return 1;
}

static int lsh_execute(char **args) {
  if (args[0] == NULL) {
    return 1;
  }

  for (int i = 0; i < lsh_num_builtins(); i++) {
    if (strcmp(args[0], builtin_str[i]) == 0) {
      return (*builtin_func[i])(args);
    }
  }

  return lsh_launch(args);
}

void lsh_loop() {
  char *line;
  char **args;
  int status;

  do {
    printf("> ");
    line = lsh_read_line();
    args = lsh_split_line(line);
    status = lsh_execute(args);

    free(line);
    free(args);
  } while (status);
}

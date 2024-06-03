CC=gcc
CFLAGS=-lm -I.
OBJ = main.o lsh.o
EXE = program
DEPS = lsh.h

$(EXE): $(OBJ)
	$(CC) -o $@ $^ $(CFLAGS)
	./$(EXE)

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $<

.PHONY: clean
clean:
	rm $(OBJ) $(EXE)

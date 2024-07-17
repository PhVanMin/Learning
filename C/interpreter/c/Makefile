CC=gcc
CFLAGS=-lm -I.
OBJ = main.o memory.o chunk.o
EXE = program
DEPS = memory.h chunk.h common.h 

$(EXE): $(OBJ)
	$(CC) -o $@ $^ $(CFLAGS)
	./$(EXE)

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $<

.PHONY: clean
clean:
	rm $(OBJ) $(EXE)

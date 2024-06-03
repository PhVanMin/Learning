CC=gcc
CFLAGS=-lm -I.
OBJ = main.o myAlgo.o utils.o
EXE = program
DEPS = myAlgo.h utils.h 

$(EXE): $(OBJ)
	$(CC) -o $@ $^ $(CFLAGS)
	./$(EXE)

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $<

.PHONY: clean
clean:
	rm $(OBJ) $(EXE)

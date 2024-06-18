package main

import (
	"errors"
	"fmt"
	"sync"
	"time"
)

type rect struct {
	w, h float64
}

type Shape interface {
	area() (float64, error)
	perimeter() float64
}

func (r rect) area() (float64, error) {
	if r.w <= 0 || r.h <= 0 {
		return -1, errors.New("Errors: w or h < 1")
	}
	return r.w * r.h, nil
}

func (r rect) perimeter() float64 {
	return (r.w + r.h) * 2
}

func getFunc() func() int {
	var count int = 0
	return func() int {
		count++
		return count
	}
}

func hello(name string, wg *sync.WaitGroup) {
	for i := 0; i < 5; i++ {
        time.Sleep(200 * time.Millisecond)
		fmt.Println("Hello", name)
	}
	wg.Done()
}

func main() {
	var wg sync.WaitGroup
	wg.Add(2)

	go hello("Minh", &wg)
	go hello("Linh", &wg)

	wg.Wait()
	/* var shape Shape = rect{
		w: 1, h: 2,
	}

	res, err := shape.area()
	if err != nil {
		fmt.Println(err.Error())
	} else {
		fmt.Println(res)
	}
	*/
}

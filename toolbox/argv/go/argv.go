package main

import (
	"fmt"
	"os"
	"strconv"
)

func main() {
	if len(os.Args) == 0 {
		fmt.Println("Empty argv (not even program name)")
		return
	}

	prgName, posArgs := os.Args[0], os.Args[1:]
	argOrArgs := "args"
	if len(posArgs) == 1 {
		argOrArgs = "arg"
	}
	fmt.Println(len(posArgs), argOrArgs, "for ‘"+prgName+"’")

	for i, arg := range posArgs {
		fmt.Println(strconv.Itoa(i+1) + ": ‘" + arg + "’")
	}
}

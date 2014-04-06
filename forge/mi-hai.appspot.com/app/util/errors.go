package util

import (
	"appengine"
	"net/http"
)

// http://blog.golang.org/error-handling-and-go

type AppError struct {
	Error   error
	Message string
	Code    int
}

// Allocate and return a new AppError for the common case where the Message
// is taken from the error and the code is 500.
func NewAppError(err error) *AppError {
	return &AppError{err, "Error: " + err.Error(), 500}
}

type AppHandler func(http.ResponseWriter, *http.Request) *AppError

func (fn AppHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	defer func() {
		if x := recover(); x != nil {
			c := appengine.NewContext(r)
			c.Errorf("Panic: %v", x)
			http.Error(w, "Internal Server Error", 500)
		}
	}()
	if e := fn(w, r); e != nil {
		c := appengine.NewContext(r)
		c.Errorf("%v", e.Error)
		http.Error(w, e.Message, e.Code)
	}
}

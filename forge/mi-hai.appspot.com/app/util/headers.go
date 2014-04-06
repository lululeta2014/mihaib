package util

import (
	"net/http"
)

func SetContentType(w http.ResponseWriter, contentType string) {
	w.Header().Set("Content-Type", contentType)
}

func SetPlainTextContentType(w http.ResponseWriter) {
	SetContentType(w, "text/plain; charset=utf-8")
}

func SetHtmlContentType(w http.ResponseWriter) {
	SetContentType(w, "text/html; charset=utf-8")
}

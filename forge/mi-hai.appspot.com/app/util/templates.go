package util

import (
	"go/build"
	"html/template"
)

var (
	// Find our templates, no matter if we're run by appengine,
	// from a unit test, or imported by some other project.
	basePath = GetSubdirPath("util")

	HtmlStartFile = basePath + "/templates/html-start.html"
	HtmlStartTmpl = template.Must(template.ParseFiles(HtmlStartFile))

	HtmlEndFile = basePath + "/templates/html-end.html"
	HtmlEndTmpl = template.Must(template.ParseFiles(HtmlEndFile))
)

// Data-type for executing the HtmlStart{Tmpl|File} template.
type HtmlStartT struct {
	Title string
	Css   []string
}

// Find the path to a Go subdirectory of this app.
//
// Inspired by:
// https://code.google.com/p/go/source/browse?repo=talks#hg%2Fpresent
// Can't use conditional compilation (i.e. +build !appengine) because the
// unit tests also run in appengine context (goapp test …) so doing a try
// and fallback.
func GetSubdirPath(subdir string) string {
	pkg := "github.com/MihaiB/mihaib/forge/mi-hai.appspot.com/app/" + subdir
	p, err := build.Default.Import(pkg, "", build.FindOnly)
	if err != nil {
		return "./" + subdir
	}
	return p.Dir
}

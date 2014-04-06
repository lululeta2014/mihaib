package root

import (
	"bytes"
	"github.com/MihaiB/mihaib/forge/mi-hai.appspot.com/app/util"
	"html/template"
	"net/http"
)

var (
	baseDir   = util.GetSubdirPath("root")
	rootTempl = getRootTempl()
)

func getRootTempl() *template.Template {
	tmpl := template.Must(template.ParseFiles(
		baseDir + "/templates/root.html"))
	template.Must(tmpl.ParseFiles(util.HtmlStartFile))
	template.Must(tmpl.ParseFiles(util.HtmlEndFile))
	return tmpl
}

func init() {
	http.Handle("/", util.AppHandler(handler))
}

type projectT struct {
	Target, Title, Description string
}

type rootDataT struct {
	HtmlStart *util.HtmlStartT
	Projects  []projectT
}

func handler(w http.ResponseWriter, r *http.Request) *util.AppError {
	// Write to a buffer first so if an error occurs we only send the
	// error message to the client and can still set a (500) http code.
	buf := &bytes.Buffer{}

	htmlStart := &util.HtmlStartT{
		Title: "Mihai's simple test app",
		Css:   []string{"root/style.css"},
	}
	projects := []projectT{
		projectT{
			"hsl-ticket-price/",
			"HSL.fi season ticket, price calculator",
			`Price calculator for HSL.fi season tickets.
	See how the daily or monthly price changes when you get a season ticket
	for a longer period.`,
		},
	}

	rootData := &rootDataT{
		HtmlStart: htmlStart,
		Projects:  projects,
	}
	err := rootTempl.Execute(buf, rootData)
	if err != nil {
		return &util.AppError{err, "Error rendering project list", 500}
	}

	util.SetHtmlContentType(w)
	_, err = buf.WriteTo(w)
	if err != nil {
		// we might have sent part of the data already,
		// in which case we can't change the error code.
		return &util.AppError{err, "Error writing response", 500}
	}
	return nil
}

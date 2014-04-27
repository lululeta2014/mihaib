package headers

import (
	"appengine"
	"appengine/user"
	"bytes"
	"github.com/MihaiB/mihaib/forge/mi-hai.appspot.com/app/util"
	"html/template"
	"net/http"
	"strings"
)

var (
	baseDir   = util.GetSubdirPath("headers")
	mainTempl = getMainTempl()
)

func getMainTempl() *template.Template {
	tmpl := template.Must(
		template.ParseFiles(baseDir + "/templates/main.html"))
	template.Must(tmpl.ParseFiles(util.HtmlStartFile, util.HtmlEndFile))
	return tmpl
}

func init() {
	http.Handle("/headers/", util.AppHandler(handler))
}

type appengineT struct {
	AppID                  string
	Datacenter             string
	DefaultVersionHostname string
	InstanceID             string
	IsDevAppServer         bool
	ModuleHostname         string
	ModuleName             string
	RequestID              string
	ServerSoftware         string
	ServiceAccount         string
	VersionID              string
}

func newAppengineT(r *http.Request) (*appengineT, error) {
	c := appengine.NewContext(r)
	moduleHostname, err := appengine.ModuleHostname(c, "", "", "")
	if err != nil {
		return nil, err
	}
	serviceAccount, err := appengine.ServiceAccount(c)
	if err != nil {
		return nil, err
	}
	serviceAccount = strings.Replace(serviceAccount, "@", " ", -1)

	return &appengineT{
		AppID:                  appengine.AppID(c),
		Datacenter:             appengine.Datacenter(),
		DefaultVersionHostname: appengine.DefaultVersionHostname(c),
		InstanceID:             appengine.InstanceID(),
		IsDevAppServer:         appengine.IsDevAppServer(),
		ModuleHostname:         moduleHostname,
		ModuleName:             appengine.ModuleName(c),
		RequestID:              appengine.RequestID(c),
		ServerSoftware:         appengine.ServerSoftware(),
		ServiceAccount:         serviceAccount,
		VersionID:              appengine.VersionID(c),
	}, nil
}

type userT struct {
	User                *user.User
	LoginURL, LogoutURL string
}

func newUserT(r *http.Request) (*userT, error) {
	c := appengine.NewContext(r)
	loginUrl, err := user.LoginURL(c, r.URL.String())
	if err != nil {
		return nil, err
	}
	logoutUrl, err := user.LogoutURL(c, r.URL.String())
	if err != nil {
		return nil, err
	}

	return &userT{
		User:      user.Current(c),
		LoginURL:  loginUrl,
		LogoutURL: logoutUrl,
	}, nil
}

type mainDataT struct {
	HtmlStart *util.HtmlStartT
	Request   *http.Request
	Appengine *appengineT
	User      *userT
}

func handler(w http.ResponseWriter, r *http.Request) *util.AppError {
	htmlStart := &util.HtmlStartT{
		Title: "Headers",
		Css:   []string{"style.css"},
	}
	appEngData, err := newAppengineT(r)
	if err != nil {
		return &util.AppError{err, "Error getting AppEngine data", 500}
	}
	userData, err := newUserT(r)
	if err != nil {
		return &util.AppError{err,
			"Error getting Login/Logout URLs", 500}
	}

	mainData := &mainDataT{
		HtmlStart: htmlStart,
		Request:   r,
		Appengine: appEngData,
		User:      userData,
	}

	err = r.ParseForm()
	if err != nil {
		return &util.AppError{err, "Error in request.ParseForm()", 500}
	}

	buf := &bytes.Buffer{}
	err = mainTempl.Execute(buf, mainData)
	if err != nil {
		return &util.AppError{err, "Error rendering headers page", 500}
	}

	util.SetHtmlContentType(w)
	if _, err = buf.WriteTo(w); err != nil {
		return &util.AppError{err, "Error writing response", 500}
	}

	return nil
}

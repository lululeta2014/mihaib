package main

import (
	"errors"
	"net/http"
)

type tableDeleteRowH struct {
	tInf *tableInfo
}

func (h *tableDeleteRowH) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	var err error
	defer func() {
		if err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
		}
	}()

	err = r.ParseForm()
	if err != nil {
		return
	}
	idVal := r.FormValue("dbRowId")
	if idVal == "" {
		err = errors.New("No dbRowId specified")
		return
	}

	conn, err := getDb(getUser(r))
	if err != nil {
		return
	}
	defer func() {
		// this runs before the defer() above; DON'T mask an error.
		err2 := conn.Close()
		if err == nil {
			err = err2
		}
	}()

	// The user is logged in and can only affect his own database,
	// but let's still prevent SQL Injection ('?' in the SQL string).
	stmt, err := conn.Prepare("DELETE FROM " + h.tInf.table + " WHERE " +
		h.tInf.idCol + " == ?;")
	if err != nil {
		return
	}
	defer func() {
		err2 := stmt.Finalize()
		if err == nil {
			err = err2
		}
	}()

	err = stmt.Exec(idVal)
	if err != nil {
		return
	}
	for stmt.Next() {
	}
}

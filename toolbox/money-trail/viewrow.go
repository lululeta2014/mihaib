package main

import (
	"errors"
	"github.com/MihaiB/mihaib/toolbox/money-trail/sqlite"
	"log"
	"net/http"
)

type tableViewRowH struct {
	tInf *tableInfo
}

func (t *tableInfo) getRowForView(conn *sqlite.Conn, idVal interface{}) (
	row []interface{}, err error) {
	sql := t.getViewRowSql(t.table + "." + t.idCol + " = ?")
	sql += ";"

	stmt, err := conn.Prepare(sql)
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

	found := false
	warned := false
	for stmt.Next() {
		if !found {
			row, err = t.scan(stmt, false)
			if err != nil {
				return
			}
			found = true
		} else {
			if !warned {
				log.Println("Multiple rows with same ID")
				warned = true
			}
		}
	}

	if !found {
		err = errors.New("Not found")
		return
	}

	return
}

func (h *tableViewRowH) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	var err error
	var tData map[string]interface{}
	defer func() {
		if err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
		} else {
			err = templ.ExecuteTemplate(w, "viewrow", tData)
			if err != nil {
				log.Println("Error executing template:", err)
			}
		}
	}()

	err = r.ParseForm()
	if err != nil {
		return
	}
	idVal, err := h.tInf.idColType.parseString(r.FormValue("dbRowId"))
	if err != nil {
		return
	}

	conn, err := getDb(getUser(r))
	if err != nil {
		return
	}
	defer func() {
		err2 := conn.Close()
		if err == nil {
			err = err2
		}
	}()

	row, err := h.tInf.getRowForView(conn, idVal)
	if err != nil {
		return
	}

	tData = map[string]interface{}{
		"row":             row,
		"userCols":        h.tInf.userCols,
		"groupDigitsFunc": groupDigitsFunc,
		"cssRowClassFunc": h.tInf.cssRowClassFunc,
		"groupBy":         h.tInf.groupBy,
	}
}

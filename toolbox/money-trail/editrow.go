package main

import (
	"errors"
	"github.com/MihaiB/mihaib/toolbox/money-trail/sqlite"
	"log"
	"net/http"
)

type tableEditRowH struct {
	tInf *tableInfo
}

func (t *tableInfo) getRowForEdit(conn *sqlite.Conn, idVal interface{}) (
	row []interface{}, err error) {
	sql := "SELECT\n"
	for _, uc := range t.userCols {
		isDate := uc.finalTypeForEditRow() == dbDate
		sql += "IFNULL("

		if isDate {
			sql += "DATE("
		}
		if uc.Sel == nil || !uc.NoEdit {
			sql += "(" + uc.what + ")"
		} else {
			// we actually need these (SELECT ...) parantheses
			sql += "(SELECT " + uc.Sel.valCol +
				" FROM " + uc.Sel.table +
				" WHERE " + uc.Sel.keyCol + " == " +
				t.table + "." + uc.what + ")"
		}
		if isDate {
			sql += ")"
		}

		sql += ", " + uc.finalTypeForEditRow().sqlZeroVal() + ")"
		sql += " AS " + uc.asWhat + ",\n"
	}
	sql += t.idCol + " AS __MT_ID\n"
	sql += "FROM " + t.table + "\n"
	sql += "WHERE " + t.table + "." + t.idCol + " == ?;"

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
			row, err = t.scan(stmt, true)
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

func (h *tableEditRowH) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	var err error
	tData := make(map[string]interface{})
	defer func() {
		if err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
		} else {
			err = templ.ExecuteTemplate(w, "editrow", tData)
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

	row, err := h.tInf.getRowForEdit(conn, idVal)
	if err != nil {
		return
	}

	kvPairs, err := h.tInf.getListOfKvPairs(conn)
	if err != nil {
		return
	}

	tData["userCols"] = h.tInf.userCols
	tData["kvPairs"] = kvPairs
	tData["row"] = row
	tData["groupDigitsFunc"] = groupDigitsFunc
	tData["cssRowClassFunc"] = h.tInf.cssRowClassFunc
	tData["eq"] = editRowEq
}

func editRowEq(x interface{}, y interface{}) bool {
	switch px := x.(type) {
	case *int:
		return editRowEq(*px, y)
	case *bool:
		return editRowEq(*px, y)
	case *string:
		return editRowEq(*px, y)
	}

	switch py := y.(type) {
	case *int:
		return editRowEq(x, *py)
	case *bool:
		return editRowEq(x, *py)
	case *string:
		return editRowEq(x, *py)
	}

	return x == y
}

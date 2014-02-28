package main

import (
	"github.com/MihaiB/forge/go/sqlite"
	"net/http"
)

type tableUpdateRowH struct {
	tInf *tableInfo
}

func (h *tableUpdateRowH) updateRow(conn *sqlite.Conn, vals []interface{},
	idVal interface{}) (err error) {
	sql := "UPDATE " + h.tInf.table + " SET\n"

	firstCol := true
	for _, uc := range h.tInf.userCols {
		if !uc.IsCol || uc.NoEdit {
			continue
		}
		if !firstCol {
			sql += ", "
		}
		sql += uc.what + " = ?"
		firstCol = false
	}

	sql += " WHERE " + h.tInf.idCol + " = ?;"

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

	err = stmt.Exec(append(vals, idVal)...)
	if err != nil {
		return
	}

	for stmt.Next() {
	}

	return
}

func (h *tableUpdateRowH) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	var err error
	defer func() {
		if err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
		}
	}()

	vals, err := h.tInf.getValuesFromReq(r, func(uc userCol) bool {
		return uc.skipWhenUpdatingRow()
	})
	if err != nil {
		return
	}

	parsedId, err := h.tInf.idColType.parseString(r.FormValue("dbRowId"))
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

	err = h.tInf.updateHook(conn, vals)
	if err != nil {
		return
	}

	err = h.updateRow(conn, vals, parsedId)
	if err != nil {
		return
	}
}

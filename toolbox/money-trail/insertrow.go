package main

import (
	"errors"
	"github.com/MihaiB/mihaib/toolbox/money-trail/sqlite"
	"net/http"
	"strconv"
)

type tableInsertRowH struct {
	tInf *tableInfo
}

func (t *tableInfo) getValuesFromReq(r *http.Request,
	skipCol func(userCol) bool) (vals []interface{}, err error) {
	err = r.ParseForm()
	if err != nil {
		return
	}

	for idx, uc := range t.userCols {
		if skipCol(uc) {
			continue
		}

		formKey := "col" + strconv.Itoa(idx)
		userVal := r.FormValue(formKey)
		var parsedVal interface{}

		foundNil := false
		if uc.Sel != nil || uc.WhatType == dbBool {
			if len(userVal) < 1 {
				err = errors.New("Invalid empty userVal")
				return
			}

			switch userVal[0] {
			case 'N':
				if len(userVal) != 1 {
					err = errors.New("Trailing chars " +
						"in NULL value")
					return
				}
				parsedVal = nil
				foundNil = true
			case 'V':
				userVal = userVal[1:]
			default:
				err = errors.New("Invalid val for selector: " +
					userVal)
				return
			}
		}

		if !foundNil {
			parsedVal, err = uc.WhatType.parseString(userVal)
			if err != nil {
				return
			}
		}
		vals = append(vals, parsedVal)
	}

	return
}

func (h *tableInsertRowH) insertRow(conn *sqlite.Conn, vals []interface{}) (
	err error) {
	sql := "INSERT INTO " + h.tInf.table + "("

	firstCol := true
	for _, uc := range h.tInf.userCols {
		if !uc.IsCol || uc.NoInsert {
			continue
		}
		if !firstCol {
			sql += ", "
		}
		sql += uc.what
		firstCol = false
	}

	sql = sql + ") VALUES ("
	for i := range vals {
		if i > 0 {
			sql += ", "
		}
		sql += "?"
	}
	sql += ");"

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

	err = stmt.Exec(vals...)
	if err != nil {
		return
	}

	for stmt.Next() {
	}

	return
}

func (h *tableInsertRowH) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	var err error
	defer func() {
		if err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
		}
	}()

	vals, err := h.tInf.getValuesFromReq(r, func(uc userCol) bool {
		return uc.skipWhenInsertingRow()
	})
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

	err = h.tInf.insertHook(conn, vals)
	if err != nil {
		return
	}

	err = h.insertRow(conn, vals)
	if err != nil {
		return
	}
}

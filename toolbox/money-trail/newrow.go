package main

import (
	"log"
	"net/http"
)

type tableNewRowH struct {
	tInf *tableInfo
}

func (h *tableNewRowH) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	var err error
	tData := make(map[string]interface{})
	defer func() {
		if err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
		} else {
			err = templ.ExecuteTemplate(w, "newrow", tData)
			if err != nil {
				log.Println("Error executing template:", err)
			}
		}
	}()

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

	kvPairs, err := h.tInf.getListOfKvPairs(conn)
	if err != nil {
		return
	}

	tData["userCols"] = h.tInf.userCols
	tData["kvPairs"] = kvPairs
}

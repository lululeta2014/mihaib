package main

import (
	"errors"
	"fmt"
	"github.com/MihaiB/mihaib/toolbox/gosqlite/sqlite"
	"log"
	"net/http"
	"net/url"
	"strconv"
	"strings"
)

const (
	ROWS_PER_PAGE = 25
)

func groupDigitsFunc(pn *int, noZeroOnGroup bool) string {
	n := *pn
	if n == 0 {
		if noZeroOnGroup {
			return ""
		}
		return "0"
	}

	negative := n < 0
	if negative {
		n = -n
	}

	var rev []int
	for n != 0 {
		rev = append(rev, n%1000)
		n /= 1000
	}

	result := []rune{}
	if negative {
		result = []rune{'-'}
	}
	for i := len(rev) - 1; i >= 0; i-- {
		var str string
		if i == len(rev)-1 {
			str = strconv.Itoa(rev[i])
		} else {
			str = fmt.Sprintf(",%03d", rev[i])
		}
		result = append(result, []rune(str)...)
	}

	return string(result)
}

func getPageCount(rowsInTable int) int {
	if rowsInTable < 0 {
		return 1
	}

	pages := rowsInTable / ROWS_PER_PAGE
	if rowsInTable%ROWS_PER_PAGE > 0 {
		pages++
	}
	if pages == 0 {
		pages = 1
	}
	return pages
}

// Make a slice for the pager template
func makePagerSlice(totalRows, crtPageNum int) []map[string]interface{} {
	pageCount := getPageCount(totalRows)
	result := make([]map[string]interface{}, pageCount)
	for i := 0; i < pageCount; i++ {
		first := 1 + i*ROWS_PER_PAGE
		last := first + ROWS_PER_PAGE - 1
		if last > totalRows {
			last = totalRows
		}
		result[i] = map[string]interface{}{
			"first":     first,
			"last":      last,
			"optionVal": i + 1,
			"selected":  i+1 == crtPageNum,
		}
	}
	return result
}

type dbType int

const (
	// Make the zero-value different from valid types
	_ dbType = iota

	dbInt

	// 1 is true, anything else is false
	dbBool

	dbText

	// 'YYYY-MM-DD', ordered using the SQL function date(..)
	dbDate
)

// SQL string for using in: IFNULL(what, sqlZeroVal)
func (t dbType) sqlZeroVal() string {
	switch t {
	case dbBool:
		fallthrough
	case dbInt:
		return "0"
	case dbDate:
		fallthrough
	case dbText:
		return "\"\""
	default:
		panic("Unknown dbType: " + strconv.Itoa(int(t)))
	}
	panic("not reached, required by compiler")
}

func (t dbType) newScanPtr() interface{} {
	switch t {
	case dbBool:
		var b bool
		return &b
	case dbInt:
		var i int
		return &i
	case dbDate, dbText:
		var s string
		return &s
	default:
		panic("unknown dbType: " + strconv.Itoa(int(t)))
	}
	panic("not reached")
}

// Get a value of (string, date-str, int, bool) from the string
func (t dbType) parseString(str string) (result interface{}, err error) {
	switch t {
	case dbText:
		result = str
	case dbDate:
		result, err = parseDate(str)
	case dbInt:
		result, err = strconv.Atoi(str)
	case dbBool:
		result, err = strconv.ParseBool(str)
	default:
		panic("Unknown db type: " + strconv.Itoa(int(t)))
	}

	return
}

// Silly helper function for the templates: to render the 'parsed date' element
func (t dbType) IsDateType() bool {
	return t == dbDate
}

type userCol struct {
	// SELECT (what); exception: see "Sel" below
	what string

	// AS asWhat
	asWhat string

	// Column title to display in table.
	// Must be empty if NoView && NoInsert && NoEdit. Otherwise Non-empty.
	DisplayName string

	// Hide DisplayName in table header for view;not hidden for Edit&Insert
	NoHeader bool

	// HTML <input> field size. Ignored if 0.
	InputFieldSize int

	// A column from the table, or an expression string?
	IsCol bool

	// The type of "what". Final type might differ: see "Sel" below
	WhatType dbType

	// Print as 1,234 – WhatType must be dbInt
	GroupDigits bool

	// Should groupDigitsFunc(0) return "0" or ""? Requires GroupDigits.
	NoZeroOnGroup bool

	// Not displayed when viewing a table row
	NoView bool

	// Not shown in "insert new row" form. Ignored if IsCol == false.
	NoInsert bool

	// Can't be changed when editing an existing row (if NoView is true,
	// it stays hidden; else it is displayed but not editable).
	// If NoEdit is false, the column value can be edited.
	// Ignored if IsCol == false.
	NoEdit bool

	// A selector to display valCol of valType from another table, or nil
	// Must be nil if IsCol == false.
	Sel *tableSelector
}

// Indicates that a userCol.what is an ID of type userCol.WhatType whose value
// is equal to table.keyCol but we display table.valCol of type valType.
type tableSelector struct {
	table   string
	keyCol  string
	valCol  string
	valType dbType

	AllowNull        bool   // Allow NULL value in database
	NullDropdownText string // Dropdown text for NULL option
}

// "what"'s type if no selector, else selector's type.
func (uc *userCol) finalType() dbType {
	if uc.Sel == nil {
		return uc.WhatType
	}
	return uc.Sel.valType
}

// Type in SELECT query for edit row: if editable, it's WhatType.
func (uc *userCol) finalTypeForEditRow() dbType {
	if uc.Sel == nil || !uc.NoEdit {
		return uc.WhatType
	}
	return uc.Sel.valType
}

// Skip this column when parsing col1=val1&col2=val2 for DB Insert request
func (uc *userCol) skipWhenInsertingRow() bool {
	return !uc.IsCol || uc.NoInsert
}

// Skip this column when parsing col1=val1&col2=val2 for DB Update request
func (uc *userCol) skipWhenUpdatingRow() bool {
	return !uc.IsCol || uc.NoEdit
}

// Key and Value pair for a selector userCol
type selKV struct {
	K interface{}
	V interface{}
}

func (uc *userCol) getSelKVPairs(conn *sqlite.Conn) (
	result []selKV, err error) {
	if uc.Sel == nil {
		err = errors.New("getSelKVPairs for nil selector")
		return
	}

	sql := "SELECT " + uc.Sel.keyCol + " AS key, " + "IFNULL("

	valIsDate := uc.Sel.valType == dbDate
	if valIsDate {
		sql += "DATE("
	}
	sql += uc.Sel.valCol
	if valIsDate {
		sql += ")"
	}

	sql += ", " + uc.Sel.valType.sqlZeroVal() + ")" +
		" AS val FROM " + uc.Sel.table +
		" WHERE " + uc.Sel.table + "." + uc.Sel.keyCol +
		" IS NOT NULL ORDER BY "

	if valIsDate {
		sql += "DATE("
	}
	sql += uc.Sel.table + "." + uc.Sel.valCol
	if valIsDate {
		sql += ")"
	}
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

	err = stmt.Exec()
	if err != nil {
		return
	}

	for stmt.Next() {
		crt := selKV{
			K: uc.WhatType.newScanPtr(),
			V: uc.Sel.valType.newScanPtr(),
		}
		err = stmt.Scan(crt.K, crt.V)
		if err != nil {
			return
		}

		result = append(result, crt)
	}

	return
}

type collationT string

const (
	collationBinary collationT = "BINARY"
	collationNoCase collationT = "NOCASE"
	collationRTrim  collationT = "RTRIM"
)

type orderingTerm struct {
	expr       string
	collation  collationT
	descending bool
}

// Knows all about a table and the Handlers for it.
type tableInfo struct {
	table     string
	idCol     string // name of ID column. Needed to edit rows.
	idColType dbType
	userCols  []userCol
	noTitles  bool           // hide column titles in View mode
	whereCond string         // WHERE condition to restrict rows, or empty
	groupBy   []string       // HACK; disables endpoints to no break them
	orderBy   []orderingTerm // Leave empty/nil to not sort.

	// Returns a CSS class for a row of View data, or the empty string "".
	cssRowClassFunc func(row []interface{}) string

	// Functions which check the data on insert/update
	insertHook func(conn *sqlite.Conn, vals []interface{}) error
	updateHook func(conn *sqlite.Conn, vals []interface{}) error
}

func (t *tableInfo) scan(stmt *sqlite.Stmt, forEditRow bool) (
	row []interface{}, err error) {
	row = make([]interface{}, len(t.userCols)+1)

	colTypes := make([]dbType, len(t.userCols)+1)
	for i, uc := range t.userCols {
		if forEditRow {
			colTypes[i] = uc.finalTypeForEditRow()
		} else {
			colTypes[i] = uc.finalType()
		}
	}
	colTypes[len(colTypes)-1] = t.idColType

	for idx, ct := range colTypes {
		row[idx] = ct.newScanPtr()
	}
	err = stmt.Scan(row...)
	return
}

func (t *tableInfo) getViewRowSql(extraWhereCond string) string {
	sql := "SELECT\n"
	for _, uc := range t.userCols {
		isDate := uc.finalType() == dbDate
		sql += "IFNULL("

		if isDate {
			sql += "DATE("
		}
		if uc.Sel == nil {
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

		sql += ", " + uc.finalType().sqlZeroVal() + ")"
		sql += " AS " + uc.asWhat + ",\n"
	}
	sql += t.idCol + " AS __MT_ID\n"
	sql += "FROM " + t.table + "\n"
	if t.whereCond != "" || extraWhereCond != "" {
		sql += "WHERE\n"
		if t.whereCond != "" && extraWhereCond != "" {
			sql += "(" + t.whereCond + ") AND (" +
				extraWhereCond + ")\n"
		} else {
			// exactly one non-empty
			sql += "(" + t.whereCond + extraWhereCond + ")\n"
		}
	}
	if len(t.groupBy) > 0 {
		sql += "GROUP BY \n"
		for idx, groupExpr := range t.groupBy {
			if idx > 0 {
				sql += ", "
			}
			sql += "(" + groupExpr + ")"
		}
		sql += "\n"
	}
	return sql
}

func (t *tableInfo) getPage(conn *sqlite.Conn, reqPage int) (
	rows [][]interface{}, totalRows int, page int, err error) {
	sql := t.getViewRowSql("")

	totalRows, err = getRowCount(conn, sql)
	if err != nil {
		return
	}
	pages := getPageCount(totalRows)
	page = reqPage
	switch {
	case page < 1:
		page = 1
	case page > pages:
		page = pages
	}

	if len(t.orderBy) > 0 {
		sql += "ORDER BY\n"
		for idx, ordTerm := range t.orderBy {
			if idx > 0 {
				sql += ", "
			}
			sql += "(" + ordTerm.expr + ")"
			if ordTerm.collation != "" {
				sql += " COLLATE " + string(ordTerm.collation)
			}
			if ordTerm.descending {
				sql += " DESC"
			}
		}
		sql += "\n"
	}

	sql += "LIMIT " + strconv.Itoa(ROWS_PER_PAGE) +
		" OFFSET " + strconv.Itoa((page-1)*ROWS_PER_PAGE) + ";"

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

	err = stmt.Exec()
	if err != nil {
		return
	}

	for stmt.Next() {
		var row []interface{}
		row, err = t.scan(stmt, false)
		if err != nil {
			return
		}
		rows = append(rows, row)
	}

	return
}

func (tInf *tableInfo) getListOfKvPairs(conn *sqlite.Conn) (
	kvPairs [][]selKV, err error) {
	for _, uc := range tInf.userCols {
		var kv []selKV
		switch {
		case !uc.IsCol || (uc.NoInsert && uc.NoEdit):
		case uc.Sel != nil:
			kv, err = uc.getSelKVPairs(conn)
			if err != nil {
				return
			}
		case uc.WhatType == dbBool:
			// kv returned above has K and V pointers, here values.
			// The template works with both.
			// Booleans can be 0,1 or false,true: ok for strconv.
			kv = []selKV{
				selKV{K: false, V: false},
				selKV{K: true, V: true},
			}
		}
		kvPairs = append(kvPairs, kv)
	}
	return
}

type tableSection struct {
	navLabel string
	path     string
	tInf     *tableInfo
}

func newTableSection(navLabel, table, idCol string, idColType dbType,
	userCols []userCol, noTitles bool, groupBy []string,
	orderBy []orderingTerm,
	mux *http.ServeMux, urlPattern string) *tableSection {
	if table == "" {
		panic("Empty table name")
	}
	for _, uc := range userCols {
		if uc.NoView && uc.NoInsert && uc.NoEdit &&
			uc.DisplayName != "" {
			panic("DisplayName present but " +
				"NoView, NoInsert and NoEdit")
		}
		if (uc.NoView && uc.NoInsert && uc.NoEdit) == false &&
			uc.DisplayName == "" {
			panic("View, Insert or Edit column " +
				"but empty DisplayName")
		}
		if uc.Sel != nil && uc.IsCol == false {
			panic("non-nil userCol selector but IsCol == false")
		}
		if uc.GroupDigits && uc.WhatType != dbInt {
			panic("userCol.GroupDigits true but WhatType != dbInt")
		}
		if uc.NoZeroOnGroup && !uc.GroupDigits {
			panic("userCol.NoZeroOnGroup without GroupDigits")
		}

		switch t := uc.WhatType; t {
		case dbBool:
		case dbInt:
		case dbDate:
		case dbText:
		default:
			panic("Unknown dbType: " + strconv.Itoa(int(t)))
		}
	}

	tInf := &tableInfo{
		table:           table,
		idCol:           idCol,
		idColType:       idColType,
		userCols:        userCols,
		noTitles:        noTitles,
		groupBy:         groupBy,
		orderBy:         orderBy,
		cssRowClassFunc: func(row []interface{}) string { return "" },
		insertHook: func(*sqlite.Conn, []interface{}) error {
			return nil
		},
		updateHook: func(*sqlite.Conn, []interface{}) error {
			return nil
		},
	}

	// If urlPrefix is empty, <a href="//name/"> means http://name
	pattern := "/" + strings.Trim(urlPattern, "/") + "/"
	result := &tableSection{navLabel: navLabel, path: pattern, tInf: tInf}
	mux.Handle(result.GetPath(), result)

	// groupBy pretty much breaks the whole model. So disabling endpoints.
	if len(groupBy) == 0 {
		mux.Handle(pattern+"viewrow/", &tableViewRowH{tInf: tInf})
		mux.Handle(pattern+"newrow/", &tableNewRowH{tInf: tInf})
		mux.Handle(pattern+"editrow/", &tableEditRowH{tInf: tInf})
		mux.Handle(pattern+"insertrow/", &tableInsertRowH{tInf: tInf})
		mux.Handle(pattern+"updaterow/", &tableUpdateRowH{tInf: tInf})
		mux.Handle(pattern+"deleterow/", &tableDeleteRowH{tInf: tInf})
	}

	return result
}

func (s *tableSection) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	tData := map[string]interface{}{
		"title":    s.navLabel + " – Money Trail",
		"noTitles": s.tInf.noTitles,
		"sections": sections,
		"isCrtSec": makeIsCrtSecFunc(s),
		"groupBy":  s.tInf.groupBy,
	}
	var err error
	redirectToPage := -1
	defer func() {
		// If there's an error, we want to show it; don't redirect
		if redirectToPage != -1 && err == nil {
			http.Redirect(w, r, "?page="+
				url.QueryEscape(strconv.Itoa(redirectToPage)),
				http.StatusFound)
			return
		}

		tData["err"] = err
		templErr := templ.ExecuteTemplate(w, "table", tData)
		if templErr != nil {
			log.Println("Error executing template:", templErr)
		}
	}()

	err = r.ParseForm()
	if err != nil {
		return
	}

	reqPage := 1
	if pageStr := r.FormValue("page"); pageStr != "" {
		reqPage, err = strconv.Atoi(pageStr)
		if err != nil {
			return
		}
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

	rows, totalRows, page, err := s.tInf.getPage(conn, reqPage)
	if err != nil {
		return
	}
	// User requested page 5 but we have only 4 (e.g. after a Delete).
	// Not optimal (we retrieve the results then redirect) but ok.
	if page != reqPage {
		redirectToPage = page
		return
	}

	// this is what the "single row template" expects
	rowsAndMeta := make([]map[string]interface{}, len(rows))
	for i, row := range rows {
		rowsAndMeta[i] = map[string]interface{}{
			"row":             row,
			"userCols":        s.tInf.userCols,
			"groupDigitsFunc": groupDigitsFunc,
			"cssRowClassFunc": s.tInf.cssRowClassFunc,
			"groupBy":         s.tInf.groupBy,
		}
	}
	tData["rowsAndMeta"] = rowsAndMeta
	tData["totalRows"] = totalRows
	tData["pagerSlice"] = makePagerSlice(totalRows, page)
	if page > 1 {
		tData["pagerPrev"] = page - 1
	}
	if page < getPageCount(totalRows) {
		tData["pagerNext"] = page + 1
	}
}

func (s *tableSection) GetPath() string {
	return s.path
}

func (s *tableSection) GetNavLabel() string {
	return s.navLabel
}

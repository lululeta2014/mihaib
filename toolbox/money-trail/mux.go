package main

import (
	"errors"
	"github.com/MihaiB/mihaib/toolbox/money-trail/sqlite"
	"log"
	"net/http"
	"strings"
)

var (
	authMux  = http.NewServeMux()
	sections = getSections()
)

func getSections() []siteSection {
	result := []siteSection{}

	home := &homeHandler{}
	authMux.Handle(home.GetPath(), home)
	result = append(result, home)

	tablesPrefix := ""

	balanceSql := `SELECT tableIn.total - tableOut.total FROM
		(SELECT IFNULL(SUM(amount), 0) as total FROM transactions
			WHERE to_account = accounts.ID) as tableIn,
		(SELECT IFNULL(SUM(amount), 0) as total FROM transactions
			WHERE from_account = accounts.ID) as tableOut`

	acctCols := []userCol{
		userCol{
			what:           "name",
			asWhat:         "name",
			DisplayName:    "Account",
			InputFieldSize: 14,
			IsCol:          true,
			WhatType:       dbText,
		},
		userCol{
			what:        balanceSql,
			asWhat:      "balance",
			DisplayName: "Balance",
			WhatType:    dbInt,
			GroupDigits: true,
		},
		userCol{
			what:        "currency",
			asWhat:      "currency",
			DisplayName: "Currency",
			IsCol:       true,
			WhatType:    dbInt,
			Sel: &tableSelector{
				table:   "currencies",
				keyCol:  "ID",
				valCol:  "name",
				valType: dbText,
			},
		},
		userCol{
			what:        "closed",
			asWhat:      "closed",
			DisplayName: "Closed",
			IsCol:       true,
			WhatType:    dbBool,
			NoView:      true,
			NoInsert:    true,
		},
	}
	openAcctsSec := newTableSection(
		"Accounts", "accounts", "ID", dbInt, acctCols, true, nil,
		[]orderingTerm{orderingTerm{expr: "accounts.name"}},
		authMux, tablesPrefix+"/accounts",
	)
	openAcctsSec.tInf.whereCond = "closed == 0"
	closedAcctsSec := newTableSection(
		"(old)", "accounts", "ID", dbInt, acctCols, true, nil,
		[]orderingTerm{orderingTerm{expr: "accounts.name"}},
		authMux, tablesPrefix+"/accounts_closed",
	)
	closedAcctsSec.tInf.whereCond = "closed == 1"
	result = append(result, openAcctsSec, closedAcctsSec)

	transactionTypeSql := `CASE WHEN from_account IS NULL THEN 'Income'
		WHEN to_account IS NULL THEN 'Payment'
		ELSE 'Transfer' END`

	transactionCurrencySql := `SELECT currencies.name
		FROM accounts, currencies
		WHERE (accounts.ID = from_account OR accounts.ID = to_account)
		AND currencies.ID = accounts.currency`

	transactionsSec := newTableSection(
		"Transact", "transactions", "ID", dbInt,
		[]userCol{
			userCol{
				what:        transactionTypeSql,
				asWhat:      "transaction_type",
				DisplayName: "Type",
				WhatType:    dbText,
			},
			userCol{
				what:        "from_account",
				asWhat:      "from_account",
				DisplayName: "From",
				IsCol:       true,
				WhatType:    dbInt,
				Sel: &tableSelector{
					table:     "accounts",
					keyCol:    "ID",
					valCol:    "name",
					valType:   dbText,
					AllowNull: true,
				},
			},
			userCol{
				what:        "to_account",
				asWhat:      "to_account",
				DisplayName: "To",
				IsCol:       true,
				WhatType:    dbInt,
				Sel: &tableSelector{
					table:     "accounts",
					keyCol:    "ID",
					valCol:    "name",
					valType:   dbText,
					AllowNull: true,
				},
			},
			userCol{
				what:           "date",
				asWhat:         "date",
				DisplayName:    "Date",
				InputFieldSize: 7,
				IsCol:          true,
				WhatType:       dbDate,
			},
			userCol{
				what:           "description",
				asWhat:         "description",
				DisplayName:    "Description",
				InputFieldSize: 15,
				IsCol:          true,
				WhatType:       dbText,
			},
			userCol{
				what:        "category",
				asWhat:      "category",
				DisplayName: "Category",
				NoHeader:    true,
				IsCol:       true,
				WhatType:    dbInt,
				Sel: &tableSelector{
					table:   "categories",
					keyCol:  "ID",
					valCol:  "name",
					valType: dbText,
				},
			},
			userCol{
				what:           "amount",
				asWhat:         "amount",
				DisplayName:    "Amount",
				NoHeader:       true,
				InputFieldSize: 3,
				IsCol:          true,
				WhatType:       dbInt,
				GroupDigits:    true,
			},
			userCol{
				what:        transactionCurrencySql,
				asWhat:      "currency_name",
				DisplayName: "Currency",
				NoHeader:    true,
				WhatType:    dbText,
			},
		},
		false, nil,
		[]orderingTerm{orderingTerm{expr: "date", descending: true}},
		authMux, tablesPrefix+"/transactions")
	transactionsSec.tInf.cssRowClassFunc =
		transCssRowClassFunc(transactionsSec)
	transactionsSec.tInf.insertHook = transactionsCheckAccountsHook(
		transactionsSec,
		func(uc userCol) bool {
			return uc.skipWhenInsertingRow()
		},
	)
	transactionsSec.tInf.updateHook = transactionsCheckAccountsHook(
		transactionsSec,
		func(uc userCol) bool {
			return uc.skipWhenUpdatingRow()
		},
	)
	result = append(result, transactionsSec)

	result = append(result, newTableSection(
		"Categ", "categories", "ID", dbInt,
		[]userCol{
			userCol{
				what:           "name",
				asWhat:         "name",
				DisplayName:    "Category",
				InputFieldSize: 10,
				IsCol:          true,
				WhatType:       dbText,
			},
		},
		true, nil,
		[]orderingTerm{orderingTerm{expr: "categories.name"}},
		authMux, tablesPrefix+"/categories",
	))

	result = append(result, newTableSection(
		"Coin", "currencies", "ID", dbInt,
		[]userCol{
			userCol{
				what:           "name",
				asWhat:         "name",
				DisplayName:    "Currency",
				InputFieldSize: 3,
				IsCol:          true,
				WhatType:       dbText,
			},
		},
		true, nil,
		// SELECT IFNULL(name, "") as name ... ORDER BY x;
		// So if x is "name", it refers to the IFNULL(..) result, and
		// you must specify the NOCASE collation (default is BINARY).
		// currencies.name is the column, with NOCASE collation.
		[]orderingTerm{orderingTerm{expr: "currencies.name"}},
		authMux, tablesPrefix+"/currencies",
	))

	result = append(result, getReportsSection("Month", false, false,
		authMux, tablesPrefix+"/reports"))
	result = append(result, getReportsSection("[cat]", false, true,
		authMux, tablesPrefix+"/reports_categ"))

	result = append(result, getReportsSection("Year", true, false,
		authMux, tablesPrefix+"/reports_year"))
	result = append(result, getReportsSection("[cat]",
		true, true,
		authMux, tablesPrefix+"/reports_year_categ"))

	logout := &logoutHandler{}
	authMux.Handle(logout.GetPath(), logout)
	result = append(result, logout)

	quit := &quitHandler{}
	authMux.Handle(quit.GetPath(), quit)
	result = append(result, quit)

	return result
}

func transCssRowClassFunc(sec *tableSection) func(row []interface{}) string {
	userCols := sec.tInf.userCols
	i := 0
	for i = 0; i < len(userCols); i++ {
		if userCols[i].asWhat == "transaction_type" {
			break
		}
	}
	if i == len(userCols) {
		panic("transaction_type col not found")
	}

	return func(row []interface{}) string {
		switch v := row[i].(type) {
		case *string:
			switch strings.ToLower(*v) {
			case "income":
				return "incomeTransactionRow"
			case "payment":
				return "paymentTransactionRow"
			case "transfer":
				return "transferTransactionRow"
			default:
				log.Println("Unexpected transaction_type", *v)
			}
		default:
			log.Println("Unexpected transaction_type row value")
		}
		return ""
	}
}

func transactionsCheckAccountsHook(sec *tableSection,
	skipCol func(userCol) bool) (
	// using underscore for param name so "go fmt" doesnt make long lines
	_ func(conn *sqlite.Conn, vals []interface{}) error) {
	idx, from_idx, to_idx := 0, -1, -1
	for i := 0; i < len(sec.tInf.userCols); i++ {
		if skipCol(sec.tInf.userCols[i]) {
			continue
		}
		switch sec.tInf.userCols[i].what {
		case "from_account":
			from_idx = idx
		case "to_account":
			to_idx = idx
		}
		if from_idx >= 0 && to_idx >= 0 {
			break
		}
		idx++
	}
	if from_idx < 0 || to_idx < 0 {
		panic("Can't find from_account and to_account")
	}

	return func(conn *sqlite.Conn, vals []interface{}) (err error) {
		v1, v2 := vals[from_idx], vals[to_idx]
		// DB takes care of this
		if v1 == nil || v2 == nil {
			return
		}

		stmt, err := conn.Prepare("SELECT " +
			"(SELECT currency FROM accounts WHERE ID = ?)" +
			"==" +
			"(SELECT currency FROM accounts WHERE ID = ?);")
		if err != nil {
			return
		}
		defer func() {
			err2 := stmt.Finalize()
			if err == nil {
				err = err2
			}
		}()

		err = stmt.Exec(v1, v2)
		if err != nil {
			return
		}

		foundRow := false
		for stmt.Next() {
			if !foundRow {
				var b bool
				err = stmt.Scan(&b)
				if err != nil {
					return
				}
				if !b {
					err = errors.New(
						"Account currencies differ")
					return
				}
				foundRow = true
			} else {
				err = errors.New("Found multiple result rows")
				return
			}
		}
		if !foundRow {
			err = errors.New("Hook found no result rows")
			return
		}

		return
	}
}

func getReportsSection(navLabel string, perYear bool, groupCategories bool,
	authMux *http.ServeMux, pattern string) *tableSection {
	sumInSql :=
		"SUM(CASE WHEN from_account IS NULL THEN amount ELSE 0 END)"
	sumOutSql :=
		"-SUM(CASE WHEN to_account IS NULL THEN amount ELSE 0 END)"
	sumTotalSql := "SUM(CASE " +
		"WHEN from_account IS NULL THEN amount " +
		"WHEN to_account IS NULL THEN -amount " +
		"ELSE 0 END)"

	currencyIdSql := "SELECT currency FROM accounts " +
		"WHERE ID = from_account OR ID = to_account"
	currencyNameSql := "SELECT name FROM currencies WHERE ID = (" +
		"SELECT currency FROM accounts WHERE ID = from_account OR " +
		"ID = to_account)"

	monthForGroupingSql := "strftime('%Y-%m-01', date)"
	monthForDisplaySql := "strftime('%m–%Y', date) || ' (' || " +
		"CASE strftime('%m', date) " +
		"WHEN '01' THEN 'Jan' " + "WHEN '02' THEN 'Feb' " +
		"WHEN '03' THEN 'Mar' " + "WHEN '04' THEN 'Apr' " +
		"WHEN '05' THEN 'May' " + "WHEN '06' THEN 'Jun' " +
		"WHEN '07' THEN 'Jul' " + "WHEN '08' THEN 'Aug' " +
		"WHEN '09' THEN 'Sep' " + "WHEN '10' THEN 'Oct' " +
		"WHEN '11' THEN 'Nov' " + "WHEN '12' THEN 'Dec' " +
		"ELSE 'UNKNOWN' END" +
		"|| ')'"

	yearForGroupingSql := "strftime('%Y-01-01', date)"
	yearForDisplaySql := "strftime('%Y', date)"

	var dateForGroupingSql, dateForDisplaySql string
	if perYear {
		dateForGroupingSql = yearForGroupingSql
		dateForDisplaySql = yearForDisplaySql
	} else {
		dateForGroupingSql = monthForGroupingSql
		dateForDisplaySql = monthForDisplaySql
	}

	userCols := []userCol{
		userCol{
			what:     dateForGroupingSql,
			asWhat:   "date_for_grouping",
			WhatType: dbText,
			NoView:   true,
			NoInsert: true,
			NoEdit:   true,
		},
		userCol{
			what:        dateForDisplaySql,
			asWhat:      "date_for_display",
			DisplayName: "Date",
			WhatType:    dbText,
		},
	}

	if groupCategories {
		userCols = append(userCols, []userCol{
			userCol{
				what:        "category",
				asWhat:      "category_name",
				IsCol:       true,
				DisplayName: "Category",
				WhatType:    dbInt,
				NoInsert:    true,
				NoEdit:      true,
				Sel: &tableSelector{
					table:   "categories",
					keyCol:  "ID",
					valCol:  "name",
					valType: dbText,
				},
			},
		}...)
	}

	userCols = append(userCols, []userCol{
		userCol{
			what:        sumTotalSql,
			asWhat:      "sum_total",
			DisplayName: "Total",
			WhatType:    dbInt,
			GroupDigits: true,
		},
		userCol{
			what:     currencyIdSql,
			asWhat:   "currency_id",
			WhatType: dbInt,
			NoView:   true,
			NoInsert: true,
			NoEdit:   true,
		},
		userCol{
			what:        currencyNameSql,
			asWhat:      "currency_name",
			DisplayName: "Currency",
			WhatType:    dbText,
		},
		userCol{
			what:        "'='",
			asWhat:      "equal_sign",
			DisplayName: "=",
			WhatType:    dbText,
		},
		userCol{
			what:          sumInSql,
			asWhat:        "sum_in",
			DisplayName:   "In",
			WhatType:      dbInt,
			GroupDigits:   true,
			NoZeroOnGroup: true,
		},
		userCol{
			what:          sumOutSql,
			asWhat:        "sum_out",
			DisplayName:   "Out",
			WhatType:      dbInt,
			GroupDigits:   true,
			NoZeroOnGroup: true,
		},
	}...)

	groupBy := []string{"DATE(date_for_grouping)"}
	if groupCategories {
		// defensive programming: in case we SELECT .. AS category
		groupBy = append(groupBy, "transactions.category")
	}
	groupBy = append(groupBy, "currency_id")

	orderBy := []orderingTerm{
		orderingTerm{
			// If we display "Jan 2012, Jan 2011" we can't
			// order by that format.
			expr:       "DATE(date_for_grouping)",
			descending: true,
		},
		orderingTerm{
			expr:       "abs(sum_total)",
			descending: true,
		},
	}
	if groupCategories {
		// In case multiple categories have the same amount
		orderBy = append(orderBy, orderingTerm{
			expr:      "category_name",
			collation: collationNoCase,
		})
	}
	orderBy = append(orderBy, orderingTerm{
		expr:      "currency_name",
		collation: collationNoCase,
	})

	result := newTableSection(navLabel, "transactions", "ID", dbInt,
		userCols,
		true,
		groupBy,
		orderBy,
		authMux, pattern,
	)
	// exclude rows which are transfers between accounts: if they are the
	// only rows for that currency in a specific month, the result would
	// contain a row with In, Out and Total equal to 0.
	result.tInf.whereCond = "to_account IS NULL OR from_account IS NULL"
	result.tInf.cssRowClassFunc = reportCssRowClassFunc(result)
	return result
}

func reportCssRowClassFunc(sec *tableSection) func(row []interface{}) string {
	userCols := sec.tInf.userCols
	i := 0
	for i = 0; i < len(userCols); i++ {
		if userCols[i].asWhat == "sum_total" {
			break
		}
	}
	if i == len(userCols) {
		panic("sum_total col not found")
	}

	return func(row []interface{}) string {
		switch v := row[i].(type) {
		case *int:
			switch {
			case *v > 0:
				return "incomeReportRow"
			case *v < 0:
				return "paymentReportRow"
			case *v == 0:
				return ""
			}
		default:
			log.Println("Unexpected sum_total row value")
		}
		return ""
	}
}

type siteSection interface {
	http.Handler

	// URL path
	GetPath() string

	// Label to show in navigation, empty string to hide from navigation
	GetNavLabel() string
}

// Helps make the function used by the navigation template
func makeIsCrtSecFunc(crtSec siteSection) func(s siteSection) bool {
	return func(s siteSection) bool {
		return s == crtSec
	}
}

type homeHandler struct{}

func (*homeHandler) GetPath() string {
	return "/"
}

func (*homeHandler) GetNavLabel() string {
	return ""
}

func (h *homeHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	var err error
	defer func() {
		if err != nil {
			http.Error(w, err.Error(),
				http.StatusInternalServerError)
		} else {
			err = templ.ExecuteTemplate(w, "home",
				map[string]interface{}{
					"sections": sections,
					"isCrtSec": makeIsCrtSecFunc(h),
				})
			if err != nil {
				log.Println("Error executing template", err)
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

	err = create_tables_if_missing(conn)
	if err != nil {
		return
	}
}

type logoutHandler struct{}

func (*logoutHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	removeCookie(r)
	templ.ExecuteTemplate(w, "logout", map[string]interface{}{
		"title": "Logout – Money Trail",
	})
}

func (*logoutHandler) GetPath() string {
	return "/logout/"
}

func (*logoutHandler) GetNavLabel() string {
	return "Logout"
}

type quitHandler struct{}

func (*quitHandler) GetPath() string {
	return "/quit/"
}

func (*quitHandler) GetNavLabel() string {
	return "Quit"
}

func (*quitHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	ok := canShutdown(getUser(r))
	templ.ExecuteTemplate(w, "quit", map[string]interface{}{
		"title":       "Quit – Money Trail",
		"canShutdown": ok,
	})
	if ok {
		wsListener.Close()
	}
}

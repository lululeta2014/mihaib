package main

import (
	"errors"
	"fmt"
	"log"
	"net/http"
	"strconv"
	"strings"
	"time"
)

var (
	dateSepMap = make(map[rune]bool)
	dateMonths = make(map[string]int)
)

func init() {
	authMux.HandleFunc("/date/", dateHandleFunc)

	for _, s := range " \t/\\:-,." {
		dateSepMap[s] = true
	}

	for idx, month := range []string{"january", "february", "march",
		"april", "may", "june", "july", "august", "september",
		"october", "november", "december"} {
		dateMonths[strings.ToLower(month)] = idx + 1
		dateMonths[strings.ToLower(month)[:3]] = idx + 1
	}
}

func isMonthName(name string) bool {
	_, ok := dateMonths[strings.ToLower(name)]
	return ok
}

func getMonthByName(name string) int {
	return dateMonths[strings.ToLower(name)]
}

func isDay(day string) bool {
	n, err := strconv.Atoi(day)
	return err == nil && n > 0 && n <= 31
}

func getDay(day string) int {
	n, _ := strconv.Atoi(day)
	return n
}

func getDateFromSegments(parts []string) (result string, err error) {
	var year, month, day int
	defer func() {
		if err == nil {
			result = fmt.Sprintf("%d-%02d-%02d", year, month, day)
		}
	}()

	switch len(parts) {
	case 1:
		// This is what strings.Split returns for an empty string
		if parts[0] == "" {
			nw := time.Now()
			year, month, day = nw.Year(), int(nw.Month()), nw.Day()
			return
		}
	case 2:
		switch {
		case isMonthName(parts[0]) && isDay(parts[1]):
			month = getMonthByName(parts[0])
			day = getDay(parts[1])
			year = time.Now().Year()
			return
		case isDay(parts[0]) && isMonthName(parts[1]):
			day = getDay(parts[0])
			month = getMonthByName(parts[1])
			year = time.Now().Year()
			return
		}
	case 3:
		year, err = strconv.Atoi(parts[0])
		if err != nil || year < 0 {
			break
		}

		if isMonthName(parts[1]) {
			month = getMonthByName(parts[1])
		} else {
			month, err = strconv.Atoi(parts[1])
			if err != nil || month < 1 || month > 12 {
				break
			}
		}

		if isDay(parts[2]) {
			day = getDay(parts[2])
		} else {
			break
		}
		return
	}
	err = errors.New("Invalid date")
	return
}

func parseDate(userStr string) (result string, err error) {
	lastWasDash := false
	for _, c := range userStr {
		if dateSepMap[c] {
			if !lastWasDash {
				result += "-"
				lastWasDash = true
			}
		} else {
			result += string(c)
			lastWasDash = false
		}
	}
	result = strings.Trim(result, "-")

	parts := strings.Split(result, "-")
	result, err = getDateFromSegments(parts)
	return
}

func dateHandleFunc(w http.ResponseWriter, r *http.Request) {
	var err error
	var result string
	defer func() {
		if err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
		} else {
			_, err = fmt.Fprint(w, result)
			if err != nil {
				log.Println("Error writing date/ result:", err)
			}
		}
	}()

	err = r.ParseForm()
	if err != nil {
		return
	}

	result, err = parseDate(r.FormValue("date"))
	if err != nil {
		return
	}
}

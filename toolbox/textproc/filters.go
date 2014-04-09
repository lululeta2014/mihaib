package textproc

import (
	"fmt"
	"sort"
	"strconv"
	"strings"
	"unicode"
)

// Returns a new filter which returns its source token stream
// and an extra '\n' token if the source token stream wasn't empty
// and its last token didn't end with '\n'.
func NewNewlineAtEnd(r TokenReader) TokenReader {
	return &newNewlineAtEnd{wrapped: r}
}

type newNewlineAtEnd struct {
	wrapped    TokenReader
	hasTokens  bool
	endsWithNl bool
}

func (r *newNewlineAtEnd) ReadToken() ([]rune, error) {
	tok, err := r.wrapped.ReadToken()
	if len(tok) > 0 {
		r.hasTokens = true
		r.endsWithNl = tok[len(tok)-1] == '\n'
		return tok, nil
	}
	if r.hasTokens && !r.endsWithNl {
		r.endsWithNl = true
		return []rune{'\n'}, nil
	}
	return nil, err
}

// Returns a new filter which groups the runes into tokens of the given size.
// The last token may be shorter. If size is < 1 it is set to 1.
// If the TokenReader argument is already a fixed token size reader with the
// same size, it is returned.
func NewFixedTokenSize(r TokenReader, size int) TokenReader {
	if size < 1 {
		size = 1
	}
	fixR, ok := r.(*fixedTokenSize)
	if ok && fixR.size == size {
		return fixR
	}
	return &fixedTokenSize{wrapped: r, size: size}
}

type fixedTokenSize struct {
	wrapped TokenReader
	buf     []rune
	size    int
	err     error
}

func (r *fixedTokenSize) ReadToken() ([]rune, error) {
	for r.err == nil && len(r.buf) < r.size {
		var tok []rune
		tok, r.err = r.wrapped.ReadToken()
		r.buf = append(r.buf, tok...)
	}
	if len(r.buf) > 0 {
		n := r.size
		if len(r.buf) < n {
			n = len(r.buf)
		}
		tok := r.buf[:n:n]
		r.buf = r.buf[n:]
		return tok, nil
	}
	return nil, r.err
}

// Returns a new TokenReader which strips white space (except the '\n' rune)
// occuring before a '\n' rune or at the end of the stream.
// The stream of runes is arbitrarily broken into tokens.
func NewStripTrailingSpace(r TokenReader) TokenReader {
	return &newStripTrailingSpace{wrapped: r}
}

type newStripTrailingSpace struct {
	wrapped  TokenReader
	depleted bool // the wrapped reader is depleted
	buf      []rune
	idx      int // cursor in buffer, only trailing spaces before it
	err      error
}

func (*newStripTrailingSpace) isSpace(r rune) bool {
	return r != '\n' && unicode.IsSpace(r)
}

func (r *newStripTrailingSpace) extractBuf() []rune {
	tok := r.buf[:r.idx:r.idx]
	r.buf = r.buf[r.idx:]
	r.idx = 0

	i := len(tok) - 1
	hasNl := tok[i] == '\n'
	if hasNl {
		i--
	}
	// need to check i>=0 for the " \n" case
	for i >= 0 && r.isSpace(tok[i]) {
		i--
	}
	if hasNl {
		i++
		tok[i] = '\n'
	}
	return tok[:i+1]
}

func (r *newStripTrailingSpace) ReadToken() ([]rune, error) {
	for !r.depleted || r.idx < len(r.buf) {
		found := false
		for ; r.idx < len(r.buf); r.idx++ {
			x := r.buf[r.idx]
			if r.isSpace(x) {
				if found {
					break
				}
			} else {
				found = true
				if x == '\n' {
					r.idx++
					break
				}
			}
		}
		if found {
			return r.extractBuf(), nil
		}

		if !r.depleted {
			var tok []rune
			tok, r.err = r.wrapped.ReadToken()
			r.depleted = len(tok) == 0
			r.buf = append(r.buf, tok...)
		}
	}

	if len(r.buf) > 0 {
		// discard trailing whitespace at end of file to free up mem
		r.buf = nil
		r.idx = 0
	}

	return nil, r.err
}

// Returns a new TokenReader which replaces all '\t' runes with one or more
// ' ' runes until the number of runes after the last '\n' rune
// (or from the start of the stream, if there is no '\n' rune so far)
// is a multiple of tabSize. If tabSize < 1 it is set to 1.
// The rune stream is arbitrarily broken into tokens.
func NewExpandTab(r TokenReader, tabSize int) TokenReader {
	if tabSize < 1 {
		tabSize = 1
	}
	return &expandTab{r: NewFixedTokenSize(r, 1), tabSize: tabSize}
}

type expandTab struct {
	r        TokenReader
	err      error
	tabSize  int
	lineSize int
}

func (r *expandTab) ReadToken() ([]rune, error) {
	if r.err != nil {
		return nil, r.err
	}
	var wrappedTok []rune
	wrappedTok, r.err = r.r.ReadToken()
	if len(wrappedTok) > 0 {
		rn := wrappedTok[0]
		var tok []rune
		switch rn {
		case '\n':
			tok = []rune{rn}
			r.lineSize = 0
		case '\t':
			tok = make([]rune, r.tabSize-r.lineSize)
			for i := range tok {
				tok[i] = ' '
			}
			r.lineSize = 0
		default:
			r.lineSize = (r.lineSize + 1) % r.tabSize
			tok = []rune{rn}
		}
		return tok, nil
	}
	return r.ReadToken()
}

// Returns a new TokenReader which replaces "\r\n" and "\r" (if not followed
// immediately by a "\n") with "\n". The stream of runes is arbitrarily broken
// into tokens.
func NewUnixNewline(r TokenReader) TokenReader {
	return &unixNewline{r: NewFixedTokenSize(r, 1)}
}

type unixNewline struct {
	r          TokenReader
	err        error
	crBuffered bool
}

func (r *unixNewline) ReadToken() ([]rune, error) {
	var tok []rune
	tok, r.err = r.r.ReadToken()
	if len(tok) == 0 {
		var result []rune
		if r.crBuffered {
			result = []rune{'\n'}
			r.crBuffered = false
		}
		return result, r.err
	}

	r1 := tok[0]
	if r.crBuffered {
		result := []rune{'\n'}
		switch r1 {
		case '\n':
			r.crBuffered = false
		case '\r':
		default:
			r.crBuffered = false
			result = append(result, r1)
		}
		return result, r.err
	}

	if r1 != '\r' {
		return []rune{r1}, r.err
	}

	result := []rune{'\n'}
	tok, r.err = r.r.ReadToken()
	if len(tok) == 0 {
		return result, r.err
	}
	r2 := tok[0]
	switch r2 {
	case '\n':
	case '\r':
		r.crBuffered = true
	default:
		result = append(result, r2)
	}
	return result, r.err
}

// Returns a new TokenReader which strips whitespace (except the '\n' rune)
// at the start of the stream or following a '\n' rune.
// The stream of runes is arbitrarily broken into tokens.
func NewStripLeadingSpace(r TokenReader) TokenReader {
	return &stripLeadingSpace{r: NewFixedTokenSize(r, 1), atStart: true}
}

type stripLeadingSpace struct {
	r       TokenReader
	err     error
	atStart bool
}

func (r *stripLeadingSpace) ReadToken() ([]rune, error) {
	var tok []rune
	tok, r.err = r.r.ReadToken()
	for r.atStart && len(tok) > 0 && tok[0] != '\n' &&
		unicode.IsSpace(tok[0]) {
		tok, r.err = r.r.ReadToken()
	}
	if len(tok) == 0 {
		return nil, r.err
	}
	if tok[0] == '\n' {
		r.atStart = true
		return tok, r.err
	}
	if r.atStart {
		// tok[0] is not '\n' and not a leading space
		r.atStart = false
	}
	return tok, r.err
}

// Returns a new TokenReader which replaces consecutive space runes
// (except '\n') with a single space ' ' rune.
// The stream of runes is arbitrarily broken into tokens.
func NewSingleSpace(r TokenReader) TokenReader {
	return &singleSpace{r: NewFixedTokenSize(r, 1)}
}

type singleSpace struct {
	r          TokenReader
	err        error
	afterSpace bool
}

func (r *singleSpace) ReadToken() ([]rune, error) {
	var tok []rune
	tok, r.err = r.r.ReadToken()
	for r.afterSpace && len(tok) > 0 && tok[0] != '\n' &&
		unicode.IsSpace(tok[0]) {
		tok, r.err = r.r.ReadToken()
	}
	if len(tok) == 0 {
		return nil, r.err
	}
	rn := tok[0]
	if rn == '\n' {
		r.afterSpace = false
		return []rune{rn}, r.err
	}
	if unicode.IsSpace(tok[0]) {
		// r.afterSpace is false
		r.afterSpace = true
		return []rune{' '}, r.err
	} else {
		r.afterSpace = false
		return []rune{rn}, r.err
	}
}

// Returns a new TokenReader which converts formatted paragraphs, which occupy
// multiple adjacent lines and are separated by empty or whitespace-only lines,
// to logical paragraphs, which occupy a single line.
// Removes leading and trailing whitespace and empty or whitespace-only lines,
// replaces consecutive whitespace with a single space and ensures a newline
// at end of output if the output isn't empty.
// Lines are terminated by '\n' and the input is broken into tokens of
// arbitrary size.
func NewLogicalParagraphs(r TokenReader) TokenReader {
	r = NewStripLeadingSpace(NewStripTrailingSpace(r))
	r = NewSingleSpace(r)
	r = NewFixedTokenSize(r, 1)
	return NewNewlineAtEnd(&logicalParagraphs{r: r})
}

type logicalParagraphs struct {
	r           TokenReader
	inParagraph bool
}

func (r *logicalParagraphs) ReadToken() ([]rune, error) {
	for {
		tok, err := r.r.ReadToken()
		if len(tok) == 0 {
			return nil, err
		}

		a := tok[0]
		if !r.inParagraph {
			if a == '\n' {
				// continue an infinite loop instead of doing a
				// recursive call to use O(1) space
				continue
			}
			r.inParagraph = true
			return []rune{a}, nil
		}

		if a == '\n' {
			tok, err = r.r.ReadToken()
			if len(tok) == 0 {
				return nil, err
			}

			b := tok[0]
			if b == '\n' {
				r.inParagraph = false
				return []rune{'\n'}, nil
			}
			return []rune{' ', b}, nil
		}
		return []rune{a}, nil
	}
}

// Returns a new TokenReader which converts logical paragraphs, which occupy
// a single line, to formatted paragraphs, which occupy multiple adjacent
// lines and are separated by a blank line. Removes leading and trailing
// whitespace and whitespace-only lines, replaces consecutive whitespace
// with a single space and ensures a newline at end of output if the output
// isn't empty.
//
// Lines are broken only on whitespace, after at most lineSize runes.
// Loger words (more than lineSize consecutive non-whitespace runes) are
// placed on a separate line unbroken.
// If lineSize is < 1 it is set to 1.
//
// Lines are terminated by '\n' and the input is broken into arbitrary tokens.
func NewFormattedParagraphs(r TokenReader, lineSize int) TokenReader {
	if lineSize < 1 {
		lineSize = 1
	}
	r = NewStripLeadingSpace(NewStripTrailingSpace(r))
	r = NewSingleSpace(r)
	r = NewFixedTokenSize(r, 1)
	return NewNewlineAtEnd(&formattedParagraphs{r: r, lineSize: lineSize})
}

type formattedParagraphs struct {
	r        TokenReader
	lineSize int

	inParagraph        bool
	finishedParagraphs bool
	word               []rune
	crtLineSize        int
}

func (r *formattedParagraphs) emitWord() []rune {
	var result []rune
	if r.crtLineSize == 0 {
		result = r.word
		r.crtLineSize = len(r.word)
	} else {
		result = append([]rune{' '}, r.word...)
		if r.crtLineSize+len(result) > r.lineSize {
			result[0] = '\n'
			r.crtLineSize = len(r.word)
		} else {
			r.crtLineSize += len(result)
		}
	}
	r.word = nil
	return result
}

func (r *formattedParagraphs) ReadToken() ([]rune, error) {
	for {
		tok, err := r.r.ReadToken()
		if !r.inParagraph {
			if len(tok) == 0 {
				return nil, err
			}
			rn := tok[0]
			if rn == '\n' {
				continue
			}

			r.inParagraph = true
			r.word = []rune{rn}
			if r.finishedParagraphs {
				return []rune("\n\n"), nil
			}
			continue
		}

		if len(tok) == 0 || tok[0] == '\n' {
			result := r.emitWord()

			r.inParagraph = false
			r.finishedParagraphs = true
			r.crtLineSize = 0
			return result, nil
		}
		rn := tok[0]
		if unicode.IsSpace(rn) {
			return r.emitWord(), nil
		}
		r.word = append(r.word, rn)
	}
}

// Returns a new TokenReader which breaks long lines, on whitespace, to at most
// lineSize runes. If lineSize is < 1 it is set to 1. If a word (consecutive
// non-whitespace runes) is longer than lineSize, it is placed unbroken on a
// line by itself.
// Strips leading and trailing whitespace and replaces multiple whitespace
// runes with a single space.
// Input lines end with '\n' and the input is arbitrarily split into tokens.
func NewBreakLines(r TokenReader, lineSize int) TokenReader {
	if lineSize < 1 {
		lineSize = 1
	}
	r = NewStripTrailingSpace(NewStripLeadingSpace(r))
	r = NewSingleSpace(r)
	r = NewFixedTokenSize(r, 1)
	return &breakLines{r: r, lineSize: lineSize}
}

type breakLines struct {
	r        TokenReader
	lineSize int

	crtLineSize int
	word        []rune
}

func (r *breakLines) emitWord() []rune {
	var result []rune
	if r.crtLineSize == 0 {
		result = r.word
		r.crtLineSize = len(result)
	} else if r.crtLineSize+1+len(r.word) <= r.lineSize {
		result = append([]rune{' '}, r.word...)
		r.crtLineSize += len(result)
	} else {
		result = append([]rune{'\n'}, r.word...)
		r.crtLineSize = len(r.word)
	}
	r.word = nil
	return result
}

func (r *breakLines) ReadToken() ([]rune, error) {
	for {
		tok, err := r.r.ReadToken()
		if len(tok) == 0 {
			if len(r.word) == 0 {
				return nil, err
			}

			return r.emitWord(), err
		}

		rn := tok[0]
		if rn == '\n' {
			result := []rune{'\n'}
			if len(r.word) > 0 {
				result = append(r.emitWord(), '\n')
			}
			r.crtLineSize = 0
			return result, nil
		} else if unicode.IsSpace(rn) {
			// len(r.word) > 0 because of no leading, trailing
			// or multiple consecutive whitespace
			return r.emitWord(), err
		} else {
			r.word = append(r.word, rn)
			continue
		}
	}
}

// Returns a new TokenReader which converts to upper case.
func NewUpperCase(r TokenReader) TokenReader {
	return &upperCase{r: r}
}

type upperCase struct {
	r TokenReader
}

func (r *upperCase) ReadToken() ([]rune, error) {
	tok, err := r.r.ReadToken()
	for i := range tok {
		tok[i] = unicode.ToUpper(tok[i])
	}
	return tok, err
}

// Returns a new TokenReader which converts to lower case.
func NewLowerCase(r TokenReader) TokenReader {
	return &lowerCase{r: r}
}

type lowerCase struct {
	r TokenReader
}

func (r *lowerCase) ReadToken() ([]rune, error) {
	tok, err := r.r.ReadToken()
	for i := range tok {
		tok[i] = unicode.ToLower(tok[i])
	}
	return tok, err
}

// Returns a new TokenReader which consumes all input and produces a single
// token, showing the number of tokens in the input.
func NewTokenCounter(r TokenReader) TokenReader {
	return &tokenCounter{r: r}
}

type tokenCounter struct {
	r   TokenReader
	err error
}

func (r *tokenCounter) ReadToken() ([]rune, error) {
	if r.err != nil {
		return nil, r.err
	}
	var n uint64
	for r.err == nil {
		var tok []rune
		tok, r.err = r.r.ReadToken()
		if len(tok) > 0 {
			n++
		}
	}
	return []rune(fmt.Sprintf("%v", n)), r.err
}

// Returns a new TokenReader which outputs one token per word and nothing else.
func NewWordToken(r TokenReader) TokenReader {
	return &wordToken{r: NewFixedTokenSize(r, 1)}
}

type wordToken struct {
	r TokenReader
}

func (r *wordToken) ReadToken() ([]rune, error) {
	var word []rune
	for {
		tok, err := r.r.ReadToken()
		if len(tok) == 0 {
			return word, err
		}
		rn := tok[0]
		if unicode.IsLetter(rn) {
			word = append(word, rn)
		} else {
			if len(word) > 0 {
				return word, nil
			}
		}
	}
}

// Returns a new TokenReader which outputs its input tokens and the separator
// token sep between them. If sep has length zero it is replaced with
// []rune{' '}. The TokenReader interface does not permit returning
// zero-length tokens.
func NewJoinTokens(r TokenReader, sep []rune) TokenReader {
	if len(sep) == 0 {
		sep = []rune{' '}
	}
	return &joinTokens{r: r, sep: sep, first: true}
}

type joinTokens struct {
	r     TokenReader
	sep   []rune
	first bool
	buf   []rune
}

func (r *joinTokens) ReadToken() ([]rune, error) {
	if r.first {
		tok, err := r.r.ReadToken()
		if len(tok) == 0 {
			return nil, err
		}
		r.first = false
		return tok, nil
	}

	if len(r.buf) > 0 {
		result := r.buf
		r.buf = nil
		return result, nil
	}
	var err error
	r.buf, err = r.r.ReadToken()
	if len(r.buf) > 0 {
		return r.sep, nil
	}
	return nil, err
}

// Returns a new TokenReader that emits the pref token before all the tokens
// from the underlying TokenReader. If len(pref) = 0, pref is set to
// []rune{' '}.
func NewPrefixToken(r TokenReader, pref []rune) TokenReader {
	if len(pref) == 0 {
		pref = []rune{' '}
	}
	return &prefixToken{r: r, pref: pref}
}

type prefixToken struct {
	r    TokenReader
	pref []rune
}

func (r *prefixToken) ReadToken() ([]rune, error) {
	if r.pref != nil {
		defer func() {
			r.pref = nil
		}()
		return r.pref, nil
	}
	return r.r.ReadToken()
}

// Returns a new TokenReader that emits the suf tokes after all the tokens
// from the underlying TokenReader. If len(suf) = 0, suf is set to []rune{' '}.
func NewSuffixToken(r TokenReader, suf []rune) TokenReader {
	if len(suf) == 0 {
		suf = []rune{' '}
	}
	return &suffixToken{r: r, suf: suf}
}

type suffixToken struct {
	r   TokenReader
	suf []rune
}

func (r *suffixToken) ReadToken() ([]rune, error) {
	tok, err := r.r.ReadToken()
	if len(tok) == 0 {
		tok = r.suf
		r.suf = nil
		return tok, err
	}
	return tok, nil
}

// Returns a new TokenReader that records occurrences of groupSize consecutive
// tokens and outputs statistics. If groupSize < 1 it is set to 1.
// Lists the top toShow token groups. If toShow < 0, shows all groups.
func NewTokenGroupFrequency(r TokenReader, groupSize, toShow int) TokenReader {
	if groupSize < 1 {
		groupSize = 1
	}

	return &tokenGroupFrequency{
		r:         r,
		groupSize: groupSize,
		toShow:    toShow,
	}
}

type tokenGroupFrequency struct {
	r          TokenReader
	groupSize  int
	toShow     int
	totalCount uint64
	groupsLeft []freqGrpScore
}

func (*tokenGroupFrequency) encode(toks [][]rune) string {
	result := ""
	for _, tokRn := range toks {
		tokStr := string(tokRn)
		result += strconv.Itoa(len(tokStr))
		result += "_"
		result += tokStr
	}
	return result
}

func (*tokenGroupFrequency) decode(crypt string) [][]rune {
	result := [][]rune{}
	for len(crypt) > 0 {
		i := 0
		for crypt[i] != '_' {
			i++
		}
		lenBytes, _ := strconv.Atoi(crypt[:i])
		crypt = crypt[i+1:]
		result = append(result, []rune(crypt[:lenBytes]))
		crypt = crypt[lenBytes:]
	}
	return result
}

type freqGrpScore struct {
	toks  [][]rune
	count uint64
}
type byFreqGrpScore []freqGrpScore

func (a byFreqGrpScore) Len() int           { return len(a) }
func (a byFreqGrpScore) Swap(i, j int)      { a[i], a[j] = a[j], a[i] }
func (a byFreqGrpScore) Less(i, j int) bool { return a[i].count < a[j].count }

func (r *tokenGroupFrequency) ReadToken() ([]rune, error) {
	if r.groupsLeft == nil {
		countMap := make(map[string]uint64)
		group := [][]rune{}
		for {
			tok, _ := r.r.ReadToken()
			if len(tok) == 0 {
				break
			}
			group = append(group, tok)
			if len(group) == r.groupSize {
				countMap[r.encode(group)]++
				group = group[1:]
			}
		}

		// ensure this is non-nil even if there are no groups
		r.groupsLeft = []freqGrpScore{}
		for k, v := range countMap {
			r.groupsLeft = append(r.groupsLeft, freqGrpScore{
				toks:  r.decode(k),
				count: v,
			})
			r.totalCount += v
		}
		sort.Sort(sort.Reverse(byFreqGrpScore(r.groupsLeft)))

		result := fmt.Sprintf("%v items, %v unique",
			r.totalCount, len(r.groupsLeft))
		toShow := r.toShow
		if toShow >= 0 {
			result += fmt.Sprintf(", showing top %v", toShow)
		}
		if toShow < 0 || toShow > len(r.groupsLeft) {
			toShow = len(r.groupsLeft)
		}
		result += "\n"
		r.groupsLeft = r.groupsLeft[:toShow]
		return []rune(result), nil
	}

	if len(r.groupsLeft) > 0 {
		g := r.groupsLeft[0]
		r.groupsLeft = r.groupsLeft[1:]
		toksStr := []string{}
		for _, tokR := range g.toks {
			repr := fmt.Sprintf("%#v", string(tokR))
			toksStr = append(toksStr, repr)
		}
		result := fmt.Sprintf("%v\t%v\t%.6f\n",
			strings.Join(toksStr, ", "), g.count,
			float64(g.count)/float64(r.totalCount))
		return []rune(result), nil
	}
	return r.r.ReadToken()
}

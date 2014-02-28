#! /usr/bin/env python3

# Copyright © Mihai Borobocea 2012
#
# This file is part of ‘book-pub’.
#
# ‘book-pub’ is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# ‘book-pub’ is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with ‘book-pub’.  If not, see <http://www.gnu.org/licenses/>.

import argparse
from collections import OrderedDict
import json
import os, os.path
import shutil
from string import Template
import sys
import textwrap


class Error(Exception):
    pass


program_dir = os.path.dirname(os.path.realpath(sys.argv[0]))

sample_title_info = OrderedDict((
    ('title', 'Iliad'),
    ('author', 'Homer'),
    ('tagline', '800 BC'),
    ('publisher', 'Homer'),
    ('publish_date', 'YYYY-MM-DD'),
    ('uuid', 'generate at http://www.famkruithof.net/uuid/uuidgen'),
    ('ignore', ['notes', '.git', '.gitignore']),
    ))


def parse_args():
    'Parse args or exit'

    parser = argparse.ArgumentParser(
            description='''
            Format a book for publishing.
            Replaces epub-gen/ and pdf-gen/
            in the current directory with the generated output.
            See below for the required input format.
            All content must be UTF-8.
            ''',
            epilog='''
            Supported markup in book source:
            <em>..</em>, &hellip;, &mdash;
            ''')

    parser.add_argument('srcdir', help='''
            Directory containing the book source in the format below.
            A file named ‘cover.jpg’.
            A file named ‘book-pub’ containing a JSON dictionary
            with the following format: ''' + json.dumps(sample_title_info) +
            '''. Files in the "ignore" list are skipped.
            All remaining files, in alphabetical order, are chapters
            where the first line is the title
            and each subsequent line is one paragraph.
            ''')

    parser.add_argument('--documentclass', choices=('book', 'report'),
            default='book', help='TeX documentclass, default %(default)s')
    parser.add_argument('--book',
            action='store_const', dest='documentclass', const='book',
            help='set %(const)s %(dest)s')
    parser.add_argument('--report',
            action='store_const', dest='documentclass', const='report',
            help='set %(const)s %(dest)s')

    parser.add_argument('--doctype', choices=('html5', 'xhtml11'),
            default='xhtml11',
            help='''EPUB HTML doctype, default %(default)s.
            EpubCheck currently shows errors when using html5 doctype.''')
    parser.add_argument('--html5', action='store_const', dest='doctype',
            const='html5', help='set %(const)s %(dest)s')
    parser.add_argument('--xhtml11', action='store_const', dest='doctype',
            const='xhtml11', help='set %(const)s %(dest)s')

    return parser.parse_args()


def get_title_info(srcdir):
    '''Return dict with all keys of sample_title_info from the ‘book-pub’ file.

    Throws an exception if data is incomplete.
    '''

    bpub_file_path = os.path.join(srcdir, 'book-pub')
    with open(bpub_file_path, encoding='utf-8') as f:
        try:
            title_info = json.load(f)
        except ValueError as e:
            msg = 'while parsing ' + bpub_file_path + ': ' + str(e) + '\n' + \
                    'Expected structure: \n' + \
                    json.dumps(sample_title_info, indent=2)
            raise Error(msg)

        for k in sample_title_info:
            if k not in title_info:
                msg = 'Incomplete ‘book-pub’ file ' + bpub_file_path + '. ' + \
                        'Expected keys:\n' + \
                        json.dumps(sample_title_info, indent = 2) + \
                        '\nFound:\n' + \
                        json.dumps(title_info, indent=2)
                raise Error(msg)

        unused_keys = []
        for k in title_info:
            if k not in sample_title_info:
                unused_keys.append(k)
        if unused_keys:
            print('Warning: ignoring unknown keys', unused_keys, 'in file',
                    bpub_file_path, file=sys.stderr)

        return title_info


def get_chapter_paths(srcdir, title_info):
    '''Returns the file paths, sorted ascending by name.

    The search excludes ignored files, ‘book-pub’ and ‘cover.jpg’.
    Throws an exception if there are no chapters.
    '''

    ignore = {'book-pub', 'cover.jpg'}
    ignore.update(title_info['ignore'])

    paths = [os.path.join(srcdir, f) for f in os.listdir(srcdir)
            if f not in ignore]
    if not paths:
        raise Error('No chapters found')
    paths.sort()
    return paths


def get_chapter_title(f):
    '''Returns the chapter title (trimmed first line in file object f).

    If first line is empty after trimming, or there are no lines in the file,
    throws an exception.
    '''

    title = f.readline().strip()
    if not title:
        raise Error('Missing chapter title in file ' + f.name)

    return title


def delete_and_create_dir(path):
    '''Delete path if it exists and it's a directory, then create path dir.'''

    shutil.rmtree(path, ignore_errors=True)
    os.mkdir(path)


def writeTexStart(f, title_info, documentclass):
    '''Writes .tex file start to file object f'''

    # combine the info into a new dictionary for string formatting
    format_dict = dict(title_info)
    format_dict['documentclass'] = documentclass

    f.write(textwrap.dedent('''\
            \\documentclass[a5paper]{{{documentclass}}}

            \\usepackage{{fontspec}}
            \\usepackage{{xunicode}}
            \\usepackage{{xltxtra}}

            % required, else the page is a4 and the printed area is a5
            \\usepackage{{hyperref}}
            \\hypersetup{{colorlinks=true,%
                citecolor=black,%
                filecolor=black,%
                linkcolor=black,%
                urlcolor=black,%
                %
                pdftitle={{{title}}},%
                pdfauthor={{{author}}}%
            }}

            \\begin{{document}}

            \\title{{{title}}}
            \\author{{{author}}}
            \\date{{{tagline}}}

            \\maketitle

            \\tableofcontents
            '''.format(**format_dict)))


def writeTexEnd(f):
    '''Writes .tex file ending to file object f'''

    f.write('\\end{document}')


def escapeToTex(text):
    '''Escape source text for TeX output'''

    text = text.replace('<em>', '\\emph{')
    text = text.replace('</em>', '}')
    text = text.replace('&hellip;', '\\ldots{}')
    text = text.replace('&mdash;', '---')

    return text


def writeTexChapter(f, chap_path):
    '''Writes the source chapter in chap_path to file object f.'''

    with open(chap_path, encoding='utf-8') as chap_file:
        chap_title = get_chapter_title(chap_file)
        f.write(textwrap.dedent('''\


                \\chapter{{{title}}}
                '''.format(title=chap_title)))
        for line in chap_file:
            line = line.strip()
            f.write('\n')
            f.write(escapeToTex(line))
            f.write('\n')


def genTex(args):
    outdir = 'pdf-gen'
    delete_and_create_dir(outdir)
    title_info = get_title_info(args.srcdir)

    shutil.copy(os.path.join(program_dir, 'tex-resources', 'make'), outdir)

    with open(os.path.join(outdir, 'book.tex'), 'w', encoding='utf-8') as f:
        writeTexStart(f, title_info, args.documentclass)
        for chap_path in get_chapter_paths(args.srcdir, title_info):
            writeTexChapter(f, chap_path)
        writeTexEnd(f)


def escapeToEpub(line):
    '''Escape source text for Epub output'''

    return line


def writeEpubChapterTitle(chap_path, ch_num, title_info,
        f_toc_ncx, t_ncx_item, f_book_html, t_book_toc_item):
    '''Writes the chapter title from chap_path to f_toc_ncx and f_book_html.

    t_ncx_item and t_book_toc_item are templates.
    '''

    with open(chap_path, encoding='utf-8') as chap_file:
        chap_title = get_chapter_title(chap_file)

        d = dict(title_info)
        d['chapter_num'] = ch_num
        d['play_order'] = ch_num + 3
        d['chapter_title'] = chap_title

        f_toc_ncx.write(t_ncx_item.substitute(d))
        f_book_html.write(t_book_toc_item.substitute(d))


def writeEpubChapterContents(chap_path, ch_num, title_info,
        f_book_html, t_book_ch_item):
    '''Writes the chapter heading and contents from chap_path to f_book_html.

    t_book_ch_item is a template.
    '''

    with open(chap_path, encoding='utf-8') as chap_file:
        chap_title = get_chapter_title(chap_file)

        d = dict(title_info)
        d['chapter_num'] = ch_num
        d['chapter_title'] = chap_title

        f_book_html.write(t_book_ch_item.substitute(d))

        for line in chap_file:
            line = line.strip()
            f_book_html.write('    <p>')
            f_book_html.write(escapeToEpub(line))
            f_book_html.write('</p>\n')


def genEpub(args):
    outdir = 'epub-gen'
    delete_and_create_dir(outdir)
    title_info = get_title_info(args.srcdir)

    epub_src = os.path.join(program_dir, 'epub-resources')
    shutil.copy(os.path.join(epub_src, 'mimetype'), outdir)
    shutil.copy(os.path.join(epub_src, 'make'), outdir)

    src_metainf = os.path.join(epub_src, 'META-INF')
    out_metainf = os.path.join(outdir, 'META-INF')
    os.mkdir(out_metainf)
    shutil.copy(os.path.join(src_metainf, 'container.xml'), out_metainf)

    src_oebps = os.path.join(epub_src, 'OEBPS')
    out_oebps = os.path.join(outdir, 'OEBPS')
    os.mkdir(out_oebps)
    shutil.copy(os.path.join(src_oebps, 'style.css'), out_oebps)
    shutil.copy(os.path.join(args.srcdir, 'cover.jpg'), out_oebps)

    src_doctype = os.path.join(src_oebps, args.doctype)

    src_cover = os.path.join(src_oebps, 'cover.html')
    out_cover = os.path.join(out_oebps, 'cover.html')
    with open(out_cover, 'w', encoding='utf-8') as f_out_cover:
        with open(src_doctype, encoding='utf-8') as f_src_doctype:
            f_out_cover.write(f_src_doctype.read())
        with open(src_cover, encoding='utf-8') as f_src_cover:
            f_out_cover.write(f_src_cover.read())

    src_content_opf = os.path.join(src_oebps, 'content.opf')
    out_content_opf = os.path.join(out_oebps, 'content.opf')
    with open(src_content_opf, encoding='utf-8') as f_src:
        t = Template(f_src.read())
        with open(out_content_opf, 'w', encoding='utf-8') as f_dst:
            f_dst.write(t.substitute(title_info))

    out_toc_ncx = os.path.join(out_oebps, 'toc.ncx')
    out_book_html = os.path.join(out_oebps, 'book.html')
    with open(out_toc_ncx, 'w', encoding='utf-8') as f_toc_ncx, \
            open(out_book_html, 'w', encoding='utf-8') as f_book_html:
        # start toc.ncx and book.html
        src_ncx_start = os.path.join(src_oebps, 'toc.ncx-start')
        src_book_start = os.path.join(src_oebps, 'book.html-start')
        with open(src_ncx_start, encoding='utf-8') as f_ncx_start, \
                open(src_doctype, encoding='utf-8') as f_src_doctype, \
                open(src_book_start, encoding='utf-8') as f_book_start:
            t_ncx = Template(f_ncx_start.read())
            t_book = Template(f_book_start.read())
            f_toc_ncx.write(t_ncx.substitute(title_info))
            f_book_html.write(f_src_doctype.read())
            f_book_html.write(t_book.substitute(title_info))

        # add an item to toc.ncx and book.html for each chapter
        src_ncx_item = os.path.join(src_oebps, 'toc.ncx-item')
        src_book_toc_item = os.path.join(src_oebps, 'book.html-toc-item')
        src_book_ch_item = os.path.join(src_oebps, 'book.html-ch-item')
        with open(src_ncx_item, encoding='utf-8') as f_ncx_item, \
                open(src_book_toc_item, encoding='utf-8') as f_book_toc_item, \
                open(src_book_ch_item, encoding='utf-8') as f_book_ch_item:
            t_ncx_item = Template(f_ncx_item.read())
            t_book_toc_item = Template(f_book_toc_item.read())
            t_book_ch_item = Template(f_book_ch_item.read())

        ch_num = 1
        for chap_path in get_chapter_paths(args.srcdir, title_info):
            writeEpubChapterTitle(chap_path, ch_num, title_info,
                    f_toc_ncx, t_ncx_item, f_book_html, t_book_toc_item)
            ch_num += 1

        ch_num = 1
        for chap_path in get_chapter_paths(args.srcdir, title_info):
            writeEpubChapterContents(chap_path, ch_num, title_info,
                    f_book_html, t_book_ch_item)
            ch_num += 1

        # end toc.ncx and book.html
        src_ncx_end = os.path.join(src_oebps, 'toc.ncx-end')
        src_book_end = os.path.join(src_oebps, 'book.html-end')
        with open(src_ncx_end, encoding='utf-8') as f_ncx_end, \
                open(src_book_end, encoding='utf-8') as f_book_end:
            f_toc_ncx.write(f_ncx_end.read())
            f_book_html.write(f_book_end.read())


if __name__ == '__main__':
    try:
        args = parse_args()
        for gen_func in (genTex, genEpub):
            gen_func(args)
    except (Error, IOError, OSError) as e:
        print('Error:', e)
        sys.exit(1)

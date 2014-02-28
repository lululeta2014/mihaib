from tkinter import *
from tkinter.filedialog import askopenfilename, asksaveasfilename
from tkinter.ttk import Combobox

from locale import getpreferredencoding
import encodings

from .encodings import getEncodings, getEncodingsAndAliases
from . import fileops

enc_set = sorted(getEncodings())
enc2aliases = getEncodingsAndAliases()
alias2name = encodings.aliases.aliases

def run_with_gui():
    root = Tk()
    root.title("PyEncConv")

    def browse_in():
        name = askopenfilename()
        if name:
            in_txt.delete(0, END)
            in_txt.insert(END, name)

    def browse_out():
        name = asksaveasfilename()
        if name:
            out_txt.delete(0, END)
            out_txt.insert(END, name)

    # input file
    f = Frame(root)
    f.pack(anchor=W, fill=X)
    Label(f, text="input file:").pack(side=LEFT)
    in_txt = Entry(f)
    in_txt.pack(fill=X, expand=True, side=LEFT)
    in_browse = Button(f, text="browse...", command=browse_in)
    in_browse.pack(side=LEFT)

    def load_file():
        try:
            txt_area.delete(1.0, END)
            in_enc = in_enc_cmb.get()
            in_alias_lbl.config(text=(' '.join(get_alt_names(in_enc))))

            with open(in_txt.get(), 'r', encoding=in_enc) as f:
                txt_area.insert(END, f.read())
        except IOError as e:
            txt_area.insert(END, str(e))
        except OSError as e:
            txt_area.insert(END, str(e))
        except LookupError as e:
            txt_area.insert(END, str(e))
        except Exception as e:
            txt_area.insert(END, str(e))

    # input encoding
    f = Frame(root)
    f.pack(anchor=W, fill=X)
    Label(f, text="input encoding:").pack(side=LEFT)
    in_enc_cmb = Combobox(f)
    in_enc_cmb.pack(side=LEFT)
    in_enc_cmb['values'] = enc_set
    in_enc_cmb.insert(END, getpreferredencoding())
    in_enc_cmb.bind('<Return>', lambda event:load_file())
    in_alias_lbl = Label(f)
    in_alias_lbl.pack(side=LEFT)

    Button(root, text="Load for preview", command=load_file).pack(anchor=W)

    def disable_output_file():
        out_txt.config(state=DISABLED)
        out_browse.config(state=DISABLED)

    def enable_output_file():
        out_txt.config(state=NORMAL)
        out_browse.config(state=NORMAL)

    # output file
    out_choice = IntVar(value=1)
    Radiobutton(root, text="overwrite original (after moving to .bak)",
            variable = out_choice, value=1,
            command=disable_output_file).pack(anchor=W)
    f = Frame(root)
    f.pack(anchor=W, fill=X)
    Radiobutton(f, text="output file:",
            variable = out_choice, value=2,
            command=enable_output_file).pack(side=LEFT)
    out_txt = Entry(f, state=DISABLED)
    out_txt.pack(fill=X, expand=True, side=LEFT)
    out_browse = Button(f, text="browse...", command=browse_out,
            state=DISABLED)
    out_browse.pack(side=LEFT)

    # output encoding
    f = Frame(root)
    f.pack(anchor=W, fill=X)
    Label(f, text="output encoding:").pack(side=LEFT)
    out_enc_cmb = Combobox(f)
    out_enc_cmb.pack(side=LEFT)
    out_enc_cmb['values'] = enc_set
    out_enc_cmb.insert(END, 'UTF-8')
    out_alias_lbl = Label(f)
    out_alias_lbl.pack(side=LEFT)

    def convert():
        try:
            txt_area.delete(1.0, END)
            in_enc = in_enc_cmb.get()
            in_alias_lbl.config(text=(' '.join(get_alt_names(in_enc))))
            out_enc = out_enc_cmb.get()
            out_alias_lbl.config(text=(' '.join(get_alt_names(out_enc))))

            src_file = in_txt.get()
            if out_choice.get() == 1:
                dest_file = None
            else:
                dest_file = out_txt.get()

            fileops.convert(src_file, in_enc, dest_file, out_enc)
        except IOError as e:
            txt_area.insert(END, str(e))
        except OSError as e:
            txt_area.insert(END, str(e))
        except LookupError as e:
            txt_area.insert(END, str(e))
        except Exception as e:
            txt_area.insert(END, str(e))

    Button(root, text='Convert', command=convert).pack(anchor=W)

    # Text Area
    f = Frame(root)
    f.pack(expand=True, fill=BOTH)

    txt_area = Text(f, height=8, width=10, wrap=NONE)

    v_scroll = Scrollbar(f, command=txt_area.yview)
    v_scroll.pack(side=RIGHT, fill=Y)
    h_scroll = Scrollbar(f, orient=HORIZONTAL, command=txt_area.xview)
    h_scroll.pack(side=BOTTOM, fill=X)

    txt_area.config(yscrollcommand=v_scroll.set, xscrollcommand=h_scroll.set)
    txt_area.pack(expand=True, fill=BOTH)

    root.update()
    root.pack_propagate(False)
    root.mainloop()

def get_alt_names(enc_name):
    key = enc_name.lower().replace('-', '_')
    if key in alias2name:
        enc_name = alias2name[key]
        key = enc_name

    if key in enc2aliases:
        return [key] + enc2aliases[key]
    else:
        return [enc_name]

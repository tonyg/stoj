ifneq ($(BYTECODE),)
OCAMLC = ocamlc
CMO = cmo
OCAMLRUN = ocamlrun
else
OCAMLC = ocamlopt
CMO = cmx
OCAMLRUN =
endif

TARGETS = stojrun visdump graphconc index.cgi
OBJECTS = \
	stojlang.$(CMO) \
	stojparse.cmi \
	stojparse.$(CMO) \
	stojlex.$(CMO) \
	stojout.$(CMO) \
	stoj.$(CMO)

all: $(TARGETS)
	[ -f index.cgi.py ] && (chmod a+x index.cgi.py)

index.cgi: index.cgi.py
	ln -s $< $@

graphconc: graphconc.ml
	$(OCAMLC) $< -o $@

%: %.ml $(OBJECTS)
	$(OCAMLC) stojlex.$(CMO) stojparse.$(CMO) stojout.$(CMO) stoj.$(CMO) $< -o $*

run: all
	$(OCAMLRUN) stojrun
clean:
	rm -f $(TARGETS) $(OBJECTS)
	rm -f stojlex.ml
	rm -f stojparse.mli stojparse.ml stojparse.output
	rm -f *.cmi *.cmo *.cmx *.o

.PRECIOUS: $(OBJECTS)

%.$(CMO): %.ml
	$(OCAMLC) -c $<

%.cmi: %.mli
	$(OCAMLC) -c $<

stojlex.ml: stojlex.mll stojparse.mli
	ocamllex $<

stojparse.ml stojparse.mli: stojparse.mly
	ocamlyacc -v $<

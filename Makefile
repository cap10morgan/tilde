SOURCES := $(shell find src)

target/tilde: $(SOURCES) bb.edn prelude
	mkdir -p $(@D)
	bb uberscript $@.uberscript -m tilde.main
	cat prelude $@.uberscript > $@
	chmod +x $@
	rm $@.uberscript

.PHONY: install
install: target/tilde
	cp $< ~/bin/

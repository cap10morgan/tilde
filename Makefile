SOURCES := $(shell find src)

tilde: $(SOURCES) bb.edn prelude
	bb uberscript tilde.uberscript -m tilde.main
	cat prelude tilde.uberscript > tilde
	chmod +x tilde
	rm tilde.uberscript

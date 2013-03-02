# js1k processing

SHIM=shim.html
SOURCE=source.html
OUT=out
EXTRACTED=$(OUT)/extracted.js
OPTIMIZED=$(OUT)/optimized.js
CRUSHED=$(OUT)/crushed.js
TESTHTML=$(OUT)/test.html

CLOSURECOMPILER=closure/compiler.jar

all: $(TESTHTML) stat

$(EXTRACTED): $(SOURCE)
	[ -d $(OUT) ] || mkdir $(OUT)
	sed -n '1,/start of submission/d;/end of submission/q;p' <$< >$@

$(OPTIMIZED): $(EXTRACTED)
	java -jar $(CLOSURECOMPILER) --compilation_level ADVANCED_OPTIMIZATIONS --externs externs.js --js $< --js_output_file $@

$(CRUSHED): $(OPTIMIZED)
	jscrush/jscrush.groovy <$< >$@

$(TESTHTML): $(CRUSHED)
	groovy -p -e 'line=="SCRIPT" ? new File("$(CRUSHED)").text : line' <$(SHIM) >$@

stat:
	@wc -c $(EXTRACTED) 
	@wc -c $(OPTIMIZED)
	@wc -c $(CRUSHED)

outdir:

clean: 
	rm -r $(OUT)

.PHONY: all clean stat



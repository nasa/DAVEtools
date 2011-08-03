# Creates the DAVEtools README pages
#
# 2011-07-29 Bruce Jackson, NASA Langley <bruce.jackson@nasa.gov>

PAGES       = README.html dave_demo.html

make: ${PAGES}

%.html : %.txt
	asciidoc -a toclevels=3 -a data-uri -a icons $<

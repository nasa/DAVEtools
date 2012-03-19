#
# dist.make - Makefile to create a distributable .zip file for DAVEtools
#
#  2010-10-04 Bruce Jackson, LaRC <bruce.jackson@nasa.gov>
#

DIRS = DAVE DAVE2SL ModelSweeper tests
FILES = DAVEtools.jar LICENSE.html README.txt build.xml overview.html

VERSTRING = 0.9.2

COMMIT_ID = $(shell git rev-parse --short HEAD)

DISTDIR = dist

DISTNAME = DAVE_tools_${VERSTRING}

DISTZIP  = ${DISTNAME}_${COMMIT_ID}.zip

TEMPDIR = ${DISTNAME}

make: dist/${DISTZIP}

dist/${DISTZIP}: 
	 ant build
	-rm -rf ${TEMPDIR}
	 mkdir ${TEMPDIR}
	 cp ${FILES} ${TEMPDIR}
	 cp -R ${DIRS} ${TEMPDIR}
	-rm ${TEMPDIR}/DAVE/.classpath
	-rm ${TEMPDIR}/DAVE/.gitignore
	-rm ${TEMPDIR}/DAVE/.project
	-rm -rf ${TEMPDIR}/DAVE/bin
	-rm ${TEMPDIR}/DAVE2SL/.classpath
	-rm ${TEMPDIR}/DAVE2SL/.gitignore
	-rm ${TEMPDIR}/DAVE2SL/.project
	-rm -rf ${TEMPDIR}/DAVE2SL/bin
	-rm ${TEMPDIR}/ModelSweeper/.classpath
	-rm ${TEMPDIR}/ModelSweeper/.gitignore
	-rm ${TEMPDIR}/ModelSweeper/.project
	-rm -rf ${TEMPDIR}/ModelSweeper/apple
	-rm -rf ${TEMPDIR}/ModelSweeper/bin
	 cd ${TEMPDIR}/tests/app-tests; make copy_models
	 rm ${TEMPDIR}/tests/app-tests/Makefile
	 cd ${TEMPDIR}/tests/app-tests; mv Makefile.dist Makefile
	-rm ${TEMPDIR}/tests/app-tests/dave/.gitignore
	 rm ${TEMPDIR}/tests/app-tests/dave/catalog.xml
	 rm ${TEMPDIR}/tests/app-tests/dave/Makefile
	 cd ${TEMPDIR}/tests/app-tests/dave; mv catalog.dist.xml catalog.xml
	 cd ${TEMPDIR}/tests/app-tests/dave; mv Makefile.dist Makefile
	-rm ${TEMPDIR}/tests/app-tests/dave2sl/.gitignore
	 rm ${TEMPDIR}/tests/app-tests/dave2sl/catalog.xml
	 rm ${TEMPDIR}/tests/app-tests/dave2sl/Makefile
	 cd ${TEMPDIR}/tests/app-tests/dave2sl; mv catalog.dist.xml catalog.xml
	 cd ${TEMPDIR}/tests/app-tests/dave2sl; mv Makefile.dist Makefile
	 cd ${TEMPDIR}; zip -r ${DISTZIP} *
	 mv ${TEMPDIR}/${DISTZIP} ${DISTDIR}

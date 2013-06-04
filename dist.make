#
# dist.make - Makefile to create a distributable .zip file for DAVEtools
#
#  2010-10-04 Bruce Jackson, LaRC <bruce.jackson@nasa.gov>
#

DIRS = DAVE DAVE2SL DAVE2OTIS DAVE2POST ModelSweeper tests
FILES = DAVEtools.jar LICENSE.html README/README.html CHANGELOG.txt build.xml overview.html

VERSTRING = 0.9.5b

COMMIT_ID = $(shell git rev-parse --short HEAD)

DISTDIR = .

DISTNAME = DAVEtools_${VERSTRING}

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
	-rm ${TEMPDIR}/DAVE2OTIS/.classpath
	-rm ${TEMPDIR}/DAVE2OTIS/.gitignore
	-rm ${TEMPDIR}/DAVE2OTIS/.project
	-rm -rf ${TEMPDIR}/DAVE2OTIS/bin
	-rm ${TEMPDIR}/DAVE2POST/.classpath
	-rm ${TEMPDIR}/DAVE2POST/.gitignore
	-rm ${TEMPDIR}/DAVE2POST/.project
	-rm -rf ${TEMPDIR}/DAVE2SL/bin
	-rm ${TEMPDIR}/ModelSweeper/.classpath
	-rm ${TEMPDIR}/ModelSweeper/.gitignore
	-rm ${TEMPDIR}/ModelSweeper/.project
	-rm -rf ${TEMPDIR}/ModelSweeper/apple
	-rm -rf ${TEMPDIR}/ModelSweeper/bin
	 cd ${TEMPDIR}/tests/app-tests; make copy_models
	 rm ${TEMPDIR}/tests/app-tests/Makefile
	 rm ${TEMPDIR}/tests/app-tests/test_models/.d
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
	 mv ${TEMPDIR}/${DISTZIP} ${DISTDIR}/

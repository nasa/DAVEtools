#
#   Makefile for tests/app_tests/dave2sl subdirectory
#   Note: This is the 'development' makefile for Bruce's machine
#   It is not part of the distribution, which is renamed
#   from Makefile.dist.
#

PROJECTS  = ${HOME}/Documents/Projects
DAVE_DIR  = ${PROJECTS}/DAVE
MODELS_DIR= ${DAVE_DIR}/Tech/Models
SCHEMA_DIR= ${DAVE_DIR}/Schema
EXAMPLES  = ${SCHEMA_DIR}/Examples
F16_DIR   = ${MODELS_DIR}/F-16
HL20_DIR  = ${PROJECTS}/HL-20/Sim_models/DAVE

VPATH = ${EXAMPLES}:${F16_DIR}:${HL20_DIR}

include Makefile.dist

${DTD} : ${SCHEMA_DIR}/${DTD}
	cp $< $@


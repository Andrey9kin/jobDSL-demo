# Init "module" command in bash
source ${MODULESHOME-/app/modules/0}/init/bash

# Load report generator settings
module add $PWD/modulefiles/local

# test it
./test_all.sh

echo "TEST=TEST1" > prop.env
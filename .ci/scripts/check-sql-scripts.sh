#!/bin/bash

errors=0

for create_script in engine/src/main/resources/org/camunda/bpm/engine/db/create/*.sql; do
    drop_script=${create_script//create/drop}
    created_indexes=$(grep -i "^\s*create \(unique \)\?index" $create_script | tr [A-Z] [a-z] | sed 's/^\s*create \(unique \)\?index \(\S\+\).*$/\2/' | sort)
    dropped_indexes=$(grep -i "^\s*drop index" $drop_script | tr [A-Z] [a-z] | sed 's/^\s*drop index \([^.]\+\.\)\?\([^ ;]\+\).*$/\2/' | sort)
    diff_indexes=$(diff <(echo "$created_indexes") <(echo "$dropped_indexes"))
    if [ $? -ne 0 ]; then
        echo "Found index difference for:"
        echo $create_script
        echo $drop_script
        echo -e "${diff_indexes}\n"
        errors=$[errors + 1]
    fi
done

exit $errors

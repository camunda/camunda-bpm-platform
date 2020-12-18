export $(xargs < env.properties)
test -n "${PGDATABASE}" && psql postgres -c "DROP DATABASE IF EXISTS \"${PGDATABASE}\""

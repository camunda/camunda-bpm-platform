PGHOST=${AURORA_POSTGRES_ENDPOINT%%:*}
PGPORT=${AURORA_POSTGRES_ENDPOINT##*:}
echo "PGHOST=${PGHOST}" >> env.properties
echo "PGPORT=${PGPORT}" >> env.properties
echo "PGUSER=${AURORA_POSTGRES_USR}" >> env.properties
echo "PGPASSWORD=${AURORA_POSTGRES_PSW}" >> env.properties
echo "PGDATABASE=$(echo ${BUILD_TAG} | tail -c 50)" >> env.properties
export $(xargs < env.properties)
psql postgres -c "DROP DATABASE IF EXISTS \"${PGDATABASE}\""
psql postgres -c "CREATE DATABASE \"${PGDATABASE}\""
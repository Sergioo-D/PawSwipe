#!/bin/bash

set -e

host="$1"
port="$2"
cmd="${@:3}" # Recoge todos los argumentos después del segundo argumento como el comando a ejecutar

DB_USER="${DB_USER:-bogdanadmin}"
DB_PASS="${DB_PASS:-tuContraseña}"
# Configura un timeout y el número de intentos

TIMEOUT=5
ATTEMPTS=50

echo "Esperando a que MySQL en '$host:$port' esté disponible..."

for ((i=1;i<=ATTEMPTS;i++)); do
    if mysql -h "$host" -P "$port" -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" > /dev/null 2>&1; then
        echo "MySQL está disponible después de $((i * TIMEOUT)) segundos - ejecutando comando."
        exec $cmd
        exit 0
    else
        echo "MySQL aún no está disponible después de $((i * TIMEOUT)) segundos..."
        sleep $TIMEOUT
    fi
done

echo "Se alcanzó el tiempo máximo de espera tras $((ATTEMPTS * TIMEOUT)) segundos para MySQL."
exit 1

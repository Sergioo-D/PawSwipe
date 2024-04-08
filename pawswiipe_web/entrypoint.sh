#!/bin/sh

# Salir si algún comando falla
set -e

# Esperar a que la base de datos esté lista
echo "Esperando a la base de datos..."
/app/wait-for-db.sh db 3306

# Aplicar migraciones de Django
echo "Aplicando migraciones..."
python manage.py migrate --noinput

# Iniciar el servidor de desarrollo de Django
echo "Iniciando el servidor de Django..."
exec python manage.py runserver 0.0.0.0:8000

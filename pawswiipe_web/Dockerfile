FROM python:3.12.2

# Establecer el directorio de trabajo en /app
WORKDIR /app

# Usar apt-get para instalar dependencias
# Se elimina el comentario al final de la línea para evitar errores
RUN apt-get update && apt-get install -y \
    default-libmysqlclient-dev \
    gcc \
    build-essential \
    netcat-openbsd \
    mariadb-client \
    && rm -rf /var/lib/apt/lists/*

# Copiar los archivos del proyecto al directorio de trabajo
COPY . .

# Instalar las dependencias del proyecto
RUN pip install --no-cache-dir -r requirements.txt

# Copiar el script de entrada y hacerlo ejecutable
RUN chmod +x /app/entrypoint.sh
RUN chmod +x /app/wait-for-db.sh

# Exponer el puerto 8000 para que la aplicación sea accesible
EXPOSE 8000

# Usar el script de entrada como punto de entrada
CMD ["/app/entrypoint.sh"]

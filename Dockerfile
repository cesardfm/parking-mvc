FROM ubuntu:22.04 as openalpr-builder

# Configurar para instalación no interactiva
ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=America/Bogota

# Instalar dependencias y configurar timezone
RUN apt-get update && \
    apt-get install -y tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    apt-get install -y \
    software-properties-common \
    build-essential \
    cmake \
    git \
    libopencv-dev \
    libtesseract-dev \
    libleptonica-dev \
    liblog4cplus-dev \
    libcurl4-openssl-dev \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Compilar e instalar OpenALPR
RUN cd /tmp && \
    git clone https://github.com/openalpr/openalpr.git && \
    cd openalpr/src && \
    mkdir build && cd build && \
    cmake -DCMAKE_INSTALL_PREFIX:PATH=/usr -DCMAKE_INSTALL_SYSCONFDIR:PATH=/etc .. && \
    make -j$(nproc) && \
    make install && \
    cd / && rm -rf /tmp/openalpr

# Imagen final
FROM eclipse-temurin:22-jdk

# Configurar timezone en la imagen final
ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=America/Bogota

# Instalar dependencias de runtime
RUN apt-get update && \
    apt-get install -y tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    apt-get install -y \
    libopencv-core406t64 \
    libopencv-imgproc406t64 \
    libopencv-highgui406t64 \
    libopencv-imgcodecs406t64 \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Crear directorios de configuración
RUN mkdir -p /config /data /var/log/parking

# Copiar OpenALPR desde el builder
COPY --from=openalpr-builder /usr/bin/alpr* /usr/bin/
COPY --from=openalpr-builder /usr/lib/libopenalpr* /usr/lib/
COPY --from=openalpr-builder /usr/share/openalpr /usr/share/openalpr
COPY --from=openalpr-builder /etc/openalpr /etc/openalpr

# COPIAR ARCHIVOS DE CONFIGURACIÓN (desde el host)
COPY src/main/java/com/g3/parking/config/database-config.json /config/database-config.json
COPY src/main/java/com/g3/parking/config/db_config /config/db_config

# Verificar instalación
RUN alpr --version || echo "OpenALPR instalado"

# Verificar que los archivos de configuración existen
RUN echo "=== Verificando archivos de configuración ===" && \
    ls -la /config/ && \
    echo "Contenido de db_config:" && \
    cat /config/db_config || echo "No se pudo leer db_config"

COPY target/*.jar app.jar

ENV LD_LIBRARY_PATH="/usr/lib:/usr/local/lib:$LD_LIBRARY_PATH"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
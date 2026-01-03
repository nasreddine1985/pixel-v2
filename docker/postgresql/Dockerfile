# PostgreSQL Docker Image for PIXEL-V2
# Based on official PostgreSQL 15 with PIXEL-V2 specific configuration

FROM postgres:15-alpine

# Set environment variables for PostgreSQL
ENV POSTGRES_DB=pixelv2
ENV POSTGRES_USER=pixelv2
ENV POSTGRES_PASSWORD=pixelv2_secure_password
ENV POSTGRES_INITDB_ARGS="--encoding=UTF8 --locale=C"

# Create directory for custom configuration
RUN mkdir -p /docker-entrypoint-initdb.d/

# Copy initialization scripts
COPY init_pixel_v2_schema.sql /docker-entrypoint-initdb.d/
COPY init_tib_audit_tec_schema.sql /docker-entrypoint-initdb.d/
COPY insert_ochsic_sample_data.sql /docker-entrypoint-initdb.d/
COPY postgresql.conf /etc/postgresql/postgresql.conf
COPY pg_hba.conf /etc/postgresql/pg_hba.conf

# Install additional PostgreSQL extensions
RUN apk add --no-cache postgresql-contrib

# Expose PostgreSQL port
EXPOSE 5432

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD pg_isready -h localhost -p 5432 -U ${POSTGRES_USER} -d ${POSTGRES_DB}

# Set custom PostgreSQL configuration
CMD ["postgres", "-c", "config_file=/etc/postgresql/postgresql.conf"]
FROM python:3.12-alpine AS builder

WORKDIR /app

COPY src/requirements.txt .
RUN pip install --user --no-cache-dir -r requirements.txt

FROM python:3.12-alpine

WORKDIR /app

COPY --from=builder /root/.local /root/.local

COPY src/ .

ENV PATH=/root/.local/bin:$PATH

EXPOSE 8086

RUN apk add --no-cache curl

HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8086/health || exit 1

CMD ["gunicorn", "--bind", "0.0.0.0:8086", "app:app"]

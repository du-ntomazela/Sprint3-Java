#!/usr/bin/env bash
set -euo pipefail

API_URL="${API_URL:-http://localhost:8080}"
ADMIN_LOGIN="${ADMIN_LOGIN:-admin@specradar.com}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:?defina a variável ADMIN_PASSWORD com a senha do usuário admin}"
ANALISTA_LOGIN="${ANALISTA_LOGIN:-analista@specradar.com}"
ANALISTA_PASSWORD="${ANALISTA_PASSWORD:?defina a variável ANALISTA_PASSWORD com a senha do usuário analista}"

echo "== SpecRadar :: simulação de ataque e carga contra ${API_URL} =="

echo
echo "--- 1) Força bruta em /login (6 tentativas com senha errada, espera 401 x5 e 429 na 6a) ---"
for i in $(seq 1 6); do
  status=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${API_URL}/login" \
    -H "Content-Type: application/json" \
    -d "{\"login\":\"${ADMIN_LOGIN}\",\"senha\":\"senha-incorreta\"}")
  echo "tentativa ${i}: HTTP ${status}"
done

echo
echo "--- 2) Acesso sem token a rota protegida (espera 401) ---"
curl -s -o /dev/null -w "HTTP %{http_code}\n" "${API_URL}/usuarios/me"

echo
echo "--- 3) Login válido como ANALISTA e acesso a rota exclusiva de ADMIN (espera 403) ---"
token_analista=$(curl -s -X POST "${API_URL}/login" \
  -H "Content-Type: application/json" \
  -d "{\"login\":\"${ANALISTA_LOGIN}\",\"senha\":\"${ANALISTA_PASSWORD}\"}" | grep -o '"tokenJWT":"[^"]*' | cut -d'"' -f4)

curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST "${API_URL}/atributos" \
  -H "Authorization: Bearer ${token_analista}" \
  -H "Content-Type: application/json" \
  -d '{"codigo":"teste_ataque","nome":"Teste","unidade":"un","categoria":"Teste"}'

echo
echo "--- 4) Carga normal autenticada como ADMIN (30 requisições a /atributos) ---"
token_admin=$(curl -s -X POST "${API_URL}/login" \
  -H "Content-Type: application/json" \
  -d "{\"login\":\"${ADMIN_LOGIN}\",\"senha\":\"${ADMIN_PASSWORD}\"}" | grep -o '"tokenJWT":"[^"]*' | cut -d'"' -f4)

for i in $(seq 1 30); do
  status=$(curl -s -o /dev/null -w "%{http_code}" "${API_URL}/atributos" -H "Authorization: Bearer ${token_admin}")
  echo "requisição ${i}: HTTP ${status}"
done

echo
echo "== Simulação concluída. Confira os dashboards do Grafana e os alertas do Prometheus. =="

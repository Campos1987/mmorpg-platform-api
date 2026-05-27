# 1. Copie o template
cp .env.example .env

# 2. Confirme que o banco e a rede já estão no ar
docker ps | grep mysql-l2_game
docker network ls | grep mmorpg-net

# 3. Build + subir a API
docker compose up -d --build

# 4. Acompanhar logs
docker compose logs -f api

# 5. Para parar
docker compose down

# 6. Para parar e remover dados do banco
docker compose down -v


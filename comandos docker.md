# 1. Copie o template
cp .env.example .env

# 2. Confirme que o banco e a rede já estão no ar
docker ps | grep mysql-l2_game
docker network ls | grep mmorpg-net

# 2.1 Conectar os bancos externos à rede mmorpg-net (caso tenham sido criados por outros projetos Docker Compose)
# Exemplo: Se o banco 'l2-game' foi criado em outra pasta, ele estará na rede 'game_mmorpg-net'.
# Para a API se comunicar com os bancos, conecte os containers à rede compartilhada 'mmorpg-net':
docker network connect mmorpg-net l2-game
docker network connect mmorpg-net l2-login
docker network connect mmorpg-net l2-web

# 3. Build + subir a API
docker compose up -d --build

# 4. Acompanhar logs
docker compose logs -f api

# 5. Para parar
docker compose down

# 6. Para parar e remover dados do banco
docker compose down -v

// Configuration du serveur Keycloak (voir infrastructure/docker/docker-compose.keycloak.yml).
export const KEYCLOAK_CONFIG = {
  url: 'http://localhost:8180',
  realm: 'skillswap',
  clientId: 'skillswap-web'
};

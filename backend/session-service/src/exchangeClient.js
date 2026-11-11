//  Client HTTP vers exchange-service
import { ApiError } from './erreurs.js';

export function creerClientEchanges(baseUrl = process.env.EXCHANGE_SERVICE_URL || 'http://localhost:8082') {
  return async function recupererEchange(exchangeId, user, token = null) {
    let reponse;
    try {
      // On prépare les headers
      const headers = {
        'X-User-Id': String(user.id),
        'X-User-Role': user.role,
        'Accept': 'application/json'
      };

      // Si un token JWT est fourni (ce qui est le cas via Angular), on l'ajoute
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      reponse = await fetch(`${baseUrl}/api/exchanges/${exchangeId}`, {
        headers: headers,
        signal: AbortSignal.timeout(4000),
      });
    } catch (err) {
      console.error("🔴 [exchangeClient] Erreur réseau (le service 8082 est-il allumé ?) :", err.message);
      return null;
    }

    // 🔴 LOGS POUR VOIR L'ERREUR EXACTE
    if (!reponse.ok) {
      const texteErreur = await reponse.text();
      console.error(`🔴 [exchangeClient] L'exchange-service a répondu ${reponse.status} :`, texteErreur);
    }

    if (reponse.status === 404) throw new ApiError(404, 'Échange introuvable');
    if (reponse.status === 403) throw new ApiError(403, "Vous ne participez pas à cet échange");
    if (!reponse.ok) return null;

    return reponse.json();
  };
}
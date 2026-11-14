// ============================================================
//  Fournisseur LLM : API Anthropic (Claude) si ANTHROPIC_API_KEY est défini, sinon aucun (mode local).
//  La clé n'est lue que depuis l'environnement : jamais dans le code ni dans git.
// ============================================================
export function creerFournisseur({
  apiKey = process.env.ANTHROPIC_API_KEY,
  model = process.env.AI_MODEL || 'claude-sonnet-5-5',
  baseUrl = process.env.ANTHROPIC_BASE_URL || 'https://api.anthropic.com',
  fetchImpl = fetch,
} = {}) {
  if (!apiKey) return null;
  return {
    name: 'anthropic',
    model,
    /** messages = [{role:'user'|'assistant', content}] ; retourne le texte de la réponse. */
    async complete(system, messages, { maxTokens = 700 } = {}) {
      const r = await fetchImpl(`${baseUrl}/v1/messages`, {
        method: 'POST',
        headers: { 'content-type': 'application/json', 'x-api-key': apiKey, 'anthropic-version': '2023-06-01' },
        body: JSON.stringify({ model, max_tokens: maxTokens, system, messages }),
        signal: AbortSignal.timeout(25000),
      });
      if (!r.ok) throw new Error(`LLM ${r.status}`);
      const j = await r.json();
      return (j.content || []).filter((b) => b.type === 'text').map((b) => b.text).join('').trim();
    },
  };
}

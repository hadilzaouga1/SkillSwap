/** Erreur "métier" : porte un code HTTP et un message affichable à l'utilisateur. */
export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

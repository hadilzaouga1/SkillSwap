export function maintenantPourChamp(): string {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
}

export function versFormatApi(valeurChamp: string): string {
  return valeurChamp.length === 16 ? `${valeurChamp}:00` : valeurChamp;
}

export function estDansLeFutur(valeurChamp: string): boolean {
  const date = new Date(valeurChamp);
  return !isNaN(date.getTime()) && date.getTime() > Date.now();
}

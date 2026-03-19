export function normalizeString(str: string): string {
  if (!str) return "";
  return str
    .toLowerCase()
    .trim()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "");
}

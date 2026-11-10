import { ClinicalScope } from "../data/interfaces/clinical-context.interface";
import { ToothFaceEnum } from "./enums/consultation-instance.enum";
import { ToothFaceMapper } from "../../odontogram/utils/tooth-face.mapper";

const WORDS: Record<string, string> = { UPPER: "superior", LOWER: "inferior", RIGHT: "derecho", LEFT: "izquierdo", TEMPORARY: "(temporal)" };
const words = (key: string) => key.split("_").map((w) => WORDS[w]).join(" ");

/** Human readable location, e.g. "16 · vestibular", "Maxilar superior", "Boca completa". */
export function scopeLabel(scope: ClinicalScope | null): string {
  if (!scope) return "";
  if (scope.fullMouth) return "Boca completa";
  if (scope.maxillary) return `Maxilar ${words(scope.maxillary)}`;
  if (scope.quadrant) return `Cuadrante ${words(scope.quadrant)}`;
  const teeth = (scope.tooth ?? []).map((t) => Number(t.slice(1)));
  if (!teeth.length) return "";
  const face = scope.toothFace ? ` · ${ToothFaceMapper.toOdontogramFace(teeth[0], scope.toothFace as unknown as ToothFaceEnum)}` : "";
  return teeth.join(", ") + face;
}

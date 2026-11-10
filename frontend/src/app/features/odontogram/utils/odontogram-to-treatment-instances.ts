import { OdontogramInterface } from "../data/interfaces/odontogram.interface";
import { ToothInterface } from "../data/interfaces/tooth.interface";
import { ToothEnum } from "../../patients/utils/enums/consultation-instance.enum";
import { Scope, TreatmentInstanceItem } from "../../patients/data/interfaces/consultation-instance.interface";
import { TreatmentEnum } from "./enums/treatment.enum";
import { ToothFaceEnum } from "./enums/tooth-face.enum";
import { ToothFaceMapper } from "./tooth-face.mapper";

const toApiTooth = (n: number) => `T${n}` as ToothEnum;

/** Teeth of each arch in anatomical order (midline crossing included). */
export const archs = (o: OdontogramInterface): number[][] =>
  [
    [o.upperTeethLeft, o.upperTeethRight],
    [o.lowerTeethLeft, o.lowerTeethRight],
    [o.temporaryUpperLeft, o.temporaryUpperRight],
    [o.temporaryLowerLeft, o.temporaryLowerRight],
  ].map((sides) => sides.flatMap((s) => (s ?? []).map((t) => t.number)));

/** Every tooth between start and end (inclusive) following the arch order. */
function bridgeTeeth(o: OdontogramInterface, start: number, end: number): number[] {
  const arch = archs(o).find((a) => a.includes(start) && a.includes(end));
  if (!arch) return [start, end];
  const [from, to] = [arch.indexOf(start), arch.indexOf(end)].sort((a, b) => a - b);
  return arch.slice(from, to + 1);
}

/**
 * Converts the diagnoses loaded in the odontogram into `treatmentInstance[]`.
 * Treatments already saved (`instanceId`) are skipped. One item per treatment; face treatments produce one item per face (a Scope admits a single `toothFace`).
 */
export function odontogramToTreatmentInstances(
  odontogram: OdontogramInterface,
  treatmentIdByName: Map<string, number>,
): TreatmentInstanceItem[] {
  const teeth: ToothInterface[] = Object.values(odontogram).flat();

  return teeth.flatMap((tooth) =>
    (tooth.treatments ?? []).filter((t) => !t.instanceId).flatMap((t) => {
      const base = { treatmentId: treatmentIdByName.get(t.name), treatmentCondition: t.treatmentType };
      const scopes: Scope[] =
        t.name === TreatmentEnum.PUENTE && t.bridgeStart && t.bridgeEnd
          ? [{ tooth: bridgeTeeth(odontogram, t.bridgeStart, t.bridgeEnd).map(toApiTooth) }]
          : t.faces?.length
            ? t.faces.map((f) => ({
                tooth: [toApiTooth(tooth.number)],
                toothFace: ToothFaceMapper.toApiFace(tooth.number, f as ToothFaceEnum),
              }))
            : [{ tooth: [toApiTooth(tooth.number)] }];
      return scopes.map((scope) => ({ ...base, scope }));
    }),
  );
}

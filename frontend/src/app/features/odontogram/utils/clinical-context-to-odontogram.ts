import { OdontogramInterface } from "../data/interfaces/odontogram.interface";
import { TreatmentInterfaceOld } from "../data/interfaces/treatment.interface";
import { TreatmentInstanceContext } from "../../patients/data/interfaces/clinical-context.interface";
import { ToothFaceEnum as ApiFace } from "../../patients/utils/enums/consultation-instance.enum";
import { TreatmentEnum } from "./enums/treatment.enum";
import { ToothFaceMapper } from "./tooth-face.mapper";
import { archs } from "./odontogram-to-treatment-instances";

const FACE_TREATMENTS = [TreatmentEnum.CARIES, TreatmentEnum.OBTURACION_COMPOSITE];
const ALL_FACES = Object.values(ApiFace);

/**
 * Returns a copy of the odontogram with the saved treatment instances painted on their teeth.
 * Only tooth-based scopes can be drawn; quadrant, maxillary and full-mouth ones are skipped.
 * `nameByLabel` resolves the catalog `name` (which drives the icons) from the instance label.
 */
export function applyTreatmentInstances(
  odontogram: OdontogramInterface,
  instances: TreatmentInstanceContext[],
  nameByLabel: Map<string, TreatmentEnum>,
): OdontogramInterface {
  const order = archs(odontogram).flat();
  const byTooth = new Map<number, TreatmentInterfaceOld[]>();
  const add = (tooth: number, t: TreatmentInterfaceOld) => byTooth.set(tooth, [...(byTooth.get(tooth) ?? []), t]);

  for (const i of instances) {
    const name = nameByLabel.get(i.label);
    const teeth = (i.scope.tooth ?? []).map((t) => Number(t.slice(1))).sort((a, b) => order.indexOf(a) - order.indexOf(b));
    if (!name || !teeth.length) continue;
    const base = { name, label: i.label, treatmentType: i.treatmentCondition, instanceId: i.id };

    if (name === TreatmentEnum.PUENTE) {
      add(teeth[0], { ...base, bridgeStart: teeth[0], bridgeEnd: teeth[teeth.length - 1] });
    } else if (FACE_TREATMENTS.includes(name)) {
      teeth.forEach((n) => add(n, { ...base, faces: (i.scope.toothFace ? [i.scope.toothFace] : ALL_FACES).map((f) => ToothFaceMapper.toOdontogramFace(n, f)) }));
    } else {
      teeth.forEach((n) => add(n, base));
    }
  }

  const paint = (section?: OdontogramInterface[keyof OdontogramInterface]) =>
    section?.map((t) => ({ ...t, treatments: [...(t.treatments ?? []), ...(byTooth.get(t.number) ?? [])] }));
  return Object.fromEntries(Object.entries(odontogram).map(([k, v]) => [k, paint(v)])) as unknown as OdontogramInterface;
}

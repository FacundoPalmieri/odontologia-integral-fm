import { MaxillaryZoneEnum, QuadrantEnum, StepStatusEnum, ToothEnum, ToothFaceEnum } from "../../utils/enums/consultation-instance.enum";
import { TreatmentConditionEnum } from "../../../odontogram/utils/enums/treatment.enum";

/** Response of GET /api/consultation-instance/patient/{patientId}: what the patient has open. */
export interface ClinicalContextResponse {
    treatmentInstances: TreatmentInstanceContext[];
    prestationInstances: PrestationInstanceContext[];
}

/** Location as returned by the backend: unset fields come as null. */
export interface ClinicalScope {
    tooth: ToothEnum[] | null;
    toothFace: ToothFaceEnum | null;
    quadrant: QuadrantEnum | null;
    maxillary: MaxillaryZoneEnum | null;
    fullMouth: true | null;
}

export interface DentistSpecialty {
    id: number;
    name: string;
}

export interface TreatmentInstanceContext {
    id: number;
    label: string;
    treatmentCondition: TreatmentConditionEnum.REQUIRED | TreatmentConditionEnum.IN_PROGRESS;
    scope: ClinicalScope;
    /** Empty = no restriction. */
    specialties: DentistSpecialty[];
}

export interface PrestationInstanceContext {
    id: number;
    label: string;
    status: "IN_PROGRESS";
    scope: ClinicalScope;
    treatmentInstanceId: number | null;
    originTreatmentInstanceId: number | null;
    originScopes: ClinicalScope[];
    /** Empty = no restriction. */
    specialties: DentistSpecialty[];
    /** Full catalog chain of the type, in order. */
    steps: StepContext[];
}

/** `id` = step instance (started); `stepId` = catalog step (pending). Never both. */
export interface StepContext {
    id: number | null;
    stepId: number | null;
    label: string;
    required: boolean;
    /** null = pending. */
    status: StepStatusEnum | null;
}

/** Item of GET /api/consultation/patient/{id} (history, newest first). */
export interface ConsultationHistoryItem {
    consultationId: number;
    consultationInstanceId: number | null;
    dateTime: string;
    dentistName: string;
    consultationStatus: string;
}

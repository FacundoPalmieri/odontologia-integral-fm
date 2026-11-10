import { DiscountTypeEnum, MaxillaryZoneEnum, PrestationInstanceStatusEnum, PrestationScopeEnum, QuadrantEnum, StepStatusEnum, ToothEnum, ToothFaceEnum } from "../../utils/enums/consultation-instance.enum";
import { TreatmentConditionEnum } from "../../../odontogram/utils/enums/treatment.enum";

/** Body of POST /api/consultation-instance. Response is ApiResponseInterface<number> (id of the created instance). */
export interface ConsultationInstanceRequest {
    consultationId: number;
    observation?: string;
    treatmentInstance?: TreatmentInstanceItem[];
    prestation?: PrestationInstanceItem[];
}

/** Location. Exactly one group: tooth (+ optional toothFace), quadrant, maxillary or fullMouth. */
export interface Scope {
    tooth?: ToothEnum[];
    toothFace?: ToothFaceEnum;
    quadrant?: QuadrantEnum;
    maxillary?: MaxillaryZoneEnum;
    fullMouth?: boolean;
}

/** New treatment: scope + treatmentId (+ tempId/condition). Existing: only id + treatmentCondition (REPLACED/CANCELED). */
export interface TreatmentInstanceItem {
    id?: number;
    tempId?: string;
    scope?: Scope;
    treatmentId?: number;
    treatmentCondition?: TreatmentConditionEnum;
}

/** Reference to a saved treatment (id) or to one created in the same request (tempId). Exactly one. */
export interface TreatmentInstanceRef {
    id?: number;
    tempId?: string;
}

/** New prestation: prestationTypeId + location + steps. Existing: only id + steps. */
export interface PrestationInstanceItem {
    id?: number;
    prestationTypeId?: number;
    treatmentInstanceRef?: TreatmentInstanceRef;
    scope?: Scope;
    originTreatmentInstanceRef?: TreatmentInstanceRef;
    promotionId?: number;
    discountType?: DiscountTypeEnum;
    discountValue?: number;
    steps: StepItem[];
}

/** Existing step: id + status. New step: stepId (catalog) + status. Never both ids. */
export interface StepItem {
    id?: number;
    stepId?: number;
    status: StepStatusEnum;
}

export interface ConsultationInstanceResponse {
    id: number;
    consultationResponseDTO: ConsultationResponse;
    odontogram: OdontogramResponse[];
    prestationInstace: PrestationInstanceResponse[];
    observation: string;
    totalFinalAmount: number;
}

export interface ConsultationResponse {
    id: number;
    appointmentId: number;
    dateTime: Date;
    patientId: number;
    patientName: string;
    dentistName: string;
    consultationStatus: string;
    webSocketStatus: string;
}

export interface OdontogramResponse {
    id: number;
    tooth: ToothEnum;
    toothFace: ToothFaceEnum;
    treatmentName: string;
    treatmentLabel: string;
    treatmentCondition: string;
}

export interface PrestationInstanceResponse {
    id: number;
    name: string;
    odontogram: OdontogramResponse;
    scope: PrestationScopeEnum;
    scopeDetail: string;
    status: PrestationInstanceStatusEnum;
    price: number;
    promotion: string;
    promotionAmount: number;
    discountType: DiscountTypeEnum;
    discountValue: number;
    discountAmount: number;
    finalAmount: number;
    pendingAmount: number;
}
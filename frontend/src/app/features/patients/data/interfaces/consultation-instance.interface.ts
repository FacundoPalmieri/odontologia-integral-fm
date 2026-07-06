import { DiscountTypeEnum, PrestationInstanceStatusEnum, PrestationScopeEnum, QuadrantEnum, ToothEnum, ToothFaceEnum } from "../../utils/enums/consultation-instance.enum";

export interface ConsultationInstanceRequest {
    consultationId: number;
    observation: string;
    odontogram: OdontogramRequest[];
    prestationNew: PrestationRequest[];
    stepAdvancements: StepAdvancementsRequest[];
}
export interface OdontogramRequest {
    tooth: ToothEnum;
    toothFace: ToothFaceEnum;
    treatmentId: number;
    treatmentConditionId: number;
}

export interface PrestationRequest {
    prestationTypeId: number;
    prestationStepId: number;
    prestationStepStatus: PrestationInstanceStatusEnum;
    odontogram: OdontogramRequest[];
    scope: PrestationScopeEnum;
    tooth: ToothEnum;
    quadrant: QuadrantEnum;
    maxillary: string; //????
    promtionId: number;
    discountType: DiscountTypeEnum;
    discountValue: number;
}

export interface StepAdvancementsRequest {
    prestationInstanceId: number;
    prestationStepId: number;
    status: PrestationInstanceStatusEnum;
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
import { inject } from "@angular/core";
import { HealthPlanInterface } from "../../../../shared/interfaces/person-data.interface";
import { PersonSerializer } from "../../../../shared/serializers/person.serializer";
import { PersonDataService } from "../../../../shared/services/person-data.service";
import { PatientCreateDto, PatientDto } from "../dtos/patient.dto";
import { PatientInterface } from "../interfaces/patient.interface";
import { MedicalHistoryRiskInterface } from "../interfaces/medical-history.interface";

export class PatientSerializer {
  private readonly personDataService = inject(PersonDataService);
  private personSerializer = new PersonSerializer();

  toCreateDto(patient: PatientInterface): PatientCreateDto {
    return {
      person: this.personSerializer.toCreateDto(patient.person),
      healthPlanId: patient.healthPlan?.id,
      affiliateNumber: patient.affiliateNumber,
      medicalRisk: patient.medicalRisks?.map((mr) => {
        return { medicalRiskId: mr.id, observation: mr.observation || "" };
      }),
    };
  }

  toView(patient: PatientDto): PatientInterface {
    return {
      person: this.personSerializer.toView(patient.person),
      affiliateNumber: patient.affiliateNumber,
      healthPlan: this._getHealthPlan(patient.healthPlans),
      medicalRisks: patient.medicalHistoryRisk as MedicalHistoryRiskInterface[],
    } as PatientInterface;
  }

  _getHealthPlan(healthPlan: string): HealthPlanInterface {
    return this.personDataService
      .healthPlans()
      .find((hp) => hp.name == healthPlan) as HealthPlanInterface;
  }
}

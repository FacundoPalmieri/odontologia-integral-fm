import { inject } from "@angular/core";
import {
  PatientCreateDtoInterface,
  PatientDtoInterface,
} from "../dto/patient.dto";
import {
  MedicalHistoryRiskInterface,
  PatientInterface,
} from "../interfaces/patient.interface";
import { HealthPlanInterface } from "../interfaces/person-data.interface";
import { PersonSerializer } from "./person.serializer";
import { PersonDataService } from "../../services/person-data.service";

export class PatientSerializer {
  private readonly personDataService = inject(PersonDataService);
  private personSerializer = new PersonSerializer();

  toCreateDto(patient: PatientInterface): PatientCreateDtoInterface {
    return {
      person: this.personSerializer.toCreateDto(patient.person),
      healthPlanId: patient.healthPlan?.id,
      affiliateNumber: patient.affiliateNumber,
      medicalRisk: patient.medicalRisks?.map((mr) => {
        return { medicalRiskId: mr.id, observation: mr.observation || "" };
      }),
    };
  }

  toView(patient: PatientDtoInterface): PatientInterface {
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

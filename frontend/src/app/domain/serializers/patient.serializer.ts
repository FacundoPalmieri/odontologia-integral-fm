import { PatientCreateDtoInterface } from "../dto/patient.dto";
import { PatientInterface } from "../interfaces/patient.interface";
import { PersonSerializer } from "./person.serializer";

export class PatientSerializer {
  static toCreateDto(patient: PatientInterface): PatientCreateDtoInterface {
    return {
      personDto: PersonSerializer.toCreateDto({
        firstName: patient.firstName,
        lastName: patient.lastName,
        dniType: patient.dniType,
        dni: patient.dni,
        birthDate: patient.birthDate,
        gender: patient.gender,
        nationality: patient.nationality,
        country: patient.country,
        province: patient.province,
        locality: patient.locality,
        street: patient.street,
        number: patient.number,
        floor: patient.floor,
        apartment: patient.apartment,
        contactEmails: patient.contactEmails,
        phoneType: patient.phoneType,
        phone: patient.phone,
      }),
      healthPlanId: patient.healthPlan.id,
      affiliateNumber: patient.affiliateNumber,
      medicalRiskDto: [],
    };
  }
}

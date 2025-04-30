package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.MedicalRiskResponseDTO;
import com.odontologiaintegralfm.dto.PatientCreateRequestDTO;
import com.odontologiaintegralfm.dto.PatientCreateResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.*;
import com.odontologiaintegralfm.repository.IPatientRepository;
import com.odontologiaintegralfm.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author [Facundo Palmieri]
 */

@Service
public class PatientService implements IPatientService {
    @Autowired
    private IPatientRepository patientRepository;

    @Autowired
    private IDniTypeService dniTypeService;

    @Autowired
    private IGenderService genderService;

    @Autowired
    private INationalityService nationalityService;

    @Autowired
    private IGeoService geoService;

    @Autowired
    private IHealthPlanService healthPlanService;

    @Autowired
    private IContactEmailService contactEmailService;

    @Autowired
    private ITelephoneTypeService telephoneTypeService;

    @Autowired
    private IContactPhoneService contactPhoneService;

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IMedicalRiskService medicalRiskService;







    /**
     * Método para crear un paciente
     *
     * @param patientCreateRequestDTO El objeto paciente a crear
     * @return Una respuesta que contiene el objeto {@link PatientCreateResponseDTO } del paciente recién creado
     */

    @Override
    @Transactional
    public Response<PatientCreateResponseDTO> save(PatientCreateRequestDTO patientCreateRequestDTO) {
        try{

            //Validar que el paciente no exista.
            validatePatient(patientCreateRequestDTO.dni());

            //Crear objeto dirección.
            Address address = new Address();
            address.setStreet(patientCreateRequestDTO.street());
            address.setNumber(patientCreateRequestDTO.number());
            address.setFloor(patientCreateRequestDTO.floor());
            address.setApartment(patientCreateRequestDTO.apartment());
            address.setLocality(geoService.getLocalityById(patientCreateRequestDTO.localityId()));
            address.setEnabled(true);
            addressService.save(address);




            // Crear objeto Paciente.
            Patient patient = new Patient();
            patient.setFirstName(patientCreateRequestDTO.firstName());
            patient.setLastName(patientCreateRequestDTO.lastName());
            patient.setDniType(dniTypeService.getById(patientCreateRequestDTO.dniTypeId()));
            patient.setDni(patientCreateRequestDTO.dni());
            patient.setBirthDate(patientCreateRequestDTO.birthDate());
            patient.setGender(genderService.getById(patientCreateRequestDTO.genderId()));
            patient.setNationality(nationalityService.getById(patientCreateRequestDTO.nationalityId()));
            patient.setHealthPlan(healthPlanService.getById(patientCreateRequestDTO.healthPlansId()));
            patient.setAffiliateNumber(patientCreateRequestDTO.affiliateNumber());
            patient.setAddress(address);
            patient.setCreatedAt(LocalDate.now());
            patient.setEnabled(true);

            //Guardar Paciente
            patientRepository.save(patient);

            //Crear objeto Historia Clínica.
            MedicalHistory medicalHistory = new MedicalHistory();
            medicalHistory.setPatient(patient);
            medicalHistory.setDateTime(LocalDate.now());
            medicalHistory.setMedicalRisks(medicalRiskService.getByIds(patientCreateRequestDTO.medicalRiskId()));
            medicalHistory.setObservation(patientCreateRequestDTO.medicalHistoryObservation());
            medicalHistory.setEnabled(true);

            Set<MedicalRiskResponseDTO> riskDTOs = medicalHistory.getMedicalRisks().stream()
                    .map(risk -> new MedicalRiskResponseDTO(risk.getId(), risk.getName()))
                    .collect(Collectors.toSet());


            //Crear objeto Contacto Email
            ContactEmail contactEmail = new ContactEmail();
            contactEmail.setPersons(new HashSet<>());

            contactEmail.setEmail(patientCreateRequestDTO.email());
            contactEmail.getPersons().add(patient);
            contactEmail.setEnabled(true);
            contactEmailService.save(contactEmail);

            //Crear objeto Contacto Teléfono.
            ContactPhone contactPhone = new ContactPhone();
            contactPhone.setPerson(new HashSet<>());

            contactPhone.setNumber(patientCreateRequestDTO.phone());
            contactPhone.setTelephoneType(telephoneTypeService.getById(patientCreateRequestDTO.phoneType()));
            contactPhone.setNumber(patientCreateRequestDTO.phone());
            contactPhone.getPerson().add(patient);
            contactPhone.setEnabled(true);
            contactPhoneService.save(contactPhone);


            //Crear Objeto Respuesta
            PatientCreateResponseDTO patientCreateResponseDTO = new PatientCreateResponseDTO(
                    patient.getId(),
                    patient.getFirstName(),
                    patient.getLastName(),
                    patient.getDniType().getName(),
                    patient.getDni(),
                    patient.getBirthDate(),
                    Period.between(patient.getBirthDate(), LocalDate.now()).getYears(),
                    patient.getGender().getName(),
                    patient.getNationality().getName(),
                    address.getLocality().getName(),
                    address.getStreet(),
                    address.getNumber(),
                    address.getFloor(),
                    address.getApartment(),
                    patient.getHealthPlan().getName(),
                    patient.getAffiliateNumber(),
                    contactEmail.getEmail(),
                    contactPhone.getNumber(),
                    riskDTOs,
                    medicalHistory.getObservation()
            );

            return new Response<>(true, null, patientCreateResponseDTO);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", null,patientCreateRequestDTO.dni(), "save");
        }
    }



    private void validatePatient (String dni){
        try{
            patientRepository.findByDni(dni).orElseThrow(() -> new NotFoundException(null,"exception.patientNotFound.user",null,"exception.patientNotFound.log",null,dni,"PatientService", "validatePatient", LogLevel.WARN));

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", null,dni, "validatePatient");
        }
    }




}

package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.PatientCreateRequestDTO;
import com.odontologiaintegralfm.dto.PatientCreateResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.ConflictException;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.*;
import com.odontologiaintegralfm.repository.IPatientRepository;
import com.odontologiaintegralfm.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;


/**
 * @author [Facundo Palmieri]
 */

@Service
public class PatientService implements IPatientService {
    @Autowired
    private IMessageService messageService;

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
    private IPhoneTypeService telephoneTypeService;

    @Autowired
    private IContactPhoneService contactPhoneService;

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IMedicalRiskService medicalRiskService;

    @Autowired
    private IMedicalHistoryService medicalHistoryService;

    @Autowired
    private IMedicalHistoryObservationService medicalHistoryObservationService;

    @Autowired
    private UserService userService;


    /**
     * Crea un nuevo paciente en el sistema junto con su domicilio, contactos, historia clínica
     * y devuelve una respuesta con todos los datos relevantes.
     *
     * <p>Este método sigue los siguientes pasos:
     * <ol>
     *     <li>Valida que no exista un paciente con el mismo DNI.</li>
     *     <li>Crea o habilita un domicilio según corresponda.</li>
     *     <li>Crea un nuevo objeto {@link Patient} y lo persiste.</li>
     *     <li>Genera una nueva historia clínica asociada al paciente.</li>
     *     <li>Registra los contactos del paciente (email y teléfono).</li>
     *     <li>Construye un DTO con toda la información creada.</li>
     * </ol>
     *
     * @param patientCreateRequestDTO DTO con los datos necesarios para crear el paciente.
     * @return {@link Response} que contiene un {@link PatientCreateResponseDTO} con los datos del paciente creado.
     * @throws DataBaseException si ocurre un error durante el acceso a la base de datos.
     */
    @Override
    @Transactional
    public Response<PatientCreateResponseDTO> create(PatientCreateRequestDTO patientCreateRequestDTO) {
        try{

            //Validar que el paciente no exista.
            validateNonExistentPatient(patientCreateRequestDTO.dni());

            //Crear objeto dirección.
            Address address = createAddress(patientCreateRequestDTO);

            // Crear objeto Paciente.
            Patient patient = createPatient(patientCreateRequestDTO, address);

            //Crear objeto Historia Clínica.
            MedicalHistory medicalHistory = createMedicalHistory(patientCreateRequestDTO,patient);

            //Crear Objeto Historia Clínica Observación.
            MedicalHistoryObservation medicalHistoryObservation = createMedicalHistoryObservation(patientCreateRequestDTO, medicalHistory);

            //Crear objeto Contacto Email
            ContactEmail contactEmail = createContactEmail(patientCreateRequestDTO,patient);

            //Crear objeto Contacto Teléfono.
            ContactPhone contactPhone = createContactPhone(patientCreateRequestDTO,patient);

            //Crear Objeto Respuesta
            PatientCreateResponseDTO patientCreateResponseDTO = createResponseDTO(patient,address,contactEmail,contactPhone,medicalHistory, medicalHistoryObservation);

            //Crear mensaje para el usuario.
            String messageUser = messageService.getMessage("patientService.save.ok.user",null, LocaleContextHolder.getLocale());


            return new Response<>(true,messageUser, patientCreateResponseDTO);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", null,patientCreateRequestDTO.dni(), "save");
        }
    }


    /**
     * Método privado para validar si exista el paciente.
     * Si existe arroja la exception {@link ConflictException } con el mensaje "exception.patientExists"
     * Si no existe no se realiza ninguna acción.
     * @param dni del paciente a buscar.
     */
    private void validateNonExistentPatient(String dni){
        try{
           Optional<Patient> patient = patientRepository.findByDni(dni);
            if(patient.isPresent()){
                throw new ConflictException("exception.patientExists.user",new Object[]{patient.get().getLastName() + patient.get().getFirstName()},"exception.patientExists.log",new Object[]{patient.get().getId(), patient.get().getLastName() + ", " +  patient.get().getFirstName(),"PatientService", "save"}, LogLevel.ERROR);
            }

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", null,dni, "validateNonExistentPatient");
        }
    }

    /**
     * Método protegido para crear el paciente.
     * @param patientCreateRequestDTO con los datos del paciente.
     * @param address con los datos del domicilio del paciente.
     * @return Patient con los datos creados.
     */
    @Transactional
    protected Patient createPatient(PatientCreateRequestDTO patientCreateRequestDTO, Address address){
        try {

            //Obtiene Usuario autenticado.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserSec user = userService.getByUsername(authentication.getName());

            //Crea paciente.
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
            patient.setCreatedAt(LocalDateTime.now());
            patient.setCreatedBy(user);
            patient.setEnabled(true);

            return patientRepository.save(patient);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", null,patientCreateRequestDTO.dni(), "createPatient");
        }
    }


    /**
     Método privado que construye un objeto {@link Address} y llama al servicio correspondiente para su creación.
     * @param patientCreateRequestDTO Objeto Paciente con datos del domicilio.
     * @return Address con el objeto creado o habilitado según el caso.
     */
    private Address createAddress (PatientCreateRequestDTO patientCreateRequestDTO){
        Address address = new Address();
        address.setStreet(patientCreateRequestDTO.street());
        address.setNumber(patientCreateRequestDTO.number());
        address.setFloor(patientCreateRequestDTO.floor());
        address.setApartment(patientCreateRequestDTO.apartment());
        address.setLocality(geoService.getLocalityById(patientCreateRequestDTO.localityId()));
        address = addressService.enableOrCreate(address);
        return address;
    }


    /**
     * Método privado que construye un objeto {@link MedicalHistory} y llama al servicio correspondiente para su creación.
     * @param patientCreateRequestDTO con los datos de la historia clínica del paciente.
     * @param patient con los datos personales del paciente (incluye ID)
     * @return MedicalHistory con la historia clínica creada.
     */
    private MedicalHistory createMedicalHistory (PatientCreateRequestDTO patientCreateRequestDTO, Patient patient){
        MedicalHistory medicalHistory = new MedicalHistory();
        medicalHistory.setPatient(patient);
        medicalHistory.setDateTime(LocalDate.now());
        medicalHistory.setMedicalRisks(medicalRiskService.getByIds(patientCreateRequestDTO.medicalRiskId()));
        medicalHistory.setEnabled(true);
        medicalHistory = medicalHistoryService.create(medicalHistory);
        return medicalHistory;
    }


    private MedicalHistoryObservation createMedicalHistoryObservation(PatientCreateRequestDTO patientCreateRequestDTO, MedicalHistory medicalHistory){
        //Obtiene Usuario autenticado.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserSec user = userService.getByUsername(authentication.getName());

        MedicalHistoryObservation medicalHistoryObservation = new MedicalHistoryObservation();
        medicalHistoryObservation.setMedicalHistory(medicalHistory);
        medicalHistoryObservation.setObservation(patientCreateRequestDTO.medicalHistoryObservation());
        medicalHistoryObservation.setCreatedAt(LocalDateTime.now());
        medicalHistoryObservation.setCreatedBy(user);
        medicalHistoryObservation.setEnabled(true);
        medicalHistoryObservation = medicalHistoryObservationService.create(medicalHistoryObservation);
        return medicalHistoryObservation;
    }


    /**
     * Método privado que construye un objeto {@link ContactEmail} y llama al servicio correspondiente para su creación.
     * @param patientCreateRequestDTO con los datos del contacto.
     * @param patient con los datos del paciente (incluye ID)
     * @return ContactEmail con el objeto creado.
     */
    private ContactEmail createContactEmail (PatientCreateRequestDTO patientCreateRequestDTO, Patient patient){
        ContactEmail contactEmail = new ContactEmail();
        contactEmail.setPersons(new HashSet<>());
        contactEmail.setEmail(patientCreateRequestDTO.email());
        contactEmail.getPersons().add(patient);
        contactEmail.setEnabled(true);
        contactEmail = contactEmailService.save(contactEmail);
        return contactEmail;
    }


    /**
     * Método privado que construye un objeto {@link ContactPhone} y llama al servicio correspondiente para su creación.
     * @param patientCreateRequestDTO con los datos del contacto.
     * @param patient con los datos del paciente (incluye ID)
     * @return ContactEmail con el objeto creado.
     */
    private ContactPhone createContactPhone(PatientCreateRequestDTO patientCreateRequestDTO, Patient patient){
        ContactPhone contactPhone = new ContactPhone();
        contactPhone.setPerson(new HashSet<>());
        contactPhone.setNumber(patientCreateRequestDTO.phone());
        contactPhone.setPhoneType(telephoneTypeService.getById(patientCreateRequestDTO.phoneType()));
        contactPhone.setNumber(patientCreateRequestDTO.phone());
        contactPhone.getPerson().add(patient);
        contactPhone.setEnabled(true);
        contactPhone = contactPhoneService.save(contactPhone);
        return contactPhone;
    }


    /**
     * Construye un DTO de respuesta con los datos del paciente recién creado, su dirección,
     * contactos y antecedentes médicos.
     *
     * @param patient Objeto {@link Patient} creado.
     * @param address Objeto {@link Address} asociado al paciente.
     * @param contactEmail Objeto {@link ContactEmail} del paciente.
     * @param contactPhone Objeto {@link ContactPhone} del paciente.
     * @param medicalHistory Objeto {@link MedicalHistory} del paciente.
     * @return DTO de respuesta con toda la información del paciente.
     */
    private PatientCreateResponseDTO createResponseDTO (Patient patient, Address address, ContactEmail contactEmail,ContactPhone contactPhone, MedicalHistory medicalHistory, MedicalHistoryObservation medicalHistoryObservation){
        return new PatientCreateResponseDTO(
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
                medicalHistory.getMedicalRisks(),
                medicalHistoryObservation.getObservation()
         );
    }



// YA LO VOY A NECESITAR.
// (null,"exception.patientNotFound.user",null,"exception.patientNotFound.log",null,dni,"PatientService", "validatePatient", LogLevel.WARN));

}

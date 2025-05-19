package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.*;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.ConflictException;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.*;
import com.odontologiaintegralfm.repository.IPatientRepository;
import com.odontologiaintegralfm.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


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
    private IHealthPlanService healthPlanService;

    @Autowired
    private IContactEmailService contactEmailService;

    @Autowired
    private IContactPhoneService contactPhoneService;

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IMedicalRiskService medicalRiskService;

    @Autowired
    private IMedicalHistoryService medicalHistoryService;

    @Autowired IMedicalHistoryRiskService medicalHistoryRiskService;


    @Autowired
    private PersonService personService;


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
     * @param patientRequestDTO DTO con los datos necesarios para crear el paciente.
     * @return {@link Response} que contiene un {@link PatientResponseDTO} con los datos del paciente creado.
     * @throws DataBaseException si ocurre un error durante el acceso a la base de datos.
     */
    @Override
    @Transactional
    public Response<PatientResponseDTO> create(PatientCreateRequestDTO patientRequestDTO) {
        try{


            //Valída que la persona no exista por combinación Tipo DNI + DNI número.
            personService.validatePerson(patientRequestDTO.personDto().dniTypeId(), patientRequestDTO.personDto().dni());

            //Valída que no exista el n.° de afiliado del plan de salud.
            validatePatient(patientRequestDTO.affiliateNumber());


            //Crear objeto dirección.
            Address address = addressService.enableOrCreate(
                    addressService.buildAddress(patientRequestDTO.addressDto())
            );

            // Crear objeto Paciente (Internamente crea la persona)
            Patient patient = buildPatient(patientRequestDTO, address);
            patientRepository.save(patient);

            //Crear objeto Historia Clínica.
            MedicalHistory medicalHistory = medicalHistoryService.create(
                    medicalHistoryService.buildMedicalHistory(patient)
            );

            //Crear Objeto Historia Clínica - Riesgos.
            Set <MedicalHistoryRiskResponseDTO> medicalHistoryRiskResponseDTOS = medicalHistoryRiskService.createMedicalHistoryRisk(patientRequestDTO.medicalRiskDto(), medicalHistory);

            //Crear objeto Contacto Email
            ContactEmail contactEmail = contactEmailService.create(
                    contactEmailService.buildContactEmail(
                            patientRequestDTO.contactDto().email(),
                            patient
                    )
            );

            //Crear objeto Contacto Teléfono.
            ContactPhone contactPhone = contactPhoneService.create(
                    contactPhoneService.buildContactPhone(
                            patientRequestDTO.contactDto().phone(),
                            patientRequestDTO.contactDto().phoneType(),
                            patient)
            );



            //Crear Objeto Respuesta
            PatientResponseDTO patientResponseDTO = buildResponseDTO(patient,address,contactEmail,contactPhone,medicalHistory,medicalHistoryRiskResponseDTOS);

            //Crear mensaje para el usuario.
            String messageUser = messageService.getMessage("patientService.save.ok.user",null, LocaleContextHolder.getLocale());


            return new Response<>(true,messageUser, patientResponseDTO);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", null, patientRequestDTO.personDto().dni(), "save");
        }
    }

    /**
     * Método para actualizar datos de un paciente.
     *
     * @param patientUpdateRequestDTO El objeto paciente a Actualizar.
     * @return Una respuesta que contiene el objeto {@link PatientUpdateRequestDTO } del paciente actualizado.
     */
    @Override
    @Transactional
    public Response<PatientResponseDTO> update(PatientUpdateRequestDTO patientUpdateRequestDTO) {
        try{
            //Validar que el paciente exista y recuperarlo desde la Base.
            Patient patient = validateExistentPatient(patientUpdateRequestDTO.personDto().id());

            //Actualiza datos de la persona.
            patient = (Patient) personService.update(patient, patientUpdateRequestDTO.personDto());

            //Actualiza datos del paciente
            patient.setAffiliateNumber(patientUpdateRequestDTO.affiliateNumber());
            if(!patient.getHealthPlan().getId().equals(patientUpdateRequestDTO.healthPlanId())){
                patient.setHealthPlan(healthPlanService.getById(patientUpdateRequestDTO.healthPlanId()));
            }

            //Actualiza datos de otras entidades.
            Address address = addressService.updatePatientAddress(patient, patientUpdateRequestDTO.addressDto());
            ContactEmail contactEmail = contactEmailService.updateContactEmail(patientUpdateRequestDTO.contactDto().email(), patient);
            ContactPhone contactPhone = contactPhoneService.updatePatientContactPhone(patientUpdateRequestDTO.contactDto().phone(), patientUpdateRequestDTO.contactDto().phoneType(), patient);
            Set<MedicalHistoryRiskResponseDTO> medicalHistoryRiskResponseDTOS = medicalHistoryRiskService.updatePatientMedicalRisk(patient, patientUpdateRequestDTO.medicalRiskDto());
            MedicalHistory medicalHistory = medicalHistoryService.getByPatient(patient.getId());

            patientRepository.save(patient);

            PatientResponseDTO patientResponseDTO = buildResponseDTO(patient,address,contactEmail,contactPhone,medicalHistory,medicalHistoryRiskResponseDTOS);


            return new Response<>(true,"", patientResponseDTO);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", patientUpdateRequestDTO.personDto().id(), patientUpdateRequestDTO.personDto().lastName() + "," + patientUpdateRequestDTO.personDto().firstName(), "validateNonExistentPatient");
        }
    }

    /**
     * Método para obtener un listado de pacientes habilitados en el sistema.
     *
     * @return Una respuesta que contiene una lista de objetos {@link PatientResponseDTO }
     */
    @Override
    public Response<List<PatientResponseDTO>> getAll() {
        try{
            List <Patient> patients = patientRepository.findAllByEnabledTrue();

            List<PatientResponseDTO> patientResponseDTOS = patients.stream()
                    .map(this::buildFullPatient) // Por cada elemento del stream, se llama al método buildFullPatient de esta instancia, pasando el elemento como parámetro.
                    .toList();

            return new Response<>(true,null, patientResponseDTOS);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService",null,null, "getAll");
        }
    }

    /**
     * Método para obtener un paciente habilitado por ID
     *
     * @param id del paciente
     * @return Una respuesta que contiene el objeto {@link PatientResponseDTO } del paciente
     */
    @Override
    public Response<PatientResponseDTO> getById(Long id) {
        try{
            Patient patient = patientRepository.findById(id).orElseThrow(()-> new NotFoundException("exception.patientNotFound.user",null, "exception.patientNotFound.log", new Object[]{id, "PatientService", "getById"}, LogLevel.ERROR ));
            return new Response<>(true,null, buildFullPatient(patient));

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService",null,null, "getById");
        }
    }


    /**
     * Método privado para validar:
     * Si existe el paciente por combinación Tipo dni + Dni Número
     * Si existe un paciente con N° de afiliado a un plan de salud.
     * Si existe arroja la exception {@link ConflictException } con el mensaje "exception.patientExists"
     * Si no existe no se realiza ninguna acción.
     * @param affiliateNumber del paciente a buscar.
     */
    private void validatePatient(String affiliateNumber){
        try{
            Optional<Patient>patientAffiliateNumber =  patientRepository.findByAffiliateNumberAndEnabledTrue(affiliateNumber);
            if(patientAffiliateNumber.isPresent()){
                throw new ConflictException("exception.affiliateNumber.user", null,"exception.affiliateNumber.log", new Object[]{affiliateNumber,"PatientService", "validatePatient"}, LogLevel.ERROR);
            }

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", null,affiliateNumber, "validatePatient");
        }
    }


    private Patient validateExistentPatient(Long Id){
        try{
            Optional<Patient> patient = patientRepository.findById(Id);
            if(patient.isEmpty()){
                throw new NotFoundException("exception.patientNotFound.user",null,"exception.patientNotFound.log", new Object[]{Id, "PatientService", "validateExistentPatient"}, LogLevel.WARN);

            }
             return patient.get();

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e,"PatientService",Id,null,"validateExistentPatient");
        }
    }


    /**
     * Método protegido para crear el paciente.
     * @param patientRequestDTO con los datos del paciente.
     * @param address con los datos del domicilio del paciente.
     * @return Patient con los datos creados.
     */
    @Transactional
    protected Patient buildPatient(PatientCreateRequestDTO patientRequestDTO, Address address){
        try {
            //Crea paciente.
            Patient patient = new Patient();
            patient.setHealthPlan(healthPlanService.getById(patientRequestDTO.healthPlanId()));
            patient.setAffiliateNumber(patientRequestDTO.affiliateNumber());
            patient = (Patient) personService.build(patient, patientRequestDTO.personDto(), address);
            return patient;

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", null, patientRequestDTO.personDto().dni(), "createPatient");
        }
    }

    /**
     * Método privado para obtener todos los datos adicionales de otras entidades para devolver un paciente completo
     * @param patient objeto con los datos del paciente
     * @return PatientResponseDTO
     */
    private PatientResponseDTO buildFullPatient(Patient patient){
        Address address = addressService.getByPersonId(patient.getId());
        ContactEmail contactEmail = contactEmailService.getByPerson(patient);
        ContactPhone contactPhone = contactPhoneService.getByPerson(patient);
        MedicalHistory medicalHistory = medicalHistoryService.getByPatient(patient.getId());
        Set<MedicalHistoryRisk> risk = medicalHistoryRiskService.getByIdHistoryRisk(medicalHistory.getId());
        Set<MedicalHistoryRiskResponseDTO> riskResponseDTOS = risk.stream()
                .map(r -> new MedicalHistoryRiskResponseDTO(
                        r.getId(),
                        r.getMedicalRisk().getName(),
                        r.getObservation()))
                .collect(Collectors.toSet());

        return buildResponseDTO(patient,address,contactEmail,contactPhone,medicalHistory,riskResponseDTOS);
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
    private PatientResponseDTO buildResponseDTO(Patient patient, Address address, ContactEmail contactEmail, ContactPhone contactPhone, MedicalHistory medicalHistory, Set<MedicalHistoryRiskResponseDTO> medicalHistoryRiskResponseDTO){
        return new PatientResponseDTO(
             new PersonResponseDTO(
                        patient.getId(),
                        patient.getFirstName(),
                        patient.getLastName(),
                        patient.getDniType().getName(),
                        patient.getDni(),
                        patient.getBirthDate(),
                        Period.between(patient.getBirthDate(), LocalDate.now()).getYears(),
                        patient.getGender().getName(),
                        patient.getNationality().getName()
                ),
                new AddressResponseDTO(
                        address.getLocality().getId(),
                        address.getLocality().getName(),
                        address.getLocality().getProvince().getId(),
                        address.getLocality().getProvince().getName(),
                        address.getLocality().getProvince().getCountry().getId(),
                        address.getLocality().getProvince().getCountry().getName(),
                        address.getStreet(),
                        address.getNumber(),
                        address.getFloor(),
                        address.getApartment()
                ),
                new ContactResponseDTO(
                        contactEmail.getEmail(),
                        contactPhone.getPhoneType().getName(),
                        contactPhone.getNumber()
                ),
                patient.getHealthPlan().getName(),
                patient.getAffiliateNumber(),
                medicalHistory.getId(),
                medicalHistoryRiskResponseDTO
         );
    }


}

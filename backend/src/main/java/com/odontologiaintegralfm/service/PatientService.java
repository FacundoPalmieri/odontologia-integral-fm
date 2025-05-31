package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.configuration.securityConfig.AuthenticatedUserService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class PatientService implements IPatientService {
    @Autowired
    private IMessageService messageService;

    @Autowired
    private IPatientRepository patientRepository;

    @Autowired
    private IHealthPlanService healthPlanService;

    @Autowired
    IPatientMedicalRiskService patientMedicalRiskService;

    @Autowired
    private PersonService personService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private IAddressService addressService;


    /**
     * Crea un nuevo paciente en el sistema junto a los riesgos médicos.
     * Devuelve una respuesta con todos los datos relevantes.
     *
     * <p>Este método sigue los siguientes pasos:
     * <ol>
     *     <li>Valida que no exista un paciente con el mismo DNI.</li>
     *     <li>Crea por medio del PersonService un nuevo objeto {@link Patient} y lo persiste.</li>
     *     <li>Crea por medio del patientMedicalRiskService los riesgos médicos asociados al paciente .</li>
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
            //Valída que no exista el n.° de afiliado del plan de salud.
            validatePatient(patientRequestDTO.affiliateNumber());

            //Valída y crea una persona.
            Person person = personService.create(patientRequestDTO.personDto());

            //Crea Paciente
            Patient patient = new Patient();
            patient.setPerson(person);
            patient.setHealthPlan(healthPlanService.getById(patientRequestDTO.healthPlanId()));
            patient.setAffiliateNumber(patientRequestDTO.affiliateNumber());
            patient.setCreatedAt(LocalDateTime.now());
            patient.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
            patient.setEnabled(true);

            //Persiste el paciente.
            patientRepository.save(patient);


            //Crear Objeto Riegos médicos del paciente y persiste la misma.
            Set <PatientMedicalRiskResponseDTO> patientMedicalRiskResponseDTOS = patientMedicalRiskService.create(patientRequestDTO.medicalRiskDto(),patient);

            //Crear Objeto Respuesta
            PatientResponseDTO patientResponseDTO = buildResponseDTO(patient, patientMedicalRiskResponseDTOS);

            //Crear mensaje para el usuario.
            String messageUser = messageService.getMessage("patientService.save.ok.user",null, LocaleContextHolder.getLocale());


            return new Response<>(true,messageUser, patientResponseDTO);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", null, "DNI Paciente" + patientRequestDTO.personDto().dni(), "create");
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
            Person person = personService.update(patient.getPerson(), patientUpdateRequestDTO.personDto());
            patient.setPerson(person);

            //Actualiza datos del paciente
            patient.setAffiliateNumber(patientUpdateRequestDTO.affiliateNumber());
            patient.setHealthPlan(healthPlanService.getById(patientUpdateRequestDTO.healthPlanId()));
            Set<PatientMedicalRiskResponseDTO> patientMedicalRiskResponseDTOS = patientMedicalRiskService.update(patient, patientUpdateRequestDTO.medicalRiskDto());

            //Cambios para auditoria.
            patient.setUpdatedAt(LocalDateTime.now());
            patient.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());

            patientRepository.save(patient);

            //Crear Objeto Respuesta
            PatientResponseDTO patientResponseDTO = buildResponseDTO(patient,patientMedicalRiskResponseDTOS);

            //Crear mensaje para el usuario.
            String messageUser = messageService.getMessage("patientService.update.ok.user",null, LocaleContextHolder.getLocale());

            return new Response<>(true,messageUser, patientResponseDTO);

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
                    .map(patient -> {
                        Set<PatientMedicalRisk> risks = patientMedicalRiskService.getByPatient(patient);
                        Set<PatientMedicalRiskResponseDTO> riskDTOs = patientMedicalRiskService.convertToDTO(risks);
                       return buildResponseDTO(patient,riskDTOs);
                    })
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
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(()-> new NotFoundException("exception.patientNotFound.user",null, "exception.patientNotFound.log", new Object[]{id, "PatientService", "getById"}, LogLevel.ERROR ));

            Set<PatientMedicalRisk> risks = patientMedicalRiskService.getByPatient(patient);
            Set<PatientMedicalRiskResponseDTO> riskDTOs = patientMedicalRiskService.convertToDTO(risks);

            return new Response<>(true,null, buildResponseDTO(patient,riskDTOs));

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService",id,"<- Id Paciente", "getById");
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

    /**
     * Método privado para validar la existencia de un paciente, antes de actualizarlo.
     * @param Id del paciente
     * @return Patient
     */
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
     * Construye un DTO de respuesta con los datos del paciente creado, su dirección,
     * contactos y antecedentes médicos.
     *
     * @param patient Contiene solo con los datos de la entidad Paciente (incluye Domicilio)
     * @param patientMedicalRiskResponseDTO contiene la lista de riesgos médicos.
     * @return DTO de respuesta con toda la información del paciente.
     */
    private PatientResponseDTO buildResponseDTO(Patient patient, Set<PatientMedicalRiskResponseDTO> patientMedicalRiskResponseDTO){
        return new PatientResponseDTO(
            personService.convertToDTO(patient.getPerson()),
                patient.getHealthPlan().getName(),
                patient.getAffiliateNumber(),
                patientMedicalRiskResponseDTO
         );
    }
}

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;



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
            if(patientRequestDTO.affiliateNumber() != null) {
                validatePatient(patientRequestDTO.affiliateNumber());
            }

            //Valída y crea una persona.
            Person person = personService.create(patientRequestDTO.person());

            //Crea Paciente
            Patient patient = new Patient();
            patient.setPerson(person);
            if(patientRequestDTO.healthPlanId() != null){
                patient.setHealthPlan(healthPlanService.getById(patientRequestDTO.healthPlanId()));
                patient.setAffiliateNumber(patientRequestDTO.affiliateNumber());
            }

            patient.setCreatedAt(LocalDateTime.now());
            patient.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
            patient.setEnabled(true);

            //Persiste el paciente.
            patientRepository.save(patient);


            //Crear Objeto Riegos médicos del paciente y persiste la misma.
            Set <PatientMedicalRiskResponseDTO> patientMedicalRiskResponseDTOS = patientMedicalRiskService.create(patientRequestDTO.medicalRisk(),patient);

            //Crear Objeto Respuesta
            PatientResponseDTO patientResponseDTO = buildResponseDTO(patient, patientMedicalRiskResponseDTOS);

            //Crear mensaje para el usuario.
            String messageUser = messageService.getMessage("patientService.save.ok.user",null, LocaleContextHolder.getLocale());


            return new Response<>(true,messageUser, patientResponseDTO);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", null, "DNI Paciente" + patientRequestDTO.person().dni(), "create");
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
            Patient patient = validateExistentPatient(patientUpdateRequestDTO.person().id());

            //Actualiza datos de la persona.
            Person person = personService.update(patient.getPerson(), patientUpdateRequestDTO.person());
            patient.setPerson(person);

            //Actualiza datos del paciente
            if(patientUpdateRequestDTO.healthPlanId() != null) {
                patient.setAffiliateNumber(patientUpdateRequestDTO.affiliateNumber());
                patient.setHealthPlan(healthPlanService.getById(patientUpdateRequestDTO.healthPlanId()));
            }else{
                patient.setAffiliateNumber(null);
                patient.setHealthPlan(null);
            }


            Set<PatientMedicalRiskResponseDTO> patientMedicalRiskResponseDTOS = new HashSet<PatientMedicalRiskResponseDTO>();
            if(patientUpdateRequestDTO.medicalRisk() != null){
               patientMedicalRiskResponseDTOS = patientMedicalRiskService.update(patient, patientUpdateRequestDTO.medicalRisk());
            }

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
            throw new DataBaseException(e, "PatientService", patientUpdateRequestDTO.person().id(), patientUpdateRequestDTO.person().lastName() + "," + patientUpdateRequestDTO.person().firstName(), "validateNonExistentPatient");
        }
    }

    /**
     * Método para obtener un listado de pacientes habilitados en el sistema.
     *
     * @return Una respuesta que contiene una lista de objetos {@link PatientResponseDTO }
     */
    @Override
    public Response<Page<PatientResponseDTO>> getAll(int page, int size, String sortBy, String direction) {
        try{

            //Define criterio de ordenamiento
            Sort sort = direction.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            //Se define paginación con n°página, cantidad elementos y ordenamiento.
            Pageable pageable = PageRequest.of(page,size, sort);

            Page <Patient> patients = patientRepository.findAllByEnabledTrue(pageable);

            Page<PatientResponseDTO> patientResponseDTOS = patients
                    .map(patient -> {
                        Set<PatientMedicalRisk> risks = patientMedicalRiskService.getByPatient(patient);
                        Set<PatientMedicalRiskResponseDTO> riskDTOs = patientMedicalRiskService.convertToDTO(risks);
                       return buildResponseDTO(patient,riskDTOs);
                    });

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

        PatientResponseDTO patientResponseDTO = new PatientResponseDTO();
        patientResponseDTO.setPerson(personService.convertToDTO(patient.getPerson()));

        if((patient.getHealthPlan() != null) || (patient.getAffiliateNumber() != null)){
            patientResponseDTO.setHealthPlans(patient.getHealthPlan().getName());
            patientResponseDTO.setAffiliateNumber(patient.getAffiliateNumber());
        }

        patientResponseDTO.setMedicalHistoryRisk(patientMedicalRiskResponseDTO);

        return patientResponseDTO;
    }
}

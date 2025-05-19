package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.*;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.ConflictException;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.*;
import com.odontologiaintegralfm.repository.IDentistRepository;
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

@Service
public class DentistService implements IDentistService {
    @Autowired
    private IPersonService personService;

    @Autowired
    private IDentistRepository dentistRepository;

    @Autowired
    private IAddressService addressService;

    @Autowired
    private DentistSpecialtyService dentistSpecialtyService;

    @Autowired
    private UserService userService;

    @Autowired
    private IContactEmailService contactEmailService;

    @Autowired
    private IContactPhoneService contactPhoneService;

    @Autowired
    private IMessageService messageService;


    /**
     * Crea un nuevo odontólogo en el sistema junto con su domicilio, contactos, historia clínica
     * y devuelve una respuesta con todos los datos relevantes.
     *
     * <p>Este método sigue los siguientes pasos:
     * <ol>
     *     <li>Valida que no exista un odontólogo con el mismo DNI.</li>
     *     <li>Crea o habilita un domicilio según corresponda.</li>
     *     <li>Crea un nuevo objeto {@link Dentist} y lo persiste.</li>
     *     <li>Registra los contactos del odontólogo (email y teléfono).</li>
     *     <li>Construye un DTO con toda la información creada.</li>
     * </ol>
     *
     * @param dentistCreateRequestDTO DTO con los datos necesarios para crear el odontólogo.
     * @return {@link Response} que contiene un {@link DentistResponseDTO} con los datos del odontólogo creado.
     * @throws DataBaseException si ocurre un error durante el acceso a la base de datos.
     */
    @Override
    @Transactional
    public Response<DentistResponseDTO> create(DentistCreateRequestDTO dentistCreateRequestDTO) {
        try{
            //Valída que la persona no exista por combinación Tipo DNI + DNI número.
            personService.validatePerson(dentistCreateRequestDTO.personDto().dniTypeId(), dentistCreateRequestDTO.personDto().dni());

            //Valída que no exista el n.° de licencia.
            validateDentist(dentistCreateRequestDTO.licenseNumber());

            //Crear objeto dirección.
            Address address = addressService.enableOrCreate(
                    addressService.buildAddress(dentistCreateRequestDTO.addressDto())
            );

            //Crea objeto Odontólogo (Internamente crea la persona)
            Dentist dentist = buildDentist(dentistCreateRequestDTO,address);
            dentistRepository.save(dentist);

            //Crear objeto Contacto Email
            ContactEmail contactEmail = contactEmailService.create(
                    contactEmailService.buildContactEmail(
                            dentistCreateRequestDTO.contactDto().email(),
                            dentist
                    )
            );

            //Crear objeto Contacto Teléfono.
            ContactPhone contactPhone = contactPhoneService.create(
                    contactPhoneService.buildContactPhone(
                            dentistCreateRequestDTO.contactDto().phone(),
                            dentistCreateRequestDTO.contactDto().phoneType(),
                            dentist)
            );

            //Crear Objeto Respuesta
            DentistResponseDTO dentistResponseDTO = buildResponseDTO(dentist, address,contactEmail,contactPhone);

            //Crear mensaje para el usuario.
            String messageUser = messageService.getMessage("dentistService.save.ok.user",null, LocaleContextHolder.getLocale());

            return new Response<>(true, messageUser, dentistResponseDTO);


        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistService", null, dentistCreateRequestDTO.personDto().dni(), "create");
        }
    }

    /**
     * Método para obtener un listado de pacientes habilitados en el sistema.
     *
     * @return Una respuesta que contiene una lista de objetos {@link DentistResponseDTO }
     */
    @Override
    public Response<List<DentistResponseDTO>> getAll() {
        try{
            List<Dentist> dentists = dentistRepository.findAllByEnabledTrue();
            List<DentistResponseDTO> dentistResponseDTOList = dentists.stream()
                    .map(this::buildFullDentist) // Por cada elemento del stream, se llama al método buildFullPatient de esta instancia, pasando el elemento como parámetro.
                    .toList();

            return new Response<>(true, "", dentistResponseDTOList);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistService",null,null, "getAll");
        }
    }

    private void validateDentist(String licenseNumber) {
        try{
            Optional<Dentist> dentist  = dentistRepository.findByLicenseNumberAndEnabledTrue(licenseNumber);
            if(dentist.isPresent()){
                throw new ConflictException("exception.licenseNumber.user", null,"exception.licenseNumber.log", new Object[]{licenseNumber,"DentistService", "validateDentist"}, LogLevel.ERROR);
            }
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistService", null, licenseNumber, "validateDentist");
        }
    }

    /**
     * Método protegido para crear un odontólogo.
     * @param dentistCreateRequestDTO con los datos del odontólogo.
     * @param address con los datos del domicilio del odontólogo.
     * @return Dentist con los datos creados.
     */
    @Transactional
    protected Dentist buildDentist(DentistCreateRequestDTO dentistCreateRequestDTO, Address address) {
        try{
            //Crea dentista.
            Dentist dentist = new Dentist();
            dentist.setLicenseNumber(dentistCreateRequestDTO.licenseNumber());
            dentist.setDentistSpecialty(dentistSpecialtyService.getById(dentistCreateRequestDTO.dentistSpecialtyId()));

            //Construye Persona
            dentist = (Dentist) personService.build(dentist,dentistCreateRequestDTO.personDto(), address);

            //Crea Usuario
            dentist.setUser(userService.createInternal(dentistCreateRequestDTO.user()));

            //Retorna objeto completo (Datos de paciente + persona + usuario)
            return dentist;

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistService", null, dentistCreateRequestDTO.personDto().dni(), "buildDentist");
        }
    }


    /**
     * Método privado para obtener todos los datos adicionales de otras entidades para devolver un odontólogo completo
     * @param dentist objeto con los datos del odontólogo
     * @return DentistResponseDTO
     */
    private DentistResponseDTO buildFullDentist(Dentist dentist){
        Address address = addressService.getByPersonId(dentist.getId());
        ContactEmail contactEmail = contactEmailService.getByPerson(dentist);
        ContactPhone contactPhone = contactPhoneService.getByPerson(dentist);
        return buildResponseDTO(dentist, address,contactEmail,contactPhone);
    }


    /**
     * Construye un DTO de respuesta con los datos del odontólogo creado, su dirección,
     * contactos y antecedentes médicos.
     * @param dentist  Objeto {@link Dentist} creado
     * @param address Objeto {@link Address} asociado al odontólogo.
     * @param contactEmail Objeto {@link ContactEmail} del odontólogo.
     * @param contactPhone Objeto {@link ContactPhone} del odontólogo.
     * @return DTO de respuesta con toda la información del odontólogo.
     */
    private DentistResponseDTO buildResponseDTO(Dentist dentist, Address address, ContactEmail contactEmail, ContactPhone contactPhone) {
        return new DentistResponseDTO(
                new PersonResponseDTO(
                        dentist.getId(),
                        dentist.getFirstName(),
                        dentist.getLastName(),
                        dentist.getDniType().getName(),
                        dentist.getDni(),
                        dentist.getBirthDate(),
                        Period.between(dentist.getBirthDate(), LocalDate.now()).getYears(),
                        dentist.getGender().getName(),
                        dentist.getNationality().getName()
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
                new UserSecResponseDTO(
                        dentist.getUser().getId(),
                        dentist.getUser().getUsername(),
                        dentist.getUser().getRolesList(),
                        dentist.getUser().isEnabled()
                ),
                dentist.getLicenseNumber(),
                dentist.getDentistSpecialty().getName()
        );
    }
}

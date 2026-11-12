package com.odontologiaintegralfm.feature.consultation.consultationinstance.service;

import com.odontologiaintegralfm.feature.consultation.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.dentist.model.Dentist;
import com.odontologiaintegralfm.feature.person.person.model.Person;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests de ConsultationInstanceDomainService (REQ-039, T-03).
 *
 * Consultation, Dentist, Person y UserSec se mockean: son datos de entrada que el service solo lee. Cada Person
 * deja stubbeado su id. El usuario se pasa por parámetro, así que el service no tiene colaboradores que mockear.
 */
@Tag("unit")
class ConsultationInstanceDomainServiceTest {

    private final ConsultationInstanceDomainService service = new ConsultationInstanceDomainService();

    private static Person person(long id) {
        Person person = mock(Person.class);
        when(person.getId()).thenReturn(id);
        return person;
    }

    private static Consultation consultationOfDentistWithPerson(Person dentistPerson) {
        Dentist dentist = mock(Dentist.class);
        when(dentist.getPerson()).thenReturn(dentistPerson);
        Consultation consultation = mock(Consultation.class);
        when(consultation.getDentist()).thenReturn(dentist);
        return consultation;
    }

    private static UserSec userWithPerson(Person person) {
        UserSec user = mock(UserSec.class);
        when(user.getPerson()).thenReturn(person);
        return user;
    }

    /**
     * CASO: la odontóloga de la consulta, con Person 1007, es el usuario que se le pasa.
     * Regla: T-03 — el dueño de la consulta es quien tiene la misma Person que el odontólogo de la consulta.
     * Los ids son mayores a 127 a propósito: así cada getId() devuelve un Long distinto en memoria y una comparación
     * con == en vez de equals daría false.
     * Validación: isAssignedDentist(consulta, usuario) devuelve true.
     */
    @Test
    void isAssignedDentist_authenticatedUserIsConsultationDentist_returnsTrue() {
        UserSec user = userWithPerson(person(1007L));
        Consultation consultation = consultationOfDentistWithPerson(person(1007L));

        assertThat(service.isAssignedDentist(consultation, user)).isTrue();
    }

    /**
     * CASO: el usuario tiene Person 1007 y la consulta es de un odontólogo con Person 1009.
     * Regla: T-03 — otro odontólogo no es el dueño, y el método responde false sin lanzar nada: cada llamador lanza su 404.
     * Validación: isAssignedDentist(consulta, usuario) devuelve false y no lanza excepción.
     */
    @Test
    void isAssignedDentist_authenticatedUserIsAnotherDentist_returnsFalse() {
        UserSec user = userWithPerson(person(1007L));
        Consultation consultation = consultationOfDentistWithPerson(person(1009L));

        assertThat(service.isAssignedDentist(consultation, user)).isFalse();
    }
}

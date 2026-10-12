package com.odontologiaintegralfm.feature.dentist.catalogs.service;

import com.odontologiaintegralfm.feature.dentist.catalogs.model.DentistSpecialty;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.shared.exception.AppException;
import com.odontologiaintegralfm.shared.exception.ForbiddenException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * El service no tiene colaboradores: se instancia directo. Las especialidades se construyen reales
 * con id (la igualdad de DentistSpecialty es por id, ver caso 5); el Dentist es mock (dato de entrada).
 */
@Tag("unit")
class DentistSpecialtyDomainServiceTest {

    private final DentistSpecialtyDomainService service = new DentistSpecialtyDomainService();

    private static DentistSpecialty specialty(Long id) {
        DentistSpecialty s = new DentistSpecialty();
        s.setId(id);
        return s;
    }

    private static Dentist dentistWith(DentistSpecialty specialty) {
        Dentist dentist = mock(Dentist.class);
        when(dentist.getDentistSpecialty()).thenReturn(specialty);
        return dentist;
    }

    /**
     * CASO: El catálogo del ítem no tiene especialidades cargadas.
     * Regla: D-61 — un conjunto vacío no restringe a nadie.
     * Validación: no lanza, ni con odontólogo endodoncista ni con odontólogo sin especialidad.
     */
    @Test
    void validateAllowed_emptyAllowedSpecialties_doesNotRestrict() {
        Dentist endodontist = dentistWith(specialty(1L));
        Dentist withoutSpecialty = dentistWith(null);

        assertThatCode(() -> service.validateAllowed(endodontist, Set.of(), "Restauración")).doesNotThrowAnyException();
        assertThatCode(() -> service.validateAllowed(withoutSpecialty, Set.of(), "Restauración")).doesNotThrowAnyException();
    }

    /**
     * CASO: Conjunto {Endodoncia, Ortodoncia} y odontólogo endodoncista.
     * Regla: D-61 — la especialidad del odontólogo tiene que estar en el conjunto.
     * Validación: no lanza.
     */
    @Test
    void validateAllowed_dentistSpecialtyIncluded_isAccepted() {
        DentistSpecialty endodontics = specialty(1L);
        DentistSpecialty orthodontics = specialty(2L);
        Dentist dentist = dentistWith(endodontics);

        assertThatCode(() -> service.validateAllowed(dentist, Set.of(endodontics, orthodontics), "Endodoncia"))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Conjunto {Ortodoncia} y odontólogo endodoncista.
     * Regla: D-61 — especialidad no incluida es 403.
     * Validación: ForbiddenException con el label del ítem ("Alineadores") entre los argumentos del mensaje.
     */
    @Test
    void validateAllowed_dentistSpecialtyNotIncluded_throwsForbiddenException() {
        Dentist dentist = dentistWith(specialty(1L));

        assertThatThrownBy(() -> service.validateAllowed(dentist, Set.of(specialty(2L)), "Alineadores"))
                .isInstanceOf(ForbiddenException.class)
                .satisfies(e -> assertThat(((AppException) e).getUserArgs()).contains("Alineadores"));
    }

    /**
     * CASO: El odontólogo no tiene especialidad (null) y el catálogo es restrictivo {Ortodoncia}.
     * Regla: D-61 (ampliación) — sin especialidad con catálogo restrictivo es 403.
     * Validación: ForbiddenException.
     */
    @Test
    void validateAllowed_dentistWithoutSpecialtyAndRestrictiveCatalog_throwsForbiddenException() {
        Dentist dentist = dentistWith(null);

        assertThatThrownBy(() -> service.validateAllowed(dentist, Set.of(specialty(2L)), "Alineadores"))
                .isInstanceOf(ForbiddenException.class);
    }

    /**
     * CASO: La especialidad del odontólogo y la del catálogo son instancias distintas con el mismo id.
     * Regla: La comparación es por id, no por identidad de objeto.
     * Validación: no lanza.
     */
    @Test
    void validateAllowed_singleSpecialtyCatalogMatchesById_isAccepted() {
        Dentist dentist = dentistWith(specialty(7L));
        DentistSpecialty otherInstanceSameId = specialty(7L);

        assertThat(otherInstanceSameId).isNotSameAs(dentist.getDentistSpecialty());
        assertThatCode(() -> service.validateAllowed(dentist, Set.of(otherInstanceSameId), "Endodoncia"))
                .doesNotThrowAnyException();
    }
}

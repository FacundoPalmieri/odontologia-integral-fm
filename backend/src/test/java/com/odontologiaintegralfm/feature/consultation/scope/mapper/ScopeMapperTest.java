
package com.odontologiaintegralfm.feature.consultation.scope.mapper;

import com.odontologiaintegralfm.feature.consultation.scope.dto.ScopeDTO;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.scope.enums.ToothFace;
import com.odontologiaintegralfm.feature.consultation.scope.model.Scope;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de ScopeMapper.toScopeDTO (REQ-038): método default con lógica propia (null en los grupos vacíos).
 * Cada test arma el Scope real con Scope.build y un solo grupo poblado.
 */
@Tag("unit")
class ScopeMapperTest {

    private final ScopeMapper mapper = new ScopeMapperImpl();

    private static List<Tooth> teeth(int... codes) {
        return Arrays.stream(codes).mapToObj(Tooth::fromCode).toList();
    }

    /**
     * CASO: una prótesis en 14/16/17.
     * Regla: D-06 — solo el grupo poblado viaja; fullMouth false sale null.
     * Validación: tooth [14, 16, 17]; toothFace, quadrant, maxillary y fullMouth son null.
     */
    @Test
    void toScopeDTO_toothList_toothFilledOthersNull() {
        ScopeDTO dto = mapper.toScopeDTO(Scope.build(teeth(14, 16, 17), null, null, null, false));

        assertThat(dto.tooth()).containsExactly(Tooth.fromCode(14), Tooth.fromCode(16), Tooth.fromCode(17));
        assertThat(dto.toothFace()).isNull();
        assertThat(dto.quadrant()).isNull();
        assertThat(dto.maxillary()).isNull();
        assertThat(dto.fullMouth()).isNull();
    }

    /**
     * CASO: un diente con cara.
     * Regla: D-06 — la cara se conserva.
     * Validación: tooth [14] y toothFace igual a la cara; quadrant, maxillary y fullMouth son null.
     */
    @Test
    void toScopeDTO_toothWithFace_keepsFace() {
        ScopeDTO dto = mapper.toScopeDTO(Scope.build(teeth(14), ToothFace.CENTER, null, null, false));

        assertThat(dto.tooth()).containsExactly(Tooth.fromCode(14));
        assertThat(dto.toothFace()).isEqualTo(ToothFace.CENTER);
        assertThat(dto.quadrant()).isNull();
        assertThat(dto.maxillary()).isNull();
        assertThat(dto.fullMouth()).isNull();
    }

    /**
     * CASO: una ubicación de cuadrante, sin dientes.
     * Regla: D-06 — la lista de dientes vacía sale null, no lista vacía.
     * Validación: quadrant es el cuadrante; tooth es null; toothFace, maxillary y fullMouth son null.
     */
    @Test
    void toScopeDTO_quadrant_toothNullAndFullMouthNull() {
        ScopeDTO dto = mapper.toScopeDTO(Scope.build(null, null, Quadrant.UPPER_RIGHT, null, false));

        assertThat(dto.quadrant()).isEqualTo(Quadrant.UPPER_RIGHT);
        assertThat(dto.tooth()).isNull();
        assertThat(dto.toothFace()).isNull();
        assertThat(dto.maxillary()).isNull();
        assertThat(dto.fullMouth()).isNull();
    }

    /**
     * CASO: una prótesis completa de un maxilar.
     * Regla: D-06 — el maxilar pasa tal cual.
     * Validación: maxillary es el maxilar; tooth, quadrant, toothFace y fullMouth son null.
     */
    @Test
    void toScopeDTO_maxillary_maxillaryFilled() {
        ScopeDTO dto = mapper.toScopeDTO(Scope.build(null, null, null, Maxillary.UPPER, false));

        assertThat(dto.maxillary()).isEqualTo(Maxillary.UPPER);
        assertThat(dto.tooth()).isNull();
        assertThat(dto.quadrant()).isNull();
        assertThat(dto.toothFace()).isNull();
        assertThat(dto.fullMouth()).isNull();
    }

    /**
     * CASO: una ubicación de boca completa.
     * Regla: D-06 — fullMouth true viaja como true.
     * Validación: fullMouth es true; tooth, quadrant, maxillary y toothFace son null.
     */
    @Test
    void toScopeDTO_fullMouth_fullMouthTrue() {
        ScopeDTO dto = mapper.toScopeDTO(Scope.build(null, null, null, null, true));

        assertThat(dto.fullMouth()).isTrue();
        assertThat(dto.tooth()).isNull();
        assertThat(dto.quadrant()).isNull();
        assertThat(dto.maxillary()).isNull();
        assertThat(dto.toothFace()).isNull();
    }
}
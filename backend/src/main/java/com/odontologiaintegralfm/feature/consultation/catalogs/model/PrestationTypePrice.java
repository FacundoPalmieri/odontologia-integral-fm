package com.odontologiaintegralfm.feature.consultation.catalogs.model;


import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que mantiene histórico de precios de prestaciones.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(
        name = "prestations_types_prices",
        indexes = {
                @Index(
                        name = "idx_price_active",
                        columnList = "prestation_type_id, start_date, end_date"
                )
        }
)
@Where(clause = "enabled = true")
@Audited
public class PrestationTypePrice extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestation_type_id", nullable = false)
    private PrestationType prestationType;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private PrestationTypePrice(PrestationType prestationType, BigDecimal price, LocalDate startDate, LocalDate endDate) {
        this.prestationType = prestationType;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static PrestationTypePrice build(PrestationType prestationType, BigDecimal price, LocalDate startDate, LocalDate endDate) {
        return new PrestationTypePrice(prestationType, price, startDate, endDate);
    }
}


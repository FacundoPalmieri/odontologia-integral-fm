package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.Repository;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPromotionRepository extends JpaRepository<Promotion, Long> {
}

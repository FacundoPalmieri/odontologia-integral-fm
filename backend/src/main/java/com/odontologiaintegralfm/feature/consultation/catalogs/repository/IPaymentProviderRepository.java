package com.odontologiaintegralfm.feature.consultation.catalogs.repository;


import com.odontologiaintegralfm.feature.consultation.catalogs.model.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPaymentProviderRepository extends JpaRepository<PaymentProvider, Long> {

}

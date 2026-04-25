package com.odontologiaintegralfm.feature.consultation.catalogs.paymentprovider.repository;


import com.odontologiaintegralfm.feature.consultation.catalogs.paymentprovider.model.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPaymentProviderRepository extends JpaRepository<PaymentProvider, Long> {

    List<PaymentProvider> findAllByEnabledTrue();

}

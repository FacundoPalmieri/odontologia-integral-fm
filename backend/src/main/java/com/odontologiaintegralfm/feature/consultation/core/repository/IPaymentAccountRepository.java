package com.odontologiaintegralfm.feature.consultation.core.repository;


import com.odontologiaintegralfm.feature.consultation.core.model.PaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPaymentAccountRepository extends JpaRepository<PaymentAccount, Long> {

    Optional<PaymentAccount> findByAccountIdentifier(String cbu);
}

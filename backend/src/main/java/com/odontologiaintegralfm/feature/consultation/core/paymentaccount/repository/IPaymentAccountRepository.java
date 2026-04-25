package com.odontologiaintegralfm.feature.consultation.core.paymentaccount.repository;


import com.odontologiaintegralfm.feature.consultation.core.paymentaccount.model.PaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPaymentAccountRepository extends JpaRepository<PaymentAccount, Long> {

    /** Recupera cuenta por CBU */
    Optional<PaymentAccount> findByAccountIdentifier(String cbu);

    /** Recupera cuenta por Alias */
    Optional<PaymentAccount> findByAlias(String alias);

    /** Recupera cuenta por ID con estado Habilitado */
    Optional<PaymentAccount> findByIdAndEnabledTrue(Long id);

    /** Recupera todas las cuenta Habilitadas */
    List<PaymentAccount> findAllByEnabledTrue();

}

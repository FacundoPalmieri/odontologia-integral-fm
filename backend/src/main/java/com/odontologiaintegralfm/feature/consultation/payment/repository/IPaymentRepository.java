package com.odontologiaintegralfm.feature.consultation.payment.repository;

import com.odontologiaintegralfm.feature.consultation.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPaymentRepository extends JpaRepository<Payment, Long> {
}

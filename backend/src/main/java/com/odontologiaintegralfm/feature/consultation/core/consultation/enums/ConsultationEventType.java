package com.odontologiaintegralfm.feature.consultation.core.consultation.enums;

/**
 * Representa eventos excepcionales ocurridos durante el ciclo de vida de una consulta.
 * No describe estados normales, sino intervenciones humanas o administrativas sobre una consulta.
 */
public enum ConsultationEventType {

    /**Se utiliza cuando la consulta vuelve a un estado anterior no finalizado por error operativo.*/
    CONSULTATION_CORRECTED,

    /**Se utiliza cuando la consulta queda anulada definitivamente (Ej. Se inicio una consulta de un paciente que no se presentó).*/
    CONSULTATION_CANCELED,

    /**Indica que se realizó una corrección del odontograma.*/
    CONSULTATION_INSTANCE_CORRECTED
}

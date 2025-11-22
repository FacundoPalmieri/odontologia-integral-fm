package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCreateResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;


public interface IAppointmentService {


    Response<AppointmentCreateResponseDTO> create(AppointmentCreateRequestDTO appointmentCreateRequestDTO);




}

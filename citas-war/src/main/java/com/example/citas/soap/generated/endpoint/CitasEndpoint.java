package com.example.citas.soap.generated.endpoint;

import com.example.citas.soap.generated.BuscarCitasRequest;
import com.example.citas.soap.generated.BuscarCitasResponse;
import com.example.citas.soap.generated.Cita;
import com.example.citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;
import java.util.stream.Collectors;

@Endpoint
public class CitasEndpoint {

    private static final String NAMESPACE_URI = "http://example.com/citas";

    @Autowired
    private CitaService citaService;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "BuscarCitasRequest")
    @ResponsePayload
    public BuscarCitasResponse buscarCitas(@RequestPayload BuscarCitasRequest request) {
        BuscarCitasResponse response = new BuscarCitasResponse();

        // Obtener citas del paciente desde el servicio
        List<com.example.citas.persistence.entity.Cita> citasEntity =
                citaService.listarCitasPaciente(request.getPacienteId());

        // Convertir de Entity a tipo generado por JAXB
        List<Cita> citasSOAP = citasEntity.stream()
                .map(this::convertToCitaSOAP)
                .collect(Collectors.toList());

        response.getCita().addAll(citasSOAP);

        return response;
    }

    private Cita convertToCitaSOAP(com.example.citas.persistence.entity.Cita citaEntity) {
        Cita citaSOAP = new Cita();
        citaSOAP.setId(citaEntity.getId());
        citaSOAP.setFecha(citaEntity.getFecha().toString());
        citaSOAP.setHoraInicio(citaEntity.getHoraInicio().toString());
        citaSOAP.setHoraFin(citaEntity.getHoraFin().toString());
        citaSOAP.setEstado(citaEntity.getEstado());
        citaSOAP.setNombreMedico(citaEntity.getMedico().getNombre() + " " + citaEntity.getMedico().getApellidos());
        citaSOAP.setNombrePaciente(citaEntity.getPaciente().getNombre() + " " + citaEntity.getPaciente().getApellidos());
        return citaSOAP;
    }
}
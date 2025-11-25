package com.g3.parking.controller.rest;

import com.g3.parking.datatransfer.ActiveTicketResponse;
import com.g3.parking.model.Ticket;
import com.g3.parking.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/sites")
public class SiteApiController {

    @Autowired
    private TicketRepository ticketRepository;

    @GetMapping("/{siteId}/active-ticket")
    public ResponseEntity<?> getActiveTicket(@PathVariable Long siteId) {
        Optional<Ticket> ticket = ticketRepository.findActiveBySiteId(siteId);
        
        if (ticket.isPresent()) {
            ActiveTicketResponse response = ActiveTicketResponse.fromTicket(ticket.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

package com.skillswap.exchange_service.controller;

import com.skillswap.exchange_service.dto.ExchangeRequest;
import com.skillswap.exchange_service.dto.ExchangeResponse;
import com.skillswap.exchange_service.dto.ProposeTimeRequest;
import com.skillswap.exchange_service.exception.ApiException;
import com.skillswap.exchange_service.service.ExchangeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeController {

    private static final String USER_HEADER = "X-User-Id";

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    // L'identité vient uniquement de l'en-tête posé par la Gateway après validation du JWT.
    private Long userId(String header) {
        if (header == null || header.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        try {
            return Long.parseLong(header);
        } catch (NumberFormatException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Identité invalide");
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExchangeResponse create(
            @Valid @RequestBody ExchangeRequest request,
            @RequestHeader(value = USER_HEADER, required = false) String userId) {
        return exchangeService.create(request, userId(userId));
    }

    @GetMapping("/my-requests")
    public List<ExchangeResponse> myRequests(
            @RequestHeader(value = USER_HEADER, required = false) String userId) {
        return exchangeService.myRequests(userId(userId));
    }

    @GetMapping("/received")
    public List<ExchangeResponse> received(
            @RequestHeader(value = USER_HEADER, required = false) String userId) {
        return exchangeService.received(userId(userId));
    }

    @GetMapping("/{id}")
    public ExchangeResponse getById(
            @PathVariable("id") Long id,
            @RequestHeader(value = USER_HEADER, required = false) String userId) {
        return exchangeService.getById(id, userId(userId));
    }

    @PatchMapping("/{id}/accept")
    public ExchangeResponse accept(
            @PathVariable("id") Long id,
            @RequestHeader(value = USER_HEADER, required = false) String userId) {
        return exchangeService.accept(id, userId(userId));
    }

    @PatchMapping("/{id}/reject")
    public ExchangeResponse reject(
            @PathVariable("id") Long id,
            @RequestHeader(value = USER_HEADER, required = false) String userId) {
        return exchangeService.reject(id, userId(userId));
    }

    @PatchMapping("/{id}/propose-time")
    public ExchangeResponse proposeTime(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProposeTimeRequest request,
            @RequestHeader(value = USER_HEADER, required = false) String userId) {
        return exchangeService.proposeTime(id, request, userId(userId));
    }

    @PatchMapping("/{id}/accept-proposal")
    public ExchangeResponse acceptProposal(
            @PathVariable("id") Long id,
            @RequestHeader(value = USER_HEADER, required = false) String userId) {
        return exchangeService.acceptProposal(id, userId(userId));
    }

    @PatchMapping("/{id}/cancel")
    public ExchangeResponse cancel(
            @PathVariable("id") Long id,
            @RequestHeader(value = USER_HEADER, required = false) String userId) {
        return exchangeService.cancel(id, userId(userId));
    }

    @PatchMapping("/{id}/complete")
    public ExchangeResponse complete(
            @PathVariable("id") Long id,
            @RequestHeader(value = USER_HEADER, required = false) String userId) {
        return exchangeService.complete(id, userId(userId));
    }

    @PatchMapping("/{id}/confirm")
    public ExchangeResponse confirm(
            @PathVariable("id") Long id,
            @RequestHeader(value = USER_HEADER, required = false) String userId) {
        return exchangeService.confirm(id, userId(userId));
    }
}

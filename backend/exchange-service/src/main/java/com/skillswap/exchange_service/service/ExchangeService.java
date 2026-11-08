package com.skillswap.exchange_service.service;

import com.skillswap.exchange_service.client.SkillClient;
import com.skillswap.exchange_service.client.SkillDto;
import com.skillswap.exchange_service.dto.ExchangeRequest;
import com.skillswap.exchange_service.dto.ExchangeResponse;
import com.skillswap.exchange_service.dto.ProposeTimeRequest;
import com.skillswap.exchange_service.entity.Exchange;
import com.skillswap.exchange_service.enums.ExchangeStatus;
import com.skillswap.exchange_service.exception.ApiException;
import com.skillswap.exchange_service.repository.ExchangeRepository;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.skillswap.exchange_service.enums.ExchangeStatus.*;

@Service
public class ExchangeService {

    // Statuts pour lesquels une demande est considérée "en cours" (anti-doublon)
    private static final List<ExchangeStatus> ACTIVE_STATUSES =
            List.of(PENDING, TIME_PROPOSED, ACCEPTED, AWAITING_CONFIRMATION);

    private static final String SKILL_VALIDATED = "VALIDEE";

    private final ExchangeRepository exchangeRepository;
    private final SkillClient skillClient;

    public ExchangeService(ExchangeRepository exchangeRepository, SkillClient skillClient) {
        this.exchangeRepository = exchangeRepository;
        this.skillClient = skillClient;
    }

    // ------------------------------------------------------------------ création

    // Volontairement sans @Transactional : l'appel HTTP vers skill-service ne doit pas
    // garder une connexion SQL ouverte. Le save() a sa propre transaction.
    public ExchangeResponse create(ExchangeRequest request, Long requesterId) {
        SkillDto skill = fetchSkill(request.skillId());

        if (skill.etudiantId() == null) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Réponse invalide de skill-service");
        }
        if (skill.etudiantId().equals(requesterId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Vous ne pouvez pas demander un échange sur votre propre compétence");
        }
        if (!SKILL_VALIDATED.equals(skill.statutValidation())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cette compétence n'est pas encore validée : elle ne peut pas faire l'objet d'une demande");
        }
        if (exchangeRepository.existsByRequesterIdAndSkillIdAndStatusIn(
                requesterId, skill.id(), ACTIVE_STATUSES)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Vous avez déjà une demande en cours pour cette compétence");
        }

        Exchange e = new Exchange();
        e.setRequesterId(requesterId);
        e.setProviderId(skill.etudiantId());          // déduit de la compétence, jamais de la requête
        e.setSkillId(skill.id());
        e.setSkillTitle(truncate(skill.titre(), 255));
        e.setDescription(request.description());
        e.setDurationHours(request.durationHours());
        e.setProposedDateTime(request.proposedDateTime());
        e.setStatus(PENDING);

        return ExchangeResponse.from(exchangeRepository.save(e));
    }

    // -------------------------------------------------------------- consultation

    @Transactional(readOnly = true)
    public ExchangeResponse getById(Long id, Long userId) {
        Exchange e = load(id);
        if (!isParticipant(e, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à cet échange");
        }
        return ExchangeResponse.from(e);
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> myRequests(Long userId) {
        return exchangeRepository.findByRequesterIdOrderByCreatedAtDesc(userId)
                .stream().map(ExchangeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> received(Long userId) {
        return exchangeRepository.findByProviderIdOrderByCreatedAtDesc(userId)
                .stream().map(ExchangeResponse::from).toList();
    }

    // ------------------------------------------------------------------ workflow

    @Transactional
    public ExchangeResponse accept(Long id, Long userId) {
        Exchange e = load(id);
        requireProvider(e, userId, "Seul le prestataire peut accepter cette demande");
        requireStatus(e, "accepter", PENDING);
        e.setStatus(ACCEPTED);
        // TODO (credit-service) : réserver les crédits du demandeur à l'acceptation
        return save(e);
    }

    @Transactional
    public ExchangeResponse reject(Long id, Long userId) {
        Exchange e = load(id);
        requireProvider(e, userId, "Seul le prestataire peut refuser cette demande");
        requireStatus(e, "refuser", PENDING);
        e.setStatus(REJECTED);
        return save(e);
    }

    @Transactional
    public ExchangeResponse proposeTime(Long id, ProposeTimeRequest request, Long userId) {
        Exchange e = load(id);
        requireProvider(e, userId, "Seul le prestataire peut proposer un autre créneau");
        requireStatus(e, "proposer un autre créneau", PENDING);
        e.setProposedDateTime(request.proposedDateTime());
        e.setStatus(TIME_PROPOSED);
        return save(e);
    }

    // Le demandeur accepte le nouveau créneau proposé par le prestataire
    @Transactional
    public ExchangeResponse acceptProposal(Long id, Long userId) {
        Exchange e = load(id);
        requireRequester(e, userId, "Seul le demandeur peut accepter le nouveau créneau");
        requireStatus(e, "accepter le nouveau créneau", TIME_PROPOSED);
        e.setStatus(ACCEPTED);
        // TODO (credit-service) : réserver les crédits du demandeur à l'acceptation
        return save(e);
    }

    @Transactional
    public ExchangeResponse cancel(Long id, Long userId) {
        Exchange e = load(id);
        if (!isParticipant(e, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Seuls les participants peuvent annuler cet échange");
        }
        requireStatus(e, "annuler", PENDING, TIME_PROPOSED, ACCEPTED);
        e.setStatus(CANCELLED);
        // TODO (credit-service) : libérer les crédits réservés si l'échange était ACCEPTED
        return save(e);
    }

    // Le prestataire déclare la séance terminée
    @Transactional
    public ExchangeResponse complete(Long id, Long userId) {
        Exchange e = load(id);
        requireProvider(e, userId, "Seul le prestataire peut déclarer la séance terminée");
        requireStatus(e, "déclarer la séance terminée", ACCEPTED);
        e.setStatus(AWAITING_CONFIRMATION);
        return save(e);
    }

    // Le demandeur confirme que la séance a bien eu lieu
    @Transactional
    public ExchangeResponse confirm(Long id, Long userId) {
        Exchange e = load(id);
        requireRequester(e, userId, "Seul le demandeur peut confirmer la réalisation");
        requireStatus(e, "confirmer", AWAITING_CONFIRMATION);
        e.setStatus(COMPLETED);
        // TODO (credit-service) : déclencher le transfert des crédits vers le prestataire
        return save(e);
    }

    // ------------------------------------------------------------------- helpers

    private SkillDto fetchSkill(Long skillId) {
        try {
            return skillClient.getSkill(skillId);
        } catch (FeignException.NotFound ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Compétence introuvable");
        } catch (FeignException ex) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "skill-service est indisponible, veuillez réessayer plus tard");
        }
    }

    private Exchange load(Long id) {
        return exchangeRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Échange introuvable"));
    }

    // saveAndFlush : applique @PreUpdate et le contrôle de version avant de construire la réponse
    private ExchangeResponse save(Exchange e) {
        return ExchangeResponse.from(exchangeRepository.saveAndFlush(e));
    }

    private boolean isParticipant(Exchange e, Long userId) {
        return e.getRequesterId().equals(userId) || e.getProviderId().equals(userId);
    }

    private void requireProvider(Exchange e, Long userId, String message) {
        if (!e.getProviderId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, message);
        }
    }

    private void requireRequester(Exchange e, Long userId, String message) {
        if (!e.getRequesterId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, message);
        }
    }

    private void requireStatus(Exchange e, String action, ExchangeStatus... allowed) {
        if (!Arrays.asList(allowed).contains(e.getStatus())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Impossible de " + action + " : l'échange est au statut " + e.getStatus());
        }
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}

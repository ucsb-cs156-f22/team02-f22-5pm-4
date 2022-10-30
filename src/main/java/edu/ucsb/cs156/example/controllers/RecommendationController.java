package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Recommendation;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Api(description = "recommendations")
@RequestMapping("/api/recommendations")
@RestController
@Slf4j
public class RecommendationController extends ApiController {

    @Autowired
    RecommendationRepository recommendationRepository;

    @ApiOperation(value = "List all recommendations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Recommendation> allRecommendations() {
        Iterable<Recommendation> recommendations = recommendationRepository.findAll();
        return recommendations;
    }

public Recommendation postRecommendation(
    @ApiParam("id") @RequestParam Long id,
    @ApiParam("requesterEmail") @RequestParam String requesterEmail,
    @ApiParam("professorEmail") @RequestParam String professorEmail,
    @ApiParam("explanation") @RequestParam String explanation,
    @ApiParam("done") @RequestParam boolean done,
    @ApiParam("dateRequested (in iso format, e.g. YYYY-mm-dd; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateRequested") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested,
    @ApiParam("dateNeeded (in iso format, e.g. YYYY-mm-dd; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateNeeded") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateNeeded)
        
        throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

        Recommendation recommendation = new Recommendation();
        recommendation.setRequesterEmail(requesterEmail);
        recommendation.setProfessorEmail(professorEmail);
        recommendation.setExplanation(explanation);
        recommendation.setDone(done);
        recommendation.setDateRequested(dateRequested);
        recommendation.setDateNeeded(dateNeeded);

        Recommendation saveRecommendation = recommendationRepository.save(recommendation);

        return recommendation;
    }

    @ApiOperation(value = "Get a single recommendation")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Recommendation getById(
        @ApiParam("id") @RequestParam Long id,
        @RequestBody @Valid Recommendation incoming) {

        Recommendation recommendation = recommendationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Recommendation.class, id));

        return recommendation;
    }

    @ApiOperation(value = "Update a single recommendation")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Recommendation updateRecommendation(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid edu.ucsb.cs156.example.entities.Recommendation incoming) {

        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Recommendation.class, id));

        recommendation.setId(incoming.getId());
        recommendation.setDateNeeded(incoming.getDateNeeded());
        recommendation.setDateRequested(incoming.getDateRequested());
        recommendation.setDone(incoming.getDone());
        recommendation.setExplanation(incoming.getExplanation());
        recommendation.setProfessorEmail(incoming.getProfessorEmail());
        recommendation.setRequesterEmail(incoming.getRequesterEmail());
        recommendationRepository.save(recommendation);

        return recommendation;
    }

    @ApiOperation(value = "Delete a Recommendation")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRecommendation(
            @ApiParam("id") @RequestParam Long id) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Recommendation.class, id));

        recommendationRepository.delete(recommendation);
        return genericMessage("Recommendation with id %s deleted".formatted(id));
    }
}

package edu.ucsb.cs156.example.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.ucsb.cs156.example.controllers.ApiController;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import edu.ucsb.cs156.example.entities.Recommendation;
import edu.ucsb.cs156.example.repositories.RecommendationRepository;
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
    @ApiParam("explination") @RequestParam String explination,
    @ApiParam("done") @RequestParam boolean done,
    @ApiParam("dateRequested (in iso format, e.g. YYYY-mm-dd; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("localDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested,
    @ApiParam("date (in iso format, e.g. YYYY-mm-dd; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("localDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateNeeded)
        
        throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    edu.ucsb.cs156.example.entities.Recommendation recommendation = new Recommendation();
        recommendation.setID(id);
        recommendation.setRequesterEmail(requesterEmail);
        recommendation.setProfessorEmail(professorEmail);
        recommendation.setExplination(explination);
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
            @ApiParam("id") @RequestParam Long id) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Recommendation.class, id));

        return recommendation;
    }

}

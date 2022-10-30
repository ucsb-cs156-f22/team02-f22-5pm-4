package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Recommendation;
import edu.ucsb.cs156.example.repositories.RecommendationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@WebMvcTest(controllers = RecommendationController.class)
@Import(TestConfig.class)
public class RecommendationControllerTests extends ControllerTestCase {

        @MockBean
        RecommendationRepository recommendationRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/recommendation/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/Recommendation/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/Recommendation/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/Recommendation?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/recommendation/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/Recommendation/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/Recommendation/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime dateRequested = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime dateNeeded = LocalDateTime.parse("2022-04-15T00:00:00");

                Recommendation recommendation = Recommendation.builder()
                                .requesterEmail("requester@ucsb.edu")
                                .professorEmail("professor@ucsb.edu")
                                .explanation("explanation reason")
                                .dateRequested(dateRequested)
                                .dateNeeded(dateNeeded)
                                .done(true)
                                .build();

                when(recommendationRepository.findById(eq(7L))).thenReturn(Optional.of(recommendation));

                // act
                MvcResult response = mockMvc.perform(get("/api/Recommendation?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(recommendation);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(recommendationRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/Recommendation?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(recommendationRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Recommendation with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_recommendations() throws Exception {

                // arrange
                LocalDateTime dateRequested = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime dateNeeded = LocalDateTime.parse("2022-04-15T00:00:00");
                LocalDateTime dateRequested2 = LocalDateTime.parse("2022-01-20T00:00:00");
                LocalDateTime dateNeeded2 = LocalDateTime.parse("2022-07-19T00:00:00");

                Recommendation recommendation1 = Recommendation.builder()
                                        .requesterEmail("requester@ucsb.edu")
                                        .professorEmail("professor@ucsb.edu")
                                        .explanation("reason")
                                        .dateRequested(dateRequested)
                                        .dateNeeded(dateNeeded)
                                        .done(true)
                                .build();

                Recommendation recommendation2 = Recommendation.builder()
                                        .requesterEmail("requester@ucsb.edu")
                                        .professorEmail("professor@ucsb.edu")
                                        .explanation("reason updated")
                                        .dateRequested(dateRequested2)
                                        .dateNeeded(dateNeeded2)
                                        .done(true)
                                .build();

                ArrayList<Recommendation> expectedRecommendations = new ArrayList<>();
                expectedRecommendations.addAll(Arrays.asList(recommendation1, recommendation2));

                when(recommendationRepository.findAll()).thenReturn(expectedRecommendations);

                // act
                MvcResult response = mockMvc.perform(get("/api/Recommendation/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRecommendations);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_recommendation() throws Exception {
                // arrange
                LocalDateTime dRequested = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime dNeeded = LocalDateTime.parse("2022-04-15T00:00:00");


                Recommendation recommendation1 = Recommendation.builder()
                                        .requesterEmail("requester@ucsb.edu")
                                        .professorEmail("professor@ucsb.edu")
                                        .explanation("reason")
                                        .dateRequested(dRequested)
                                        .dateNeeded(dNeeded)
                                        .done(true)
                                .build();

                when(recommendationRepository.save(eq(recommendation1))).thenReturn(recommendation1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/Recommendation/post?requesterEmail=requester@ucsb.edu&professorEmail=professor@ucsb.edu&explanation=reason&dateRequested=2022-03-11T00:00:00&dateNeeded=2022-04-15T00:00:00&done=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationRepository, times(1)).save(recommendation1);
                String expectedJson = mapper.writeValueAsString(recommendation1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_recommendation() throws Exception {

                LocalDateTime dateRequested = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime dateNeeded = LocalDateTime.parse("2022-04-15T00:00:00");

                Recommendation recommendation1 = Recommendation.builder()
                                        .requesterEmail("requester@ucsb.edu")
                                        .professorEmail("professor@ucsb.edu")
                                        .explanation("reason")
                                        .dateRequested(dateRequested)
                                        .dateNeeded(dateNeeded)
                                        .done(true)
                                .build();

                when(recommendationRepository.findById(eq(15L))).thenReturn(Optional.of(recommendation1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/Recommendation?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationRepository, times(1)).findById(15L);
                verify(recommendationRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Recommendation with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_recommendation_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(recommendationRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/Recommendation?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recommendationRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Recommendation with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_recommendation() throws Exception {
                // arrange
                LocalDateTime dateRequested = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime dateNeeded = LocalDateTime.parse("2022-04-15T00:00:00");
                LocalDateTime dateRequested2 = LocalDateTime.parse("2022-01-20T00:00:00");
                LocalDateTime dateNeeded2 = LocalDateTime.parse("2022-07-19T00:00:00");

                Recommendation recommendationOrig = Recommendation.builder()
                                        .requesterEmail("requester@ucsb.edu")
                                        .professorEmail("professor@ucsb.edu")
                                        .explanation("reason")
                                        .dateRequested(dateRequested)
                                        .dateNeeded(dateNeeded)
                                        .done(false)
                                .build();

                Recommendation recommendationEdited = Recommendation.builder()
                                        .requesterEmail("newRequester@ucsb.edu")
                                        .professorEmail("newProfessor@ucsb.edu")
                                        .explanation("newReason")
                                        .dateRequested(dateRequested2)
                                        .dateNeeded(dateNeeded2)
                                        .done(true)
                                .build();

                String requestBody = mapper.writeValueAsString(recommendationOrig);

                when(recommendationRepository.findById(eq(67L))).thenReturn(Optional.of(recommendationEdited));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/Recommendation?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(recommendationRepository, times(1)).findById(67L);
                verify(recommendationRepository, times(1)).save(recommendationOrig); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_recommendation_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime dateRequested = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime dateNeeded = LocalDateTime.parse("2022-04-15T00:00:00");

                Recommendation recommendationEdited = Recommendation.builder()
                                        .requesterEmail("requester@ucsb.edu")
                                        .professorEmail("professor@ucsb.edu")
                                        .explanation("reason")
                                        .dateRequested(dateRequested)
                                        .dateNeeded(dateNeeded)
                                        .done(true)
                                .build();

                String requestBody = mapper.writeValueAsString(recommendationEdited);

                when(recommendationRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/Recommendation?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(recommendationRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Recommendation with id 67 not found", json.get("message"));
        }
}

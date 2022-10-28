package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

        @MockBean
        UCSBOrganizationRepository ucsbOrganizationRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsborganization/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsborganization?code=carrillo"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsborganization/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBOrganization organization = UCSBOrganization.builder()
                                .orgCode("SKY")
                                .orgTranslationShort("SKYDIVING CLUB")
                                .orgTranslation("SKYDIVING CLUB AT UCSB")
                                .inactive(false);

                when(ucsbOrganizationRepository.findById(eq("SKY"))).thenReturn(Optional.of(organization));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgcode=SKY"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findById(eq("SKY"));
                String expectedJson = mapper.writeValueAsString(organization);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbOrganizationRepository.findById(eq("SKY"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgcode=SKY"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findById(eq("SKY"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganization with orgcode SKY not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborganization() throws Exception {

                // arrange

                UCSBDOrganization SKYDIVING = UCSBOrganization.builder()
                                .orgCode("SKY")
                                .hasSackMeal("SKYDIVING CLUB")
                                .hasTakeOutMeal("SKYDIVING CLUB AT UCSB")
                                .inactive(false);

                UCSBOrganization STUDENTLIFE = UCSBOrganization.builder() ////////// STOPPED HERE COME BACK HERE
                                .name("De La Guerra")
                                .code("de-la-guerra")
                                .hasSackMeal(false)
                                .hasTakeOutMeal(false)
                                .hasDiningCam(true)
                                .latitude(34.409811)
                                .longitude(-119.845026)
                                .build();

                ArrayList<UCSBDiningCommons> expectedCommons = new ArrayList<>();
                expectedCommons.addAll(Arrays.asList(carrillo, dlg));

                when(ucsbDiningCommonsRepository.findAll()).thenReturn(expectedCommons);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommons/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedCommons);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_commons() throws Exception {
                // arrange

                UCSBDiningCommons ortega = UCSBDiningCommons.builder()
                                .name("Ortega")
                                .code("ortega")
                                .hasSackMeal(true)
                                .hasTakeOutMeal(true)
                                .hasDiningCam(true)
                                .latitude(34.410987)
                                .longitude(-119.84709)
                                .build();

                when(ucsbDiningCommonsRepository.save(eq(ortega))).thenReturn(ortega);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsbdiningcommons/post?name=Ortega&code=ortega&hasSackMeal=true&hasTakeOutMeal=true&hasDiningCam=true&latitude=34.410987&longitude=-119.84709")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsRepository, times(1)).save(ortega);
                String expectedJson = mapper.writeValueAsString(ortega);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                UCSBOrganization SKY = UCSBOrganization.builder()
                                .orgCode("SKY")
                                .orgTranslationShort("SKYDIVING CLUB")
                                .orgTranslation("SKYDIVING CLUB AT UCSB")
                                .inactive(false);

                when(ucsbOrganizationRepository.findById(eq("SKY"))).thenReturn(Optional.of(SKY));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgcode=SKY")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("SKY");
                verify(ucsbOrganizationRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with orgCode SKY deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbOrganizationRepository.findById(eq("FRC"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgcode=FRC")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("FRC");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with orgCode FRC not found", json.get("message"));
        }
        //added the put tests
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_commons() throws Exception {
                // arrange

                UCSBOrganization studentLifeOrig = UCSBorganization.builder()
                                .orgcode("OSLI")
                                .orgTranslationShort("STUDENT LIFE")
                                .orgTranslation("OFFICE OF STUDENT LIFE")
                                .inactive(true);

                UCSBOrganization studentLifeEdited = UCSBOrganization.builder()
                                .orgcode("OSLI")
                                .orgTranslationShort("STUDENT LIFE")
                                .orgTranslation("OFFICE OF STUDENT LIFE")
                                .inactive(false);

                String requestBody = mapper.writeValueAsString(studentLifeEdited);

                when(ucsbOrganizationRepository.findById(eq("OSLI"))).thenReturn(Optional.of(studentLifeOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?code=OSLI")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("OSLI");
                verify(ucsbOrganizationRepository, times(1)).save(studentLifeEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {
                // arrange

                UCSBOrganization editedOrganization = UCSBOrganization.builder()
                                .orgCode("FRC")
                                .orgTranslationShort("FURRY CLUB")
                                .orgTranslation("FURRY CLUB AT UCSB")
                                .inactive(false);

                String requestBody = mapper.writeValueAsString(editedOrganization);

                when(ucsbOrganizationRepository.findById(eq("FRC"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?orgcode=FRC")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("FRC");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with orgCode FRC not found", json.get("message"));

        }
}

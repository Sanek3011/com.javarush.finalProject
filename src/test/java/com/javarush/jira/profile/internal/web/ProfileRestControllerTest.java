package com.javarush.jira.profile.internal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.profile.ContactTo;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileMapper;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
class ProfileRestControllerTest extends AbstractControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ProfileMapper profileMapper;


    @Test
    public void profileGetInfoWithoutAuthorizationTest() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }

    @Test
    public void profileGetInfoWithWrongAuthorizationTest() throws Exception {
        mockMvc.perform(get("/api/profile").with(httpBasic("admin", "madin")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }

    @Test
    public void profileGetInfoWithCorrectAuthorizationTest() throws Exception {
        mockMvc.perform(get("/api/profile").with(httpBasic("admin@gmail.com", "admin")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.mailNotifications").isArray())
                .andExpect(jsonPath("$.contacts").isArray());
    }

    @Test
    public void profilePutTestWithWrongManuallyInstalledId() throws Exception{
        ProfileTo profileTo = new ProfileTo(
                5L,
                Set.of("assigned", "deadline"),
                Set.of(new ContactTo("skype", "adminSkype"), new ContactTo("vk", "adminVk"))
        );
        mockMvc.perform(put("/api/profile").with(httpBasic("admin@gmail.com", "admin"))
                        .contentType("application/json;charset=UTF-8")
                        .content(objectMapper.writeValueAsString(profileTo)))
                .andExpect(status().is(422));


    }

    @Test
    public void profilePutSuccessTest() throws Exception {
        ProfileTo profileTo = new ProfileTo(
                2L,
                Set.of("assigned", "deadline"),
                Set.of(new ContactTo("skype", "adminSkype"), new ContactTo("vk", "adminVk"))
        );
        mockMvc.perform(put("/api/profile").with(httpBasic("admin@gmail.com", "admin"))
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(profileTo)))
                .andExpect(status().isNoContent());
        Profile profile = profileRepository.findById(2L).orElseThrow();

        ProfileTo actualProfile = profileMapper.toTo(profile);

        assertEquals(profileTo.getId(), actualProfile.getId());
        assertEquals(profileTo.getContacts(), actualProfile.getContacts());
        assertEquals(profileTo.getMailNotifications(), actualProfile.getMailNotifications());
    }



    @Test
    public void profilePutWithoutAuthorizationTest() throws Exception {
        mockMvc.perform(put("/api/profile").contentType("application/json;charset=UTF-8"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void profilePutWithWrongContentType() throws Exception {
        mockMvc.perform(put("/api/profile").with(httpBasic("admin@gmail.com", "admin"))
                        .contentType("application/pdf")
                        .content("sodfodsfo"))
                .andExpect(status().isUnsupportedMediaType());
    }


}
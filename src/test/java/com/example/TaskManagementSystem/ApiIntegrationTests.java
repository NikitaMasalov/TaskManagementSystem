package com.example.TaskManagementSystem;

import com.example.TaskManagementSystem.repository.TaskRepository;
import com.example.TaskManagementSystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired TaskRepository tasks;
    @Autowired UserRepository users;

    @Test
    void rejectsUnauthenticatedAndInvalidCredentials() throws Exception {
        mvc.perform(get("/task")).andExpect(status().isUnauthorized());
        mvc.perform(get("/user")).andExpect(status().isUnauthorized());
        mvc.perform(post("/task").contentType(MediaType.APPLICATION_JSON).content(task(null)))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/task").with(httpBasic("admin", "wrong"))).andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsConfiguredAdministrator() throws Exception {
        mvc.perform(get("/task").with(httpBasic("admin", "test-admin-password")))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    void unassignedTaskLifecycle() throws Exception {
        long id = createTask(null);
        mvc.perform(get("/task/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test task"))
                .andExpect(jsonPath("$.userId").isEmpty())
                .andExpect(jsonPath("$.user").doesNotExist());
        mvc.perform(put("/task/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated","description":"Changed","status":"DONE","userId":null}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DONE"));
        mvc.perform(get("/task/{id}", id)).andExpect(jsonPath("$.title").value("Updated"));
        mvc.perform(delete("/task/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/task/{id}", id)).andExpect(status().isNotFound());
        assertThat(tasks.findById(id)).isEmpty();
    }

    @Test
    @WithMockUser
    void assignedTaskFilteringReassignmentAndUserDeletion() throws Exception {
        long first = createUser("First", "+79991234567");
        long second = createUser("Second", "0123456789");
        long taskId = createTask(first);
        mvc.perform(get("/task").param("userId", String.valueOf(first)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(taskId))
                .andExpect(jsonPath("$[0].userId").value(first))
                .andExpect(jsonPath("$[0].user").doesNotExist());
        mvc.perform(get("/task").param("userId", String.valueOf(second)))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mvc.perform(delete("/user/{id}", first)).andExpect(status().isConflict());
        assertThat(tasks.findById(taskId)).isPresent();
        mvc.perform(put("/task/{id}", taskId).contentType(MediaType.APPLICATION_JSON).content(task(second)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(second));
        mvc.perform(delete("/user/{id}", first)).andExpect(status().isNoContent());
        mvc.perform(post("/task/{id}", taskId).contentType(MediaType.APPLICATION_JSON).content(task(null)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.userId").isEmpty());
        mvc.perform(delete("/user/{id}", second)).andExpect(status().isNoContent());
        mvc.perform(get("/task/{id}", taskId)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void userLifecycleNeverExposesPasswordAndHashesIt() throws Exception {
        long id = createUser("Alice", "0123456789");
        String stored = users.findById(id).orElseThrow().getPassword();
        assertThat(stored).isNotEqualTo("test-password-123");
        assertThat(Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8().matches("test-password-123", stored)).isTrue();
        mvc.perform(get("/user/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("0123456789"))
                .andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.tasks").doesNotExist());
        mvc.perform(get("/user")).andExpect(status().isOk()).andExpect(jsonPath("$[*].password").isEmpty());
        mvc.perform(put("/user/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"Alice Updated","phone":"+79991234567","password":"new-password-123"}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("Alice Updated"))
                .andExpect(jsonPath("$.password").doesNotExist());
        assertThat(Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()
                .matches("new-password-123", users.findById(id).orElseThrow().getPassword())).isTrue();
        mvc.perform(delete("/user/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/user/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void rejectsMissingResourcesWithoutCreatingTasks() throws Exception {
        long missing = Long.MAX_VALUE;
        long before = tasks.count();
        mvc.perform(post("/task").contentType(MediaType.APPLICATION_JSON).content(task(missing)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.detail").exists());
        assertThat(tasks.count()).isEqualTo(before);
        mvc.perform(get("/task/{id}", missing)).andExpect(status().isNotFound());
        mvc.perform(get("/task").param("userId", String.valueOf(missing))).andExpect(status().isNotFound());
        mvc.perform(put("/task/{id}", missing).contentType(MediaType.APPLICATION_JSON).content(task(null)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/task/{id}", missing)).andExpect(status().isNotFound());
        mvc.perform(get("/user/{id}", missing)).andExpect(status().isNotFound());
        mvc.perform(put("/user/{id}", missing).contentType(MediaType.APPLICATION_JSON)
                .content(user("Missing", "+79991234567"))).andExpect(status().isNotFound());
        mvc.perform(delete("/user/{id}", missing)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void invalidTaskRequestsDoNotWriteData() throws Exception {
        long before = tasks.count();
        for (String body : new String[]{"{}", "null", "{", task(-1L),
                task(null).replace("Test task", " "), task(null).replace("Test task", "x".repeat(51)),
                task(null).replace("Description", "x".repeat(256)), task(null).replace("NEW", "")}) {
            mvc.perform(post("/task").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/task").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.errors.title").exists());
        assertThat(tasks.count()).isEqualTo(before);
        mvc.perform(get("/task/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/task/not-a-number")).andExpect(status().isBadRequest());
        mvc.perform(get("/task").param("userId", "-1")).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void invalidUserRequestsDoNotWriteData() throws Exception {
        long before = users.count();
        for (String body : new String[]{"{}", user(" ", "+79991234567"), user("Alice", "abc"),
                user("Alice", "123"), user("x".repeat(101), "+79991234567"),
                user("Alice", "+79991234567").replace("test-password-123", "short")}) {
            mvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        assertThat(users.count()).isEqualTo(before);
    }

    private long createTask(Long userId) throws Exception {
        var result = mvc.perform(post("/task").contentType(MediaType.APPLICATION_JSON).content(task(userId)))
                .andExpect(status().isCreated()).andExpect(header().exists("Location")).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createUser(String username, String phone) throws Exception {
        var result = mvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(user(username, phone)))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.password").doesNotExist()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String task(Long userId) {
        return "{\"title\":\"Test task\",\"description\":\"Description\",\"status\":\"NEW\",\"userId\":" + userId + "}";
    }

    private String user(String username, String phone) {
        return "{\"username\":\"" + username + "\",\"phone\":\"" + phone + "\",\"password\":\"test-password-123\"}";
    }
}

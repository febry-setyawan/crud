package com.example.crud.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.mock.mockito.MockBean;
import com.example.crud.feature.auth.service.JwtService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldHandleRuntimeException() throws Exception {
    mockMvc.perform(get("/test/runtime-exception"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("This is a test runtime exception"))
        .andExpect(jsonPath("$.path").value("/test/runtime-exception"));
    }

    @Test
    void shouldHandleResourceNotFoundException() throws Exception {
    mockMvc.perform(get("/test/resource-not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Test resource not found"))
        .andExpect(jsonPath("$.path").value("/test/resource-not-found"));
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() throws Exception {
        TestController.TestDto testDto = new TestController.TestDto(); // name is null

    mockMvc.perform(post("/test/validation-error")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Validation Failed"))
        .andExpect(jsonPath("$.message").value("{name=must not be blank}"))
        .andExpect(jsonPath("$.path").value("/test/validation-error"));
    }

    @Test
    void shouldReturnOkWhenDtoIsValid() throws Exception {
        TestController.TestDto testDto = new TestController.TestDto();
        testDto.setName("Valid Name");

        mockMvc.perform(post("/test/validation-error")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDto)))
                .andExpect(status().isOk());
    }
}

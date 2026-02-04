package ru.yandex.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.WebConfiguration;
import ru.yandex.controller.dto.GetPostsResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebConfiguration.class)
public class PostControllerTests {
    @Autowired
    private PostController postController;

    private static ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() {
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp() {
       mockMvc = MockMvcBuilders.standaloneSetup(postController).build();
    }

    @Test
    public void testGetSearch_byTag() throws Exception {

        // act
        var result = mockMvc.perform(get("/api/posts?search=%23Tag-for-1-3&pageNumber=1&pageSize=12"))
                .andExpect(status().isOk())
                .andReturn();

        // assert
        var response = result.getResponse();
        var json = response.getContentAsByteArray();
        var postsResponse = objectMapper.readValue(json, GetPostsResponse.class);

        assertNotNull(postsResponse);
        assertEquals(2, postsResponse.posts().size());
        assertEquals(1, postsResponse.posts().getFirst().id());
        assertEquals(3, postsResponse.posts().getLast().id());
    }

    @Test
    public void testGetSearch_byTitle() throws Exception {

        // act
        var result = mockMvc.perform(get("/api/posts?search=seco&pageNumber=1&pageSize=12"))
                .andExpect(status().isOk())
                .andReturn();

        // assert
        var response = result.getResponse();
        var json = response.getContentAsByteArray();
        var postsResponse = objectMapper.readValue(json, GetPostsResponse.class);

        assertNotNull(postsResponse);
        assertEquals(1, postsResponse.posts().size());
        assertEquals(2, postsResponse.posts().getFirst().id());
    }
}

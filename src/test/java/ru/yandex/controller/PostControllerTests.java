package ru.yandex.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.WebConfiguration;
import ru.yandex.controller.dto.AddPostRequest;
import ru.yandex.controller.dto.PostResponse;
import ru.yandex.controller.dto.GetPostsResponse;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        assertFalse(postsResponse.hasPrev());
        assertFalse(postsResponse.hasNext());
        assertEquals(1, postsResponse.lastPage());
    }

    @Test
    public void testGetSearch_byTitle_getSecondPost() throws Exception {

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
        assertFalse(postsResponse.hasPrev());
        assertFalse(postsResponse.hasNext());
        assertEquals(1, postsResponse.lastPage());
    }

    @Test
    public void testGetSearch_byTitle_getAllPosts() throws Exception {

        // act
        var result = mockMvc.perform(get("/api/posts?search=T&pageNumber=2&pageSize=2"))
                .andExpect(status().isOk())
                .andReturn();

        // assert
        var response = result.getResponse();
        var json = response.getContentAsByteArray();
        var postsResponse = objectMapper.readValue(json, GetPostsResponse.class);

        assertNotNull(postsResponse);
        assertEquals(2, postsResponse.posts().size());
        assertEquals(3, postsResponse.posts().getFirst().id());
        assertEquals(4, postsResponse.posts().getLast().id());
        assertTrue(postsResponse.hasPrev());
        assertFalse(postsResponse.hasNext());
        assertEquals(2, postsResponse.lastPage());
    }

    @Test
    public void testGetPost_byId_postExists() throws Exception {

        // act
        var result = mockMvc.perform(get("/api/posts/3"))
                .andExpect(status().isOk())
                .andReturn();

        // assert
        var response = result.getResponse();
        var json = response.getContentAsByteArray();
        var postResponse = objectMapper.readValue(json, PostResponse.class);

        assertNotNull(postResponse);
        assertEquals(3, postResponse.id());
        assertEquals("Title 3 third", postResponse.title());
        assertEquals("Text third text text 3", postResponse.text());
        assertEquals(11, postResponse.likesCount());
        assertEquals(1, postResponse.commentsCount());
        assertEquals(3, postResponse.tags().size());
        assertTrue(postResponse.tags().contains("Tag-for-3"));
        assertTrue(postResponse.tags().contains("Tag-for-1-3"));
        assertTrue(postResponse.tags().contains("Tag-for-all"));
    }

    @Test
    public void testGetPost_byId_postNotExists() throws Exception {

        // act
        var result = mockMvc.perform(get("/api/posts/0"))
                .andExpect(status().isOk())
                .andReturn();

        // assert
        var response = result.getResponse();
        var json = response.getContentAsByteArray();

        assertEquals(0, json.length);
    }

    @Test
    public void testAddPost() throws Exception {
        // arrange
        var commonTag = "Tag-for-all";
        var shareTag = "Tag-for-3";
        var newTag = "Tag-for-new-" + UUID.randomUUID();
        var title = "Заголовок " + UUID.randomUUID();
        var text = "Текст " + UUID.randomUUID() + " " + UUID.randomUUID();
        var request = new AddPostRequest(title, text, List.of(commonTag, newTag, shareTag));
        var requestJson = objectMapper.writeValueAsString(request);

        // act
        var result = mockMvc
                .perform(
                        post("/api/posts")
                                .contentType(MediaType.APPLICATION_JSON) // Set the content type
                                .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        // assert
        var response = result.getResponse();
        var json = response.getContentAsByteArray();
        var postResponse = objectMapper.readValue(json, PostResponse.class);

        assertNotNull(postResponse);
        assertTrue(postResponse.id() > 4);
        assertEquals(title, postResponse.title());
        assertEquals(text, postResponse.text());
        assertEquals(0, postResponse.likesCount());
        assertEquals(0, postResponse.commentsCount());
        assertEquals(3, postResponse.tags().size());
        assertTrue(postResponse.tags().contains(commonTag));
        assertTrue(postResponse.tags().contains(shareTag));
        assertTrue(postResponse.tags().contains(newTag));
    }
}

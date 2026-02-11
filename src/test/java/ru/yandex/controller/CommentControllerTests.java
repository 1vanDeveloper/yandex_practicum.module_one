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
import ru.yandex.controller.dto.*;
import ru.yandex.repository.CommentRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebConfiguration.class)
public class CommentControllerTests {
    @Autowired
    private CommentController commentController;
    @Autowired
    private CommentRepository commentRepository;

    private static ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeAll
    static void beforeAll() {
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
    }

    @Test
    public void testGetComments_success() throws Exception {

        // act
        var result = mockMvc.perform(get("/api/posts/{postId}/comments", 1))
                .andExpect(status().isOk())
                .andReturn();

        // assert
        var response = result.getResponse();
        var json = response.getContentAsByteArray();
        var commentsResponse = objectMapper.readValue(json, CommentsResponse.class);

        assertEquals(2, commentsResponse.size());
        assertEquals(new CommentResponse(1, "Comment 1 for post 1",1), commentsResponse.getFirst());
        assertEquals(new CommentResponse(2, "Comment 2 for post 1",1), commentsResponse.getLast());
    }

    @Test
    public void testGetComment_success() throws Exception {

        // act
        var result = mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", 1, 2))
                .andExpect(status().isOk())
                .andReturn();

        // assert
        var response = result.getResponse();
        var json = response.getContentAsByteArray();
        var commentResponse = objectMapper.readValue(json, CommentResponse.class);

        assertEquals(new CommentResponse(2, "Comment 2 for post 1",1), commentResponse);
    }

    @Test
    public void testAddComment_success() throws Exception {
        // arrange
        var request = new AddCommentRequest("New text", 2);
        var requestJson = objectMapper.writeValueAsString(request);

        // act
        var result = mockMvc.perform(post("/api/posts/{postId}/comments", 2)
                        .contentType(MediaType.APPLICATION_JSON) // Set the content type
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        // assert
        var response = result.getResponse();
        var json = response.getContentAsByteArray();
        var commentResponse = objectMapper.readValue(json, CommentResponse.class);

        assertEquals("New text", commentResponse.text());
        assertEquals(2, commentResponse.postId());
        assertTrue(commentResponse.id() > 0);

        commentRepository.deleteComment(commentResponse.id());
    }

    @Test
    public void testUpdateComment_success() throws Exception {
        // arrange
        var postId = 2;
        var comment = commentRepository.addComment(postId, "New comment").get();
        var editRequest = new UpdateCommentRequest(comment.getId(), "New edited text", postId);
        var editRequestJson = objectMapper.writeValueAsString(editRequest);

        // act
        var result = mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", postId, comment.getId())
                        .contentType(MediaType.APPLICATION_JSON) // Set the content type
                        .content(editRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        // assert
        var response = result.getResponse();
        var json = response.getContentAsByteArray();
        var commentResponse = objectMapper.readValue(json, CommentResponse.class);

        assertEquals("New edited text", commentResponse.text());
        assertEquals(comment.getPostId(), commentResponse.postId());
        assertEquals(comment.getId(), commentResponse.id());

        commentRepository.deleteComment(comment.getId());
    }

    @Test
    public void testDeleteComment_success() throws Exception {
        // arrange
        var postId = 2;
        var comment = commentRepository.addComment(postId, "New comment").get();

        // act
        var result = mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, comment.getId()))
                .andExpect(status().isOk())
                .andReturn();

        // assert
        var deletedComment = commentRepository.getComment(comment.getId()).get();
        assertNull(deletedComment);
    }
}

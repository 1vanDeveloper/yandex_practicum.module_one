package ru.yandex.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.WebConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebConfiguration.class)
public class ImageControllerTests {

    @Autowired
    private ImageController imageController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageController).build();
    }

    @Test
    void testUploadImage_emptyFile_badRequest() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("image", "empty.png", "image/png", new byte[0]);

        var result = mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", 1L).file(empty))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("empty file"));
    }

    @Test
    void testUploadImage_emptyFileName_badRequest() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("image", "", "image/png", new byte[]{1, 2, 3});

        var result = mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", 1L).file(empty))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("empty file name"));
    }

    @Test
    void testUploadImage_emptyFileNameExtension_badRequest() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("image", "empty", "image/png", new byte[]{1, 2, 3});

        var result = mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", 1L).file(empty))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("file name has not extension"));
    }

    @Test
    void testGetImage_imageNotFound_400() throws Exception {
        var result = mockMvc.perform(get("/api/posts/10000000/image"))
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUploadAndGetImage_success() throws Exception {
        var pngStub = new byte[]{(byte) 137, 80, 78, 71};
        var fileName = "post_image.png";
        MockMultipartFile file = new MockMultipartFile("image", fileName, "image/png", pngStub);

        var putResult = mockMvc.perform(multipart(HttpMethod.PUT,"/api/posts/{id}/image", 1L).file(file))
                .andReturn();

        mockMvc.perform(asyncDispatch(putResult))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        var getResult = mockMvc.perform(get("/api/posts/{id}/image", 1L))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(getResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\""))
                .andExpect(content().bytes(pngStub));
    }
}

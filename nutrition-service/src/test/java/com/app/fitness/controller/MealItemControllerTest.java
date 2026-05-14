package com.app.fitness.controller;

import com.app.fitness.dto.MealItemRequest;
import com.app.fitness.dto.MealItemResponse;
import com.app.fitness.dto.PageResponse;
import com.app.fitness.exception.DuplicateResourceException;
import com.app.fitness.exception.ResourceNotFoundException;
import com.app.fitness.service.MealItemService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MealItemControllerTest extends ControllerTestSupport {

    @Mock
    private MealItemService mealItemService;

    @InjectMocks
    private MealItemController mealItemController;

    @BeforeEach
    void setUp() {
        setUpMockMvc(mealItemController);
    }

    @Test
    void getAll_shouldReturnPage() throws Exception {
        List<MealItemResponse> items = List.of(new MealItemResponse(
                1L, 2L, "Chicken Breast", new BigDecimal("200"), new BigDecimal("330"),
                new BigDecimal("62"), new BigDecimal("0"), new BigDecimal("7")));
        when(mealItemService.findAll(any(Pageable.class)))
                .thenReturn(PageResponse.of(new PageImpl<>(items, PageRequest.of(0, 20), 1)));

        mockMvc.perform(get("/api/meal-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].foodName").value("Chicken Breast"));
    }

    @Test
    void getById_whenItemExists_shouldReturnItem() throws Exception {
        when(mealItemService.findById(1L)).thenReturn(new MealItemResponse(
                1L, 2L, "Chicken Breast", new BigDecimal("200"), new BigDecimal("330"),
                new BigDecimal("62"), new BigDecimal("0"), new BigDecimal("7")));

        mockMvc.perform(get("/api/meal-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.foodName").value("Chicken Breast"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(mealItemService.findById(99L)).thenThrow(
                new ResourceNotFoundException("Meal item not found with id: 99"));

        mockMvc.perform(get("/api/meal-items/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        MealItemRequest request = new MealItemRequest(2L, "Rice", new BigDecimal("150"), new BigDecimal("190"),
                new BigDecimal("4"), new BigDecimal("41"), new BigDecimal("1"));
        when(mealItemService.create(any(MealItemRequest.class))).thenReturn(new MealItemResponse(
                3L, 2L, "Rice", new BigDecimal("150"), new BigDecimal("190"),
                new BigDecimal("4"), new BigDecimal("41"), new BigDecimal("1")));

        mockMvc.perform(post("/api/meal-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.foodName").value("Rice"));
    }

    @Test
    void create_withInvalidRequest_shouldReturn400() throws Exception {
        MealItemRequest request = new MealItemRequest(null, "", new BigDecimal("-1"), null, null, null, null);

        mockMvc.perform(post("/api/meal-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void create_whenDuplicate_shouldReturn409() throws Exception {
        MealItemRequest request = new MealItemRequest(2L, "Rice", new BigDecimal("150"), new BigDecimal("190"),
                new BigDecimal("4"), new BigDecimal("41"), new BigDecimal("1"));
        when(mealItemService.create(any(MealItemRequest.class)))
                .thenThrow(new DuplicateResourceException("Food item 'Rice' already exists in this meal log"));

        mockMvc.perform(post("/api/meal-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void update_withValidRequest_shouldReturn200() throws Exception {
        MealItemRequest request = new MealItemRequest(2L, "Brown Rice", new BigDecimal("150"), new BigDecimal("180"),
                new BigDecimal("4"), new BigDecimal("37"), new BigDecimal("1"));
        when(mealItemService.update(eq(1L), any(MealItemRequest.class))).thenReturn(new MealItemResponse(
                1L, 2L, "Brown Rice", new BigDecimal("150"), new BigDecimal("180"),
                new BigDecimal("4"), new BigDecimal("37"), new BigDecimal("1")));

        mockMvc.perform(put("/api/meal-items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foodName").value("Brown Rice"));
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        MealItemRequest request = new MealItemRequest(2L, "Brown Rice", new BigDecimal("150"), new BigDecimal("180"),
                new BigDecimal("4"), new BigDecimal("37"), new BigDecimal("1"));
        when(mealItemService.update(eq(99L), any(MealItemRequest.class)))
                .thenThrow(new ResourceNotFoundException("Meal item not found with id: 99"));

        mockMvc.perform(put("/api/meal-items/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/meal-items/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Meal item not found with id: 99"))
                .when(mealItemService).delete(99L);

        mockMvc.perform(delete("/api/meal-items/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}

package in.project.category.service;

import in.project.category.dto.CategoryDTO;
import in.project.category.entity.CategoryEntity;
import in.project.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private static final Long PROFILE_ID = 100L;
    private CategoryDTO categoryDTO;
    private CategoryEntity categoryEntity;

    @BeforeEach
    void setUp() {
        categoryDTO = CategoryDTO.builder()
                .id(1L)
                .name("Groceries")
                .icon("cart-icon")
                .type("EXPENSE")
                .build();

        categoryEntity = CategoryEntity.builder()
                .id(1L)
                .profileId(PROFILE_ID)
                .name("Groceries")
                .icon("cart-icon")
                .type("EXPENSE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void saveCategory_Success() {
        // Assert mock returns false for existence check
        when(categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), PROFILE_ID)).thenReturn(false);
        // Assert save returns the entity
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(categoryEntity);

        CategoryDTO savedDTO = categoryService.saveCategory(categoryDTO, PROFILE_ID);

        assertNotNull(savedDTO);
        assertEquals("Groceries", savedDTO.getName());
        assertEquals("EXPENSE", savedDTO.getType());
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
    }

    @Test
    void saveCategory_AlreadyExists_ThrowsException() {
        // Force the check to return true (category already exists)
        when(categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), PROFILE_ID)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.saveCategory(categoryDTO, PROFILE_ID);
        });

        assertEquals("Category with this name already exists ", exception.getMessage());
        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    void getCategoriesForCurrentUser_ReturnsList() {
        when(categoryRepository.findByProfileId(PROFILE_ID)).thenReturn(List.of(categoryEntity));

        List<CategoryDTO> categories = categoryService.getCategoriesForCurrentUser(PROFILE_ID);

        assertNotNull(categories);
        assertEquals(1, categories.size());
        assertEquals("Groceries", categories.get(0).getName());
        verify(categoryRepository, times(1)).findByProfileId(PROFILE_ID);
    }

    @Test
    void getCategoriesByTypeForCurrentUser_ReturnsFilteredList() {
        when(categoryRepository.findByTypeAndProfileId("EXPENSE", PROFILE_ID)).thenReturn(List.of(categoryEntity));

        List<CategoryDTO> categories = categoryService.getCategoriesByTypeForCurrentUser("EXPENSE", PROFILE_ID);

        assertNotNull(categories);
        assertEquals(1, categories.size());
        assertEquals("EXPENSE", categories.get(0).getType());
        verify(categoryRepository, times(1)).findByTypeAndProfileId("EXPENSE", PROFILE_ID);
    }

    @Test
    void updateCategory_Success() {
        CategoryDTO updateRequest = CategoryDTO.builder()
                .name("Supermarket")
                .icon("store-icon")
                .build();

        // Ensure find returns true
        when(categoryRepository.findByIdAndProfileId(1L, PROFILE_ID)).thenReturn(Optional.of(categoryEntity));
        // Mock save
        when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryDTO updatedOutput = categoryService.updateCategory(1L, updateRequest, PROFILE_ID);

        assertNotNull(updatedOutput);
        assertEquals("Supermarket", updatedOutput.getName());
        assertEquals("store-icon", updatedOutput.getIcon());
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
    }

    @Test
    void updateCategory_NotFound_ThrowsException() {
        CategoryDTO updateRequest = CategoryDTO.builder().name("Missing").build();

        // Ensure find returns empty
        when(categoryRepository.findByIdAndProfileId(99L, PROFILE_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.updateCategory(99L, updateRequest, PROFILE_ID);
        });

        assertEquals("Category not found or not accessible", exception.getMessage());
        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }
}

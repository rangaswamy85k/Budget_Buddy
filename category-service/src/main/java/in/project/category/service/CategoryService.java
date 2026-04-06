package in.project.category.service;

import in.project.category.dto.CategoryDTO;
import in.project.category.entity.CategoryEntity;
import in.project.category.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryDTO saveCategory(CategoryDTO categoryDTO, Long profileId){
        if(categoryRepository.existsByNameAndProfileId(categoryDTO.getName(),profileId)){
            throw new RuntimeException("Category with this name already exists ");
        }

            CategoryEntity newCategory = toEntity(categoryDTO,profileId);
            newCategory = categoryRepository.save(newCategory);
        return toDto(newCategory);
    }

    public List<CategoryDTO> getCategoriesForCurrentUser(Long profileId){
            List<CategoryEntity> categories = categoryRepository.findByProfileId(profileId);
        return categories.stream().map(this::toDto).toList();

    }

         public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type, Long profileId){
            List<CategoryEntity> entities =  categoryRepository.findByTypeAndProfileId(type, profileId);
         return entities.stream().map(this::toDto).toList();
    }


    public CategoryDTO updateCategory(Long categoryId, CategoryDTO dto, Long profileId){
       CategoryEntity existingCategory =  categoryRepository.findByIdAndProfileId(categoryId, profileId)
                    .orElseThrow(()-> new RuntimeException("Category not found or not accessible"));

            existingCategory.setName(dto.getName());
            existingCategory.setIcon(dto.getIcon());
            existingCategory = categoryRepository.save(existingCategory);
       return toDto(existingCategory);
    }




    //Helper Method to convert dto to entity
    private CategoryEntity toEntity(CategoryDTO categoryDTO, Long profileId)
    {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .profileId(profileId)
                .type(categoryDTO.getType())
                .build();
    }

    private CategoryDTO toDto(CategoryEntity entity){
        return CategoryDTO.builder()
                .id(entity.getId())
                .userId(entity.getProfileId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .type(entity.getType())
                .build();
    }
}

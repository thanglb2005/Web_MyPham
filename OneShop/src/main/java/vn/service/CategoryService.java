package vn.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();
    Page<Category> getAllCategoriesPaged(Pageable pageable);
    Page<Category> searchCategories(String keyword, Pageable pageable);
    Optional<Category> getCategoryById(Long id);
    Category saveCategory(Category category);
    void deleteCategory(Long id);
    Category findByCategoryName(String categoryName);
}

package com.eventhub.service;

import com.eventhub.dto.CategoryRequest;
import com.eventhub.dto.CategoryResponse;
import com.eventhub.entity.Category;
import com.eventhub.repository.CategoryRepository;
import com.eventhub.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public List<CategoryResponse> list() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) throw new IllegalArgumentException("Category already exists");
        Category c = Category.builder().name(name).description(request.description()).active(request.active() == null || request.active()).build();
        return CategoryResponse.from(categoryRepository.save(c));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category c = get(id);
        String name = request.name().trim();
        categoryRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) throw new IllegalArgumentException("Category already exists");
        });
        c.setName(name);
        c.setDescription(request.description());
        if (request.active() != null) c.setActive(request.active());
        return CategoryResponse.from(categoryRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        if (eventRepository.existsByCategoryId(id)) throw new IllegalArgumentException("Category is used by an event and cannot be deleted");
        categoryRepository.deleteById(id);
    }

    public Category get(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }
}

package com.example.demo.service;

import com.example.demo.model.ProfileType;
import com.example.demo.model.Template;
import com.example.demo.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;

    @Transactional
    public Template createTemplate(Template template) {
        return templateRepository.save(template);
    }

    @Transactional
    public Template updateTemplate(Long id, Template templateDetails) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));

        template.setName(templateDetails.getName());
        template.setDescription(templateDetails.getDescription());
        template.setHtmlContent(templateDetails.getHtmlContent());
        template.setCssContent(templateDetails.getCssContent());
        template.setDefault(templateDetails.isDefault());
        template.setProfileType(templateDetails.getProfileType());

        return templateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new RuntimeException("Template not found with id: " + id);
        }
        templateRepository.deleteById(id);
    }

    public Optional<Template> getTemplateById(Long id) {
        return templateRepository.findById(id);
    }

    public Optional<Template> getTemplateByName(String name) {
        return templateRepository.findByName(name);
    }

    public List<Template> getAllTemplates() {
        return templateRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Template> getTemplatesByProfileType(ProfileType profileType) {
        return templateRepository.findByProfileType(profileType);
    }

    public List<Template> searchTemplates(String searchTerm) {
        return templateRepository.searchTemplates(searchTerm);
    }

    public Optional<Template> getDefaultTemplate() {
        return templateRepository.findByIsDefaultTrue();
    }

    @Transactional
    public void setDefaultTemplate(Long id) {
        // Remove default from all templates
        List<Template> allTemplates = templateRepository.findAll();
        allTemplates.forEach(t -> t.setDefault(false));
        templateRepository.saveAll(allTemplates);

        // Set the new default
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));
        template.setDefault(true);
        templateRepository.save(template);
    }
}
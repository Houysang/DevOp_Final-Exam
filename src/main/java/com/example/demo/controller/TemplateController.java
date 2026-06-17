package com.example.demo.controller;

import com.example.demo.model.ProfileType;
import com.example.demo.model.Template;
import com.example.demo.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    // ========== REST API ==========

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Template>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Template> getTemplateById(@PathVariable Long id) {
        return templateService.getTemplateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Template> createTemplate(@RequestBody Template template) {
        return ResponseEntity.ok(templateService.createTemplate(template));
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Template> updateTemplate(@PathVariable Long id, @RequestBody Template template) {
        return ResponseEntity.ok(templateService.updateTemplate(id, template));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<Template>> searchTemplates(@RequestParam("q") String searchTerm) {
        return ResponseEntity.ok(templateService.searchTemplates(searchTerm));
    }

    @GetMapping("/type/{profileType}")
    @ResponseBody
    public ResponseEntity<List<Template>> getTemplatesByType(@PathVariable ProfileType profileType) {
        return ResponseEntity.ok(templateService.getTemplatesByProfileType(profileType));
    }

    @GetMapping("/default")
    @ResponseBody
    public ResponseEntity<Template> getDefaultTemplate() {
        return templateService.getDefaultTemplate()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/set-default")
    @ResponseBody
    public ResponseEntity<Void> setDefaultTemplate(@PathVariable Long id) {
        templateService.setDefaultTemplate(id);
        return ResponseEntity.ok().build();
    }

    // ========== THYMELEAF VIEWS ==========

    @GetMapping("/view/all")
    public String viewAllTemplates(Model model) {
        model.addAttribute("templates", templateService.getAllTemplates());
        model.addAttribute("profileTypes", ProfileType.values());
        return "templates/list";
    }

    @GetMapping("/view/create")
    public String showCreateForm(Model model) {
        model.addAttribute("template", new Template());
        model.addAttribute("profileTypes", ProfileType.values());
        return "templates/create";
    }

    @PostMapping("/view/create")
    public String createTemplateFromForm(@ModelAttribute Template template) {
        templateService.createTemplate(template);
        return "redirect:/api/templates/view/all";
    }

    @GetMapping("/view/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        return templateService.getTemplateById(id)
                .map(template -> {
                    model.addAttribute("template", template);
                    model.addAttribute("profileTypes", ProfileType.values());
                    return "templates/edit";
                })
                .orElse("redirect:/api/templates/view/all");
    }

    @PostMapping("/view/edit/{id}")
    public String updateTemplateFromForm(@PathVariable Long id, @ModelAttribute Template template) {
        templateService.updateTemplate(id, template);
        return "redirect:/api/templates/view/all";
    }

    @PostMapping("/view/delete/{id}")
    public String deleteTemplateFromForm(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return "redirect:/api/templates/view/all";
    }
}
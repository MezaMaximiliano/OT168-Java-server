package com.alkemy.ong.web.controllers;

import com.alkemy.ong.domain.testimonial.Testimonial;
import com.alkemy.ong.domain.testimonial.TestimonialPage;
import com.alkemy.ong.domain.testimonial.TestimonialService;
import com.alkemy.ong.web.utils.WebUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/testimonials")
public class TestimonialController {

    private final TestimonialService testimonialService;

    public TestimonialController(TestimonialService testimonialService) {
        this.testimonialService = testimonialService;
    }

    @PostMapping
    public ResponseEntity<TestimonialDTO> createTestimonial(@Valid @RequestBody TestimonialDTO testimonialDTO) {
        return new ResponseEntity<>(toDto(testimonialService.save(toModel(testimonialDTO))), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestimonialDTO> updateTestimonial(@PathVariable("id") Long id, @Valid @RequestBody TestimonialDTO testimonialDTO) {
        WebUtils.validateDtoIdWithBodyId(id, testimonialDTO.getId());
        return new ResponseEntity<>(toDto(testimonialService.update(id, toModel(testimonialDTO))), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        testimonialService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public ResponseEntity findAll(@RequestParam("page") Integer page) {
        return new ResponseEntity<>(toPageDto(testimonialService.findAll(page)), HttpStatus.OK);
    }

    private Testimonial toModel(TestimonialDTO testimonialDTO) {
        return Testimonial.builder()
                .id(testimonialDTO.getId())
                .name(testimonialDTO.getName())
                .image(testimonialDTO.getImage())
                .content(testimonialDTO.getContent())
                .createdAt(testimonialDTO.getCreatedAt())
                .updatedAt(testimonialDTO.getUpdatedAt())
                .deleted(testimonialDTO.getDeleted())
                .build();
    }

    private TestimonialDTO toDto(Testimonial testimonial) {
        return TestimonialDTO.builder()
                .id(testimonial.getId())
                .name(testimonial.getName())
                .image(testimonial.getImage())
                .content(testimonial.getContent())
                .createdAt(testimonial.getCreatedAt())
                .updatedAt(testimonial.getUpdatedAt())
                .deleted(testimonial.getDeleted())
                .build();
    }

    private List<TestimonialDTO> toDTOList(List<Testimonial> testimonialList) {
        return testimonialList.stream().map(testimonial -> toDto(testimonial)).collect(Collectors.toList());
    }

    private TestimonialPageDTO toPageDto(TestimonialPage testimonialPage) {
        TestimonialPageDTO testimonialPageDTO = new TestimonialPageDTO();
        testimonialPageDTO.setPreviousPage(testimonialPage.getPreviousPage());
        testimonialPageDTO.setNextPage(testimonialPage.getNextPage());
        testimonialPageDTO.setTestimonialDtoList(toDTOList(testimonialPage.getTestimonialList()));
        return testimonialPageDTO;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TestimonialDTO {
        private Long id;

        @NotNull(message = "Field 'name' is required.")
        private String name;

        private String image;

        @NotNull(message = "Field 'content' is required.")
        private String content;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        private Boolean deleted;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TestimonialPageDTO {

        @JsonProperty("testimonialList")
        private List<TestimonialDTO> testimonialDtoList;

        private String previousPage;

        private String nextPage;

        public void setPreviousPage(String previousPage) {
            if (previousPage != null) {
                this.previousPage = "/testimonials?page=" + previousPage;
            } else {
                this.previousPage = "";
            }
        }

        public void setNextPage(String nextPage) {
            if (nextPage != null) {
                this.nextPage = "/testimonials?page=" + nextPage;
            } else {
                this.nextPage = "";
            }
        }
    }
}

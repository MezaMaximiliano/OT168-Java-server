package com.alkemy.ong.web.controllers;

import com.alkemy.ong.domain.activity.Activity;
import com.alkemy.ong.domain.activity.ActivityService;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService){
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<ActivityDTO> saveActivity(@Valid @RequestBody ActivityDTO activityDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(activityService.saveActivity(toModel(activityDTO))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityDTO> updateActivity(@PathVariable Long id, @Valid @RequestBody ActivityDTO activityDTO){
        return ResponseEntity.ok(toDTO(activityService.updateActivity(id, toModel(activityDTO))));
    }

    private Activity toModel(ActivityDTO activityDTO){
        return Activity.builder()
                .name(activityDTO.getName())
                .content(activityDTO.getContent())
                .image(activityDTO.getImage())
                .createdAt(activityDTO.getCreatedAt())
                .updatedAt(activityDTO.getUpdatedAt())
                .deleted(activityDTO.getDeleted())
                .build();
    }

    private ActivityDTO toDTO(Activity activity){
        return ActivityDTO.builder()
                .id(activity.getId())
                .name(activity.getName())
                .image(activity.getImage())
                .content(activity.getContent())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .deleted(activity.getDeleted())
                .build();
    }
}

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
class ActivityDTO {
    private Long id;
    @NotEmpty(message = "Name can't be empty")
    private String name;
    @NotEmpty(message = "Content can't be empty")
    private String content;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
}

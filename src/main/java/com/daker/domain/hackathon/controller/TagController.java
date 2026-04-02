package com.daker.domain.hackathon.controller;

import com.daker.domain.hackathon.repository.TagRepository;
import com.daker.domain.hackathon.domain.Tag;
import com.daker.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagRepository tagRepository;

    @GetMapping
    public ApiResponse<List<TagResponse>> getTags() {
        List<TagResponse> tags = tagRepository.findAll().stream()
                .map(TagResponse::new)
                .toList();
        return ApiResponse.ok(tags);
    }

    public record TagResponse(Long tagId, String name) {
        public TagResponse(Tag tag) {
            this(tag.getId(), tag.getName());
        }
    }
}

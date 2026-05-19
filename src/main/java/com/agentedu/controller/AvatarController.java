package com.agentedu.controller;

import com.agentedu.common.Result;
import com.agentedu.service.AvatarCatalogService;
import com.agentedu.vo.avatar.CharacterAvatarVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/avatars")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarCatalogService avatarCatalogService;

    /**
     * Unified avatarModels endpoint for the student AI tutor.
     *
     * The response follows AIRI's character.avatarModels[] idea instead of the
     * old Live2D-only config contract.
     */
    @GetMapping("/student-tutor")
    public Result<CharacterAvatarVO> studentTutor() {
        return Result.success(avatarCatalogService.getStudentTutorCharacter());
    }
}

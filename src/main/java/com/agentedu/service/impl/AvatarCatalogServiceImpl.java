package com.agentedu.service.impl;

import com.agentedu.service.AvatarCatalogService;
import com.agentedu.vo.avatar.AvatarLayoutVO;
import com.agentedu.vo.avatar.AvatarModelSourceVO;
import com.agentedu.vo.avatar.AvatarModelVO;
import com.agentedu.vo.avatar.AvatarStateVO;
import com.agentedu.vo.avatar.CharacterAvatarVO;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AvatarCatalogServiceImpl implements AvatarCatalogService {

    @Override
    public CharacterAvatarVO getStudentTutorCharacter() {
        return CharacterAvatarVO.builder()
                .id("student-ai-tutor")
                .name("AI 学习助教")
                .description("继承 AIRI avatarModels 思路的学生问答助教形象配置。")
                .defaultAvatarModelId("airi-local-live2d")
                .avatarModels(List.of(
                        localAiriLive2d(),
                        hiyoriPro(),
                        hiyoriFree(),
                        avatarSampleA(),
                        avatarSampleB()
                ))
                .build();
    }

    private AvatarModelVO localAiriLive2d() {
        return AvatarModelVO.builder()
                .id("airi-local-live2d")
                .name("AIRI 学习助教")
                .type("live2d")
                .description("项目本地 Live2D 资源，使用 avatarModels.config.live2d.urls 承载，不再使用旧 Live2D 专用接口。")
                .config(Map.of("live2d", AvatarModelSourceVO.builder()
                        .urls(List.of("/live2d/airi/model/airi.model3.json"))
                        .build()))
                .layout(defaultLayout(0.28))
                .states(defaultStates())
                .build();
    }

    private AvatarModelVO hiyoriPro() {
        return AvatarModelVO.builder()
                .id("preset-live2d-hiyori-pro")
                .name("Hiyori (Pro)")
                .type("live2d")
                .description("AIRI 内置 Live2D Pro 形象来源。zip 资源会作为候选保留，当前渲染器优先支持 model3.json。")
                .config(Map.of("live2d", AvatarModelSourceVO.builder()
                        .urls(List.of("https://dist.ayaka.moe/live2d-models/hiyori_pro_zh.zip"))
                        .build()))
                .layout(defaultLayout(0.28))
                .states(defaultStates())
                .build();
    }

    private AvatarModelVO hiyoriFree() {
        return AvatarModelVO.builder()
                .id("preset-live2d-hiyori-free")
                .name("Hiyori (Free)")
                .type("live2d")
                .description("AIRI 内置 Live2D Free 形象来源。")
                .config(Map.of("live2d", AvatarModelSourceVO.builder()
                        .urls(List.of("https://dist.ayaka.moe/live2d-models/hiyori_free_zh.zip"))
                        .build()))
                .layout(defaultLayout(0.28))
                .states(defaultStates())
                .build();
    }

    private AvatarModelVO avatarSampleA() {
        return AvatarModelVO.builder()
                .id("preset-vrm-avatar-sample-a")
                .name("AvatarSample_A")
                .type("vrm")
                .description("AIRI 内置 VRM 示例 A。当前前端未安装 VRM 渲染依赖，会显示统一占位形象。")
                .config(Map.of("vrm", AvatarModelSourceVO.builder()
                        .urls(List.of("https://dist.ayaka.moe/vrm-models/VRoid-Hub/AvatarSample-A/AvatarSample_A.vrm"))
                        .build()))
                .layout(defaultLayout(1.0))
                .states(defaultStates())
                .build();
    }

    private AvatarModelVO avatarSampleB() {
        return AvatarModelVO.builder()
                .id("preset-vrm-avatar-sample-b")
                .name("AvatarSample_B")
                .type("vrm")
                .description("AIRI 内置 VRM 示例 B。")
                .config(Map.of("vrm", AvatarModelSourceVO.builder()
                        .urls(List.of("https://dist.ayaka.moe/vrm-models/VRoid-Hub/AvatarSample-B/AvatarSample_B.vrm"))
                        .build()))
                .layout(defaultLayout(1.0))
                .states(defaultStates())
                .build();
    }

    private AvatarLayoutVO defaultLayout(double scale) {
        return AvatarLayoutVO.builder()
                .width(320)
                .height(420)
                .scale(scale)
                .x(0)
                .y(0)
                .transparent(true)
                .followPointer(true)
                .build();
    }

    private Map<String, AvatarStateVO> defaultStates() {
        Map<String, AvatarStateVO> states = new LinkedHashMap<>();
        states.put("idle", AvatarStateVO.builder().motionGroup("Idle").expression("normal").message("准备好一起学习了。").build());
        states.put("thinking", AvatarStateVO.builder().motionGroup("Thinking").expression("thinking").message("我正在分析你的问题...").build());
        states.put("speaking", AvatarStateVO.builder().motionGroup("TapBody").expression("happy").message("这是我的学习建议。").build());
        states.put("error", AvatarStateVO.builder().motionGroup("Error").expression("sad").message("刚才出了一点小问题。").build());
        return states;
    }
}

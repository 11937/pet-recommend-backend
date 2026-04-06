package com.pet.community.web;

import com.pet.common.entity.Result;
import com.pet.community.dto.PostCreateDTO;
import com.pet.community.service.CommunityPostService;
import com.pet.community.util.JwtRequestHelper;
import com.pet.community.vo.CommunityPostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/** 发帖、编辑、详情、作者下架、点赞与收藏。 */
@RestController
@RequestMapping("/community/posts")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService postService;
    private final JwtRequestHelper jwtRequestHelper;

    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody PostCreateDTO dto) {
        long uid = jwtRequestHelper.requireUserId();
        Long id = postService.createPost(uid, dto);
        Map<String, Long> body = new HashMap<>(2);
        body.put("id", id);
        return new Result<>("创建成功", body);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") long id, @Valid @RequestBody PostCreateDTO dto) {
        long uid = jwtRequestHelper.requireUserId();
        postService.updatePostByAuthor(uid, id, dto);
        return new Result<>(200, "已保存");
    }

    @GetMapping("/{id}")
    public Result<CommunityPostVO> detail(@PathVariable("id") long id) {
        Long viewer = jwtRequestHelper.currentUserIdOrNull();
        return new Result<>("ok", postService.getDetail(id, viewer));
    }

    /**
     * 作者下架：数据库保留，visibility=作者下架。
     */
    @DeleteMapping("/{id}")
    public Result<Void> authorOffline(@PathVariable("id") long id) {
        long uid = jwtRequestHelper.requireUserId();
        postService.authorOffline(uid, id);
        return new Result<>(200, "已下架");
    }

    @PostMapping("/{id}/like")
    public Result<Map<String, Boolean>> toggleLike(@PathVariable("id") long id) {
        long uid = jwtRequestHelper.requireUserId();
        boolean on = postService.toggleLike(uid, id);
        Map<String, Boolean> body = new HashMap<>(2);
        body.put("liked", on);
        return new Result<>("ok", body);
    }

    @PostMapping("/{id}/favorite")
    public Result<Map<String, Boolean>> toggleFavorite(@PathVariable("id") long id) {
        long uid = jwtRequestHelper.requireUserId();
        boolean on = postService.toggleFavorite(uid, id);
        Map<String, Boolean> body = new HashMap<>(2);
        body.put("favorited", on);
        return new Result<>("ok", body);
    }
}

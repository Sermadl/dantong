package org.jenga.dantong.post.model.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.jenga.dantong.post.model.entity.Category;
import org.jenga.dantong.post.model.entity.Post;
import org.jenga.dantong.user.model.dto.UserResponse;

@Getter
@Setter
public class PostResponse {

    private Long postId;
    private Long surveyId;
    private String title;
    private String description;
    private String content;
    private String status;
    private Category category;
    private List<PostFileResponse> postFileResponse;
    private UserResponse userResponse;

    public PostResponse(Post post, String status, List<PostFileResponse> postFileResponse,
        Long surveyId, UserResponse userResponse) {
        this.userResponse = userResponse;
        this.surveyId = surveyId;
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.description = post.getDescription();
        this.content = post.getContent();
        this.status = status;
        this.category = post.getCategory();
        this.postFileResponse = postFileResponse;
    }
}

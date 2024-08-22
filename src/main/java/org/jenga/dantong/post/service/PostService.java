package org.jenga.dantong.post.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.global.s3.model.dto.FileUploadRequest;
import org.jenga.dantong.global.s3.model.dto.RequestFile;
import org.jenga.dantong.global.s3.service.FileUploadService;
import org.jenga.dantong.global.util.Util;
import org.jenga.dantong.notification.model.dto.request.NotificationGlobalRequest;
import org.jenga.dantong.notification.model.dto.request.NotificationRequest;
import org.jenga.dantong.notification.service.FcmService;
import org.jenga.dantong.post.exception.PermissionDeniedException;
import org.jenga.dantong.post.exception.PostNofFoundException;
import org.jenga.dantong.post.model.dto.request.PostCreateRequest;
import org.jenga.dantong.post.model.dto.request.PostUpdateRequest;
import org.jenga.dantong.post.model.dto.response.PostFileResponse;
import org.jenga.dantong.post.model.dto.response.PostPreviewResponse;
import org.jenga.dantong.post.model.dto.response.PostResponse;
import org.jenga.dantong.post.model.entity.Category;
import org.jenga.dantong.post.model.entity.Post;
import org.jenga.dantong.post.model.entity.PostFile;
import org.jenga.dantong.post.repository.PostRepository;
import org.jenga.dantong.survey.model.dto.response.SurveySummaryResponse;
import org.jenga.dantong.survey.model.entity.Survey;
import org.jenga.dantong.user.exception.UserNotFoundException;
import org.jenga.dantong.user.model.dto.response.UserResponse;
import org.jenga.dantong.user.model.entity.User;
import org.jenga.dantong.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final FcmService fcmService;

    @Transactional
    public Long savePost(PostCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Post post = Post.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .content(request.getContent())
                .category(request.getCategory())
                .startDate(request.getStartTime())
                .endDate(request.getEndTime())
                .build();
        
        if (request.getImageFiles() != null) {
            saveFiles(request.getImageFiles(), post);
        }
        Post savedPost = postRepository.save(post);

        if (post.getCategory().equals(Category.NOTICE)){
            NotificationGlobalRequest notificationRequest = NotificationGlobalRequest.builder()
                    .body("공지 알림")
                    .title(request.getTitle())
                    .build();
            try {
                fcmService.sendGlobalNotification(notificationRequest);
            } catch (FirebaseMessagingException e) {
                throw new RuntimeException(e);
            }
        }

        return savedPost.getPostId();
    }

    @Transactional
    public PostResponse findPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNofFoundException::new);
        String progress = Util.getProgress(post);

        List<PostFileResponse> files = post.getFiles().stream()
                .map(file -> {
                    String url = fileUploadService.getFileUrl(file.getFileId());
                    return new PostFileResponse(file, url);
                }).collect(Collectors.toList());
        UserResponse userResponse = new UserResponse(post.getUser());
        PostResponse postResponse = new PostResponse(post, progress, files, userResponse);
        if (post.hasSurvey()) {
            SurveySummaryResponse surveySummaryResponse = new SurveySummaryResponse(
                    post.getSurvey());
            postResponse.setSurveySummaryResponse(surveySummaryResponse);
        }
        return postResponse;
    }

    @Transactional
    public Long deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNofFoundException::new);
        if (userId != post.getUser().getId()) {
            throw new PermissionDeniedException();
        }

        Survey survey = post.getSurvey();

        post.setShown(false);

        if (survey != null) {
            post.setSurvey(null);
            survey.setPost(null);
        }

        postRepository.delete(post);

        return postId;
    }

    @Transactional
    public Page<PostPreviewResponse> showAllPost(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return getPostResponses(posts);
    }

    @Transactional
    public Page<PostPreviewResponse> showByCategory(Category category, Pageable pageable) {
        Page<Post> posts = postRepository.findByCategory(category, pageable);
        return getPostResponses(posts);
    }

    private Page<PostPreviewResponse> getPostResponses(Page<Post> posts) {
        return posts.map(currPost -> {
            List<PostFileResponse> files = currPost.getFiles().stream()
                    .map(file -> {
                        String url = fileUploadService.getFileUrl(file.getFileId());
                        return new PostFileResponse(file, url);
                    }).collect(Collectors.toList());
            String progress = Util.getProgress(currPost);
            UserResponse userResponse = new UserResponse(currPost.getUser());
            return new PostPreviewResponse(currPost, progress, files, userResponse);
        });
    }

    @Transactional
    public Long updatePost(PostUpdateRequest request, Long userId) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(PostNofFoundException::new);
        if (!userId.equals(post.getUser().getId())) {
            throw new PermissionDeniedException();
        }
        post.setContent(request.getContent());
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setCategory(request.getCategory());
        post.setStartDate(request.getStartTime());
        post.setEndDate(request.getEndTime());

        return post.getPostId();
    }

    private void saveFiles(List<MultipartFile> files, Post post) {
        List<RequestFile> requestFiles = fileUploadService.uploadFiles(
                FileUploadRequest.ofList(files));
        List<PostFile> postFiles = new ArrayList<>();

        for (RequestFile file : requestFiles) {
            PostFile.PostFileBuilder builder = PostFile.builder()
                    .fileName(file.getOriginalName())
                    .mediaType(file.getMediaType().toString())
                    .fileId(file.getFileId());

            postFiles.add(builder.build());
        }

        for (PostFile file : postFiles) {
            file.setPost(post);
        }
    }
}

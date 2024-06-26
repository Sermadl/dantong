package org.jenga.dantong.survey.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.global.s3.service.FileUploadService;
import org.jenga.dantong.global.util.Util;
import org.jenga.dantong.post.exception.PermissionDeniedException;
import org.jenga.dantong.post.exception.PostNofFoundException;
import org.jenga.dantong.post.model.dto.response.PostFileResponse;
import org.jenga.dantong.post.model.entity.Post;
import org.jenga.dantong.post.repository.PostRepository;
import org.jenga.dantong.survey.exception.AlreadyHasSurveyException;
import org.jenga.dantong.survey.exception.SurveyItemNotFoundException;
import org.jenga.dantong.survey.exception.SurveyNotFoundException;
import org.jenga.dantong.survey.model.dto.request.SurveyCreateRequest;
import org.jenga.dantong.survey.model.dto.request.SurveyItemCreateRequest;
import org.jenga.dantong.survey.model.dto.request.SurveyItemUpdateRequest;
import org.jenga.dantong.survey.model.dto.request.SurveyUpdateRequest;
import org.jenga.dantong.survey.model.dto.response.SurveyAdminResponse;
import org.jenga.dantong.survey.model.dto.response.SurveyItemResponse;
import org.jenga.dantong.survey.model.dto.response.SurveyResponse;
import org.jenga.dantong.survey.model.dto.response.TicketResponse;
import org.jenga.dantong.survey.model.entity.Survey;
import org.jenga.dantong.survey.model.entity.SurveyItem;
import org.jenga.dantong.survey.model.entity.SurveySubmit;
import org.jenga.dantong.survey.repository.SurveyItemRepository;
import org.jenga.dantong.survey.repository.SurveyRepository;
import org.jenga.dantong.survey.repository.SurveySubmitRepository;
import org.jenga.dantong.user.exception.UserNotFoundException;
import org.jenga.dantong.user.model.entity.User;
import org.jenga.dantong.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyItemRepository surveyItemRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SurveySubmitRepository surveySubmitRepository;
    private final FileUploadService fileUploadService;

    @Transactional
    public SurveyResponse findSurvey(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
            .orElseThrow(SurveyNotFoundException::new);

        if (!survey.isShown()) {
            return SurveyResponse.builder()
                .description("Deleted Survey")
                .build();
        }

        List<SurveyItem> items = surveyItemRepository.findBySurveyAndShownTrue(survey);

        List<SurveyItemResponse> responseItems = items.stream()
            .map(currItem -> SurveyItemResponse.builder()
                .surveyItemId(currItem.getSurveyItemId())
                .title(currItem.getTitle())
                .tag(currItem.getTag())
                .description(currItem.getDescription())
                .options(currItem.getOptions())
                .build())
            .toList();

        SurveyResponse response = SurveyResponse.builder()
            .title(survey.getTitle())
            .description(survey.getDescription())
            .postId(Objects.isNull(survey.getPost()) ? 0 : survey.getPost().getPostId())
            .startTime(survey.getStartTime())
            .endTime(survey.getEndTime())
            .status(Util.getProgress(survey))
            .surveyItems(responseItems)
            .build();

        return response;
    }

    @Transactional
    public Long create(SurveyCreateRequest surveyCreate, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);
        Post post = postRepository.findById(surveyCreate.getPostId())
            .orElseThrow(PostNofFoundException::new);

        if (post.hasSurvey()) {
            throw new AlreadyHasSurveyException();
        }
        Survey survey = new Survey(
            surveyCreate.getTitle(),
            surveyCreate.getDescription(),
            post,
            surveyCreate.getStartTime(),
            surveyCreate.getEndTime()
        );
        survey.setUser(user);

        surveyRepository.save(survey);

        List<SurveyItemCreateRequest> item = surveyCreate.getSurveyItems();

        item.stream()
            .map(currItem -> SurveyItem.builder()
                .survey(survey)
                .title(currItem.getTitle())
                .tag(currItem.getTag())
                .description(currItem.getDescription())
                .options(currItem.getOptions())
                .build())
            .forEach(surveyItemRepository::save);

        return survey.getSurveyId();
    }

    @Transactional
    public Long updateSurvey(Long surveyId, SurveyUpdateRequest request, Long userId) {

        Survey survey = surveyRepository.findById(surveyId)
            .orElseThrow(SurveyNotFoundException::new);
        if (userId != survey.getUser().getId()) {
            throw new PermissionDeniedException();
        }

        Post post = postRepository.findById(request.getPostId())
            .orElseThrow(PostNofFoundException::new);
        if (userId != post.getUser().getId()) {
            throw new PermissionDeniedException();
        }

        survey.setTitle(request.getTitle());
        survey.setDescription(request.getDescription());
        survey.setPost(post);
        survey.setStartTime(request.getStartTime());
        survey.setEndTime(request.getEndTime());

        List<SurveyItemUpdateRequest> itemUpdate = request.getSurveyItems();

        itemUpdate
            .forEach(currItem -> {
                Boolean isNew = currItem.getIsNew();

                if (isNew != null && isNew) {
                    log.info("New Item detected");

                    SurveyItem newItem = SurveyItem.builder()
                        .survey(survey)
                        .surveyItemId(currItem.getSurveyItemId())
                        .title(currItem.getTitle())
                        .tag(currItem.getTag())
                        .options(currItem.getOptions())
                        .build();

                    surveyItemRepository.save(newItem);
                } else {
                    SurveyItem item = surveyItemRepository.findById(currItem.getSurveyItemId())
                        .orElseThrow(SurveyItemNotFoundException::new);
                    log.info("Item detected");
                    item.setTitle(currItem.getTitle());
                    item.setTag(currItem.getTag());
                    item.getOptions().clear();
                    for (String option : currItem.getOptions()) {
                        item.getOptions().add(option);
                    }
                }
            });

        return survey.getSurveyId();
    }

    @Transactional
    public void deleteSurvey(Long surveyId, Long userId) {
        Survey survey = surveyRepository.findById(surveyId)
            .orElseThrow(SurveyNotFoundException::new);
        if (userId != survey.getUser().getId()) {
            throw new PermissionDeniedException();
        }

        Post post = survey.getPost();

        surveyRepository.delete(survey);

        if (post != null) {
            post.setSurvey(null);
            survey.setPost(null);
        }
    }

    @Transactional
    public void deleteSurveyItem(Long surveyId, Long itemId, Long userId) {
        Survey survey = surveyRepository.findById(surveyId)
            .orElseThrow(SurveyNotFoundException::new);

        if (!Objects.equals(userId, survey.getUser().getId())) {
            throw new PermissionDeniedException();
        }

        if (surveyId.equals(surveyItemRepository.findById(itemId)
            .orElseThrow(SurveyItemNotFoundException::new)
            .getSurvey().getSurveyId())) {
            SurveyItem item = surveyItemRepository.findById(itemId)
                .orElseThrow(SurveyItemNotFoundException::new);

            item.setShown(false);
        } else {
            return;
        }
    }

    public Page<SurveyAdminResponse> getSurveyInfos(Pageable pageable) {
        Page<Survey> surveys = surveyRepository.findAll(pageable);
        return surveys
            .map(survey -> {
                String progress = Util.getProgress(survey);
                Long count = surveySubmitRepository.countBySurvey(survey);
                return new SurveyAdminResponse(survey,
                    progress, count);
            });
    }

    public List<TicketResponse> getTickets(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        List<SurveySubmit> submits = surveySubmitRepository.findByUser(user);
        return submits.stream().map(surveySubmit -> {
            Survey survey = surveySubmit.getSurvey();
            Post post = survey.getPost();
            List<PostFileResponse> postFiles = getFileFromPost(post);

            return TicketResponse.builder()
                .postId(post.getPostId())
                .title(survey.getTitle())
                .surveyId(survey.getSurveyId())
                .startTime(survey.getStartTime())
                .description(survey.getDescription())
                .status(Util.getProgress(survey))
                .postFileResponse(postFiles)
                .build();
        }).collect(Collectors.toList());
    }

    private List<PostFileResponse> getFileFromPost(Post post) {
        return post.getFiles().stream().map(file -> {
            String url = fileUploadService.getFileUrl(file.getFileId());
            return new PostFileResponse(file, url);
        }).collect(Collectors.toList());
    }

}

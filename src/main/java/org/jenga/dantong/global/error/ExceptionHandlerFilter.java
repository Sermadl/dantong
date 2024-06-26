package org.jenga.dantong.global.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.global.error.exception.ApplicationException;
import org.jenga.dantong.global.error.exception.ErrorResponseDto;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ApplicationException ex) {
            writeErrorResponse(response, request.getLocale(), ex);
        } catch (Exception ex) {
            writeUnexpectedErrorResponse(response, request.getLocale(), ex);
        }

    }

    private void writeErrorResponse(HttpServletResponse response, Locale locale,
        ApplicationException ex) throws IOException {
        ErrorResponseDto dto = new ErrorResponseDto(messageSource, locale, ex);
        log.error("A problem has occurred in filter: [id={}]", dto.getTrackingId(), ex);
        writeResponse(response, dto, ex.getStatus().value());
    }

    private void writeUnexpectedErrorResponse(HttpServletResponse response, Locale locale,
        Exception ex) throws IOException {
        ErrorResponseDto dto = new ErrorResponseDto(messageSource, locale,
            ApplicationException.of(ex));
        log.error("Unexpected exception has occurred in filter: [id={}]", dto.getTrackingId(), ex);
        writeResponse(response, dto, 500);
    }

    private void writeResponse(HttpServletResponse response, Object dto, int statusCode)
        throws IOException {
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String exceptionMessage = objectMapper.writeValueAsString(dto);
        response.getWriter().write(exceptionMessage);
    }
}

package com.officemeong.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-Admin-Key";
    private static final String ADMIN_PATH_PREFIX = "/api/v1/admin/";

    @Value("${admin.api-key:}")
    private String adminApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(ADMIN_PATH_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        if (adminApiKey == null || adminApiKey.isBlank()) {
            writeError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "관리자 API가 설정되지 않았습니다.");
            return;
        }

        String provided = request.getHeader(HEADER_NAME);
        if (!adminApiKey.equals(provided)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "관리자 API 키가 유효하지 않습니다.");
            return;
        }

        chain.doFilter(request, response);
    }

    // sendError()는 Tomcat의 에러 페이지 재디스패치(/error)를 유발해 Security 체인을 다시 타면서
    // 상태 코드가 403으로 뒤바뀌므로, 응답을 직접 작성해 그대로 반환한다.
    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"data\":null,\"message\":\"" + message + "\"}");
    }
}

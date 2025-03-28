package uk.gov.companieshouse.limitedpartnershipsapi.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.limitedpartnershipsapi.utils.ApiLogger;

import static uk.gov.companieshouse.limitedpartnershipsapi.utils.Constants.ERIC_REQUEST_ID_KEY;


@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_KEY = "start-time";

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        Long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_KEY, startTime);

        ApiLogger.infoContext(getRequestId(request), String.format("Start of request. Method: %s Path: %s",
                getRequestMethod(request), getRequestPath(request)), null);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_KEY);

        if (startTime == null) {
            ApiLogger.infoContext(getRequestId(request), "Start time not found in request attributes.", null);
        } else {
            long responseTime = System.currentTimeMillis() - startTime;
            ApiLogger.infoContext(getRequestId(request), String.format("End of request. Method: %s Path: %s Duration: %sms Status: %s",
                    getRequestMethod(request), getRequestPath(request), responseTime, response.getStatus()), null);
        }
    }

    private String getRequestPath(HttpServletRequest request) {
        return (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    }

    private String getRequestMethod(HttpServletRequest request) {
        return request.getMethod();
    }

    private String getRequestId(HttpServletRequest request) {
        return request.getHeader(ERIC_REQUEST_ID_KEY);
    }
}

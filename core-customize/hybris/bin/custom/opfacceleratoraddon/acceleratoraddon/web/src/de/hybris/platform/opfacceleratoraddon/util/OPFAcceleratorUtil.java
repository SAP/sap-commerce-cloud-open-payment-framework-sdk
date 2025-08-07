package de.hybris.platform.opfacceleratoraddon.util;

import com.google.common.collect.Lists;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import org.springframework.validation.Errors;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.hybris.platform.util.Sanitizer.sanitize;

public class OPFAcceleratorUtil {
    private OPFAcceleratorUtil() {
    }

    /**
     * Handles errors internally and returns a structured ErrorListWsDTO.
     *
     * @param throwable
     *         the thrown exception
     * @return an ErrorListWsDTO containing error details
     */
    public static ErrorListWsDTO handleErrorInternal(final Throwable throwable) {
        final ErrorListWsDTO errorListDto = new ErrorListWsDTO();
        final ErrorWsDTO error = new ErrorWsDTO();
        error.setType(throwable.getClass().getSimpleName().replace("Exception", "Error"));
        error.setMessage(sanitize(throwable.getMessage()));
        if (throwable.getCause() instanceof HttpClientErrorException) {
            error.setExceptionMessage(sanitize(((HttpClientErrorException) throwable.getCause()).getMessage()));
            error.setErrorCode(String.valueOf(((HttpClientErrorException) throwable.getCause()).getRawStatusCode()));
        }
        errorListDto.setErrors(Lists.newArrayList(error));
        return errorListDto;
    }

    /**
     * @param errors
     *         error object
     * @return Return error details
     */
    public static Map<String, Object> handleErrors(Errors errors) {
        if (!errors.hasErrors()) {
            return Collections.emptyMap();
        }
        List<Map<String, String>> errorList = errors.getFieldErrors().stream().map(error -> Map.of("type", error.getField(), "message",
                error.getDefaultMessage() != null ? error.getDefaultMessage() : "")).collect(Collectors.toList());
        return Map.of("errors", errorList);
    }

}

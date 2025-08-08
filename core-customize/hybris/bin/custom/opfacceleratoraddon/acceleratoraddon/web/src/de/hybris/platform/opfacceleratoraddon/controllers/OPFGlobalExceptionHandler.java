/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.opfacceleratoraddon.controllers;

import de.hybris.platform.opfacceleratoraddon.exception.OPFAcceleratorException;
import de.hybris.platform.opfacceleratoraddon.exception.OPFRequestValidationException;
import de.hybris.platform.opfservices.client.CCAdapterClientException;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static de.hybris.platform.util.Sanitizer.sanitize;

/**
 * Global exception handler
 */
@RestControllerAdvice
public class OPFGlobalExceptionHandler {
    private static final Logger LOG = Logger.getLogger(OPFGlobalExceptionHandler.class);

    /**
     * Handle custom OPF Exception
     *
     * @param ex
     *         The exception to handle, can either be an instance of OPFAcceleratorException or OPFRequestValidationException.
     * @return ResponseEntity containing the error details and corresponding HTTP status code.
     */
    @ExceptionHandler({ OPFAcceleratorException.class, OPFRequestValidationException.class })
    public ResponseEntity<ErrorListWsDTO> handleOpfException(final Throwable ex) {
        LOG.error(sanitize(ex.getMessage()), ex);
        ErrorListWsDTO errorListWsDTO = handleErrorInternal(ex);
        String httpStatus = StringUtils.EMPTY;
        if (CollectionUtils.isNotEmpty(errorListWsDTO.getErrors())) {
            ErrorWsDTO errorWsDTO = errorListWsDTO.getErrors().get(0);
            httpStatus = errorWsDTO.getErrorCode();
        }
        return new ResponseEntity<>(errorListWsDTO, resolveHttpStatus(ex, httpStatus));
    }

    /**
     * Return the HTTP status
     *
     * @param ex
     *         exception
     * @param status
     *         HttpStatus
     * @return HttpStatus
     */
    private HttpStatus resolveHttpStatus(Throwable ex, String status) {
        if (StringUtils.isNotEmpty(status)) {
            return HttpStatus.resolve(Integer.parseInt(status));
        } else if (ex instanceof OPFRequestValidationException || ex instanceof HttpClientErrorException || ex instanceof CCAdapterClientException) {
            return HttpStatus.BAD_REQUEST;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * Handles errors internally and returns a structured ErrorListWsDTO.
     *
     * @param throwable
     *         the thrown exception
     * @return an ErrorListWsDTO containing error details
     */
    public ErrorListWsDTO handleErrorInternal(final Throwable throwable) {
        ErrorListWsDTO errorListDto = new ErrorListWsDTO();
        final Throwable cause = (throwable != null) ? throwable : ((throwable != null) ? throwable.getCause() : null);
        errorListDto = handleValidationException(errorListDto, getValidationException(cause));
        if (CollectionUtils.isNotEmpty(errorListDto.getErrors())) {
            return errorListDto;
        }
        return handleException(errorListDto, cause);
    }

    /**
     * Handle validation exception
     *
     * @param errorListDto
     *         Errorlist
     * @param ex
     *         exception
     * @return return error list
     */
    public ErrorListWsDTO handleValidationException(ErrorListWsDTO errorListDto, OPFRequestValidationException ex) {
        if (ex != null) {
            Object validationObject = ex.getValidationObject();
            if (!(validationObject instanceof Errors validationErrors)) {
                errorListDto.setErrors(Collections.singletonList(
                        populateErrorDTO("Error", "Validation error: Some required fields are missing or contain errors.",
                                sanitize(ex.getMessage()), String.valueOf(HttpStatus.BAD_REQUEST.value()))));
                return errorListDto;
            }
            List<ErrorWsDTO> fieldErrors = new ArrayList<>();
            if (validationErrors.getFieldErrors() != null && !validationErrors.getFieldErrors().isEmpty()) {
                fieldErrors = validationErrors.getFieldErrors().stream().map(errorDto -> {
                    return populateErrorDTO(errorDto.getField(), "Validation error: Some required fields are missing or contain errors.",
                            sanitize(ex.getMessage()), String.valueOf(HttpStatus.BAD_REQUEST.value()));
                }).collect(Collectors.toList());
            } else if (validationErrors.getAllErrors() != null && !validationErrors.getAllErrors().isEmpty()) {
                for (ObjectError objectError : validationErrors.getAllErrors()) {
                    fieldErrors.add(populateErrorDTO("Error", objectError.getCode(), sanitize(ex.getMessage()),
                            String.valueOf(HttpStatus.BAD_REQUEST.value())));
                }
            }
            errorListDto.setErrors(fieldErrors);
            return errorListDto;
        }
        return errorListDto;
    }

    /**
     * Create and populate Error
     *
     * @param type
     *         Error Type
     * @param message
     *         Message
     * @param exceptionMessage
     *         exception message
     * @param errorCode
     *         error code
     * @return
     */
    private ErrorWsDTO populateErrorDTO(String type, String message, String exceptionMessage, String errorCode) {
        ErrorWsDTO errorDto = new ErrorWsDTO();
        errorDto.setType(type);
        errorDto.setMessage(message);
        errorDto.setExceptionMessage(exceptionMessage);
        errorDto.setErrorCode(errorCode);
        return errorDto;
    }

    /**
     * Handle the exception other than validation
     *
     * @param errorListDto
     *         Errorlist
     * @param cause
     *         throwable
     * @return ErrorListWsDTO
     */
    public ErrorListWsDTO handleException(ErrorListWsDTO errorListDto, Throwable cause) {
        final ErrorWsDTO error = new ErrorWsDTO();
        error.setType(cause.getClass().getSimpleName().replace("Exception", "Error"));
        String exceptionMessage = StringUtils.EMPTY;
        String errorCode = StringUtils.EMPTY;
        String message = StringUtils.EMPTY;
        if (cause.getCause() instanceof HttpClientErrorException httpEx) {
            exceptionMessage = sanitize(httpEx.getMessage());
            errorCode = String.valueOf(httpEx.getRawStatusCode());
            message = "There was an error encountered during the processing of the request.";
        } else if (cause.getCause() instanceof CCAdapterClientException ccEx) {
            exceptionMessage = sanitize(ccEx.getMessage());
            errorCode = String.valueOf(HttpStatus.BAD_REQUEST.value());
            message = "Client error encountered during processing.";
        } else {
            // Handle all other uncaught exceptions
            exceptionMessage = sanitize(cause.getCause().getMessage());
            errorCode = String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        error.setExceptionMessage(exceptionMessage);
        error.setErrorCode(errorCode);
        error.setMessage(message);
        errorListDto.setErrors(Collections.singletonList(error));
        return errorListDto;
    }

    /**
     * Check for OPFRequestValidationException instance
     *
     * @param cause
     *         throwable
     * @return OPFRequestValidationException
     */
    private OPFRequestValidationException getValidationException(Throwable cause) {
        if (cause instanceof OPFRequestValidationException ex) {
            return ex;
        } else if (cause != null && cause.getCause() instanceof OPFRequestValidationException innerEx) {
            return innerEx;
        }
        return null;
    }
}





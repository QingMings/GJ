package com.yhl.gj.commons.intercepter;

import com.yhl.gj.commons.base.Response;
import com.yhl.gj.commons.constant.Constants;
import com.yhl.gj.commons.enums.ErrorEnum;
import com.yhl.gj.commons.exception.BusinessException;
import com.yhl.gj.commons.exception.TokenInvalidException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionIntercept {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Response<Object>> handleOmsException(HttpServletRequest req, BusinessException ex) {
		ex.printStackTrace();
		String requestUrl = req.getRequestURL().toString();
//		sendExceptionMessage(ex,requestUrl);
		Response<Object> omsResponse = new Response<Object>();
		omsResponse.setSuccess(Constants.FAIL);
		omsResponse.setErrorMessage(ex.getMessage());
		omsResponse.setErrorCode(ex.getErrorCode());
		log.error("异常", ex);
		return new ResponseEntity<>(omsResponse, HttpStatus.OK);
	}
	/**
	 * valid 校验参数异常
	 * @param ex
	 * @param req
	 * @return
	 */
	@ExceptionHandler(value = ConstraintViolationException.class)
	public ResponseEntity<Response<Object>> ConstraintViolationExceptionHandler(ConstraintViolationException ex, HttpServletRequest req){
		log.error("校验参数异常:URI={}",req.getRequestURI());
		Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
		String errorMessage = constraintViolations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(";"));
        log.error("errorMessage={}",errorMessage);
		Response<Object> response = new Response<>();
		response.setSuccess(Constants.FAIL);
		response.setErrorMessage(errorMessage);
		response.setErrorCode(ErrorEnum.PARAM_ERROR.getErrorCode());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * IllegalArgument Exception异常
	 *
	 * @param ex
	 * @param req
	 * @return
	 */
	@ExceptionHandler(value = IllegalArgumentException.class)
	public ResponseEntity<Response<Object>> IllegalArgumentExceptionHandler(Exception ex, HttpServletRequest req) {
		String requestUrl = req.getRequestURL().toString();
		log.error("请求地址[{}]，异常信息[{}]", requestUrl, ex.getMessage());
		Response<Object> omsResponse = new Response<>();
		omsResponse.setSuccess(Constants.FAIL);
		omsResponse.setErrorMessage(ex.getMessage());
		omsResponse.setErrorCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
		ex.printStackTrace();
		return new ResponseEntity<>(omsResponse, HttpStatus.OK);
	}
	/**
	 * valid Exception异常
	 * @param ex
	 * @param req
	 * @return
	 */
	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<Response<Object>> ExceptionHandler(Exception ex, HttpServletRequest req){
		log.error("系统异常:URI={}",req.getRequestURI());
		log.error("系统异常：errorMessage={}",ex);
		Response<Object> response = new Response<>();
		response.setSuccess(Constants.FAIL);
		response.setErrorMessage(ErrorEnum.SYS_ERROR.getErrorMsg());
		response.setErrorCode(ErrorEnum.SYS_ERROR.getErrorCode());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@ExceptionHandler(TokenInvalidException.class)
	public ResponseEntity<Response<Object>> TokenInvalidException(TokenInvalidException ex, HttpServletRequest req){
		log.error("token 无效: URI={}",req.getRequestURI());
		log.error("token 无效 异常 ={}",ex);
		Response<Object> response = new Response<>();
		response.setSuccess(Constants.FAIL);
		response.setErrorMessage(ex.getErrorMsg());
		response.setErrorCode(ex.getErrorCode());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * 参数实体类校验错误
	 *
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Response<Object>> handleValidatedParamException(HttpServletRequest req, MethodArgumentNotValidException ex) {
		ex.printStackTrace();
		String requestUrl = req.getRequestURL().toString();
//		sendExceptionMessage(ex,requestUrl);
		Response<Object> omsResponse = new Response<>();
		omsResponse.setSuccess(Constants.FAIL);

		List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
		StringBuilder sb = new StringBuilder();
		if (CollectionUtils.isNotEmpty(fieldErrors)) {
			fieldErrors.forEach(item -> sb.append(item.getDefaultMessage() + " "));
		}
		omsResponse.setErrorMessage(sb.toString());
		omsResponse.setErrorCode(ErrorEnum.PARAM_ERROR.getErrorCode());

		log.error("参数或者实体类异常", ex);
		return new ResponseEntity<>(omsResponse, HttpStatus.OK);
	}
}
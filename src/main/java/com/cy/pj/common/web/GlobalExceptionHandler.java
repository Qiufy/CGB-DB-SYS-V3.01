package com.cy.pj.common.web;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.cy.pj.common.vo.JsonResult;
/**
 * @ControllerAdvice 描述的类表示一个全局异常处理类
 * @ExceptionHandler 描述的方法为一个异常处理方法
 */
//@ControllerAdvice
//@ResponseBody
@RestControllerAdvice//==@ControllerAdvice+@ResponseBody
public class GlobalExceptionHandler {
	
	  @ExceptionHandler(RuntimeException.class)
	  //@ResponseBody
	  public JsonResult doHandleRuntimeException(
			  RuntimeException e) {
		  e.printStackTrace();
		  return new JsonResult(e);
	  }
}






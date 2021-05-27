package main.service.captcha;

import main.api.response.CaptchaResponse;

public interface CaptchaService {

  CaptchaResponse getNewCaptcha();
}
package com.sampletests.pages;

import com.sampletests.framework.WebUtils;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyPage {
    WebUtils webUtils;
    private static final Logger LOGGER = LoggerFactory.getLogger(MyPage.class);

    public MyPage(WebUtils webUtils) {
        this.webUtils = webUtils;
        PageFactory.initElements(webUtils.getDriver(), this);
    }

    public void visitHomePage() {
        webUtils.visitPage(webUtils.getValueFromProperties("baseUrl"));
    }
}

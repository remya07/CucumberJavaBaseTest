package com.sampletests.testSteps;

import com.sampletests.pages.MyPage;
import cucumber.api.java.en.Given;

public class MyStepdefs {
    MyPage myPage;

    public MyStepdefs(MyPage myPage) {
        this.myPage = myPage;
    }

    @Given("^the user is in nopecommerce home page$")
    public void theUserIsInNopecommerceHomePage() {
        myPage.visitHomePage();
    }
}

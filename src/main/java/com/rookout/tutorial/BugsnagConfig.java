package com.rookout.tutorial;

import com.bugsnag.Bugsnag;

public class BugsnagConfig {
    public Bugsnag bugsnag() {
        String bugsnagApiKey = System.getenv("BUGSNAG_API_KEY");
        if (bugsnagApiKey == null || bugsnagApiKey.equals("")) {
            return null;
        }
        return new Bugsnag(bugsnagApiKey);
    }
}
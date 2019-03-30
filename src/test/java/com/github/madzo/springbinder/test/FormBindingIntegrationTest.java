package com.github.madzo.springbinder.test;

import com.github.madzo.springbinder.model.Company;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FormBindingIntegrationTest extends AbstractIntegrationTest {
    @Ignore
    @Test
    public void arrayWithNoIndexNumberTest() {
        Company root = new Company();
        List<String> bindingList = new ArrayList<>();
        bindingList.add("name=My company");
        bindingList.add("name=My company");
    }
}

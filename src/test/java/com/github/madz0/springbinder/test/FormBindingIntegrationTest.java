package com.github.madz0.springbinder.test;

import com.github.madz0.springbinder.model.City;
import com.github.madz0.springbinder.model.House;
import com.github.madz0.springbinder.repository.HouseRepository;
import com.github.madz0.springbinder.repository.CityRepository;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import ognl.Ognl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class FormBindingIntegrationTest extends AbstractIntegrationTest {
    private final static String BASE_URL = "/company/";
    @Autowired
    HouseRepository houseRepository;
    @Autowired
    CityRepository cityRepository;

    @Test
    public void arrayWithNoIndexNumberTest() throws Exception {
        final List<String> expressionsCapture = new ArrayList<>();
        new MockUp(Ognl.class) {
            @Mock
            public Object getValue(Invocation invocation, List<String> expressions, Map context, Class cls) {
                expressionsCapture.addAll(expressions);
                return invocation.proceed();
            }
        };
        City city = new City();
        city.setName("Tehran");
        city = cityRepository.save(city);
        House house = new House();
        house.setAddress("Karoon");
        house.setCity(city);
        house = houseRepository.save(house);
        List<String> bindingList = new ArrayList<>();
        bindingList.add("name=Company1");
        bindingList.add("city.id=" + city.getId());
        bindingList.add("employees[].name=Mohammad1");
        bindingList.add("employees[].name=Mohammad2");
        bindingList.add("employees[0].house.id=" + house.getId());
        bindingList.add("employees[1].house.id=" + house.getId());
        MvcResult mvcResult = mockMvc.perform(post(BASE_URL + "create")
                .content(String.join("&", bindingList))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andReturn();
        assertTrue(String.join("", expressionsCapture).contains("employees[0].name=Mohammad"));
        assertTrue(String.join("", expressionsCapture).contains("employees[1].name=Mohammad"));
        assertTrue(mvcResult.getResponse().getContentAsString().contains("OK"));
    }
}

package com.github.madz0.springbinder.test;

import com.github.madz0.ognl2.Ognl;
import com.github.madz0.springbinder.model.*;
import com.github.madz0.springbinder.model.dto.SomeDto;
import com.github.madz0.springbinder.repository.*;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FormBindingIntegrationTest extends AbstractIntegrationTest {
    private final static String BASE_URL = "/company/";
    @Autowired
    HouseRepository houseRepository;
    @Autowired
    CityRepository cityRepository;
    @Autowired
    ParkingRepository parkingRepository;
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    CompanyRepository companyRepository;

    @Test
    public void arrayWithNoIndexNumberTest() throws Exception {
        final List<Map.Entry<String, Object>> expressionsCapture = new ArrayList<>();
        new MockUp(Ognl.class) {
            @Mock
            public Object getValue(Invocation invocation, List<Map.Entry<String, Object>> expressions, Map context, Class cls) {
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
        bindingList.add("name2=Company2");
        bindingList.add("city.id=" + city.getId());
        bindingList.add("employees[].name=Mohammad1");
        bindingList.add("employees[].name=Mohammad2");
        bindingList.add("employees[0].house.id=" + house.getId());
        bindingList.add("employees[1].house.id=" + house.getId());
        MvcResult mvcResult = mockMvc.perform(post(BASE_URL + "create")
                .content(String.join("&", bindingList))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isCreated())
                .andReturn();
        List<String> expressionsCaptureToStr = expressionsCapture.stream().map(x -> x.getKey() + "=" + x.getValue()).collect(Collectors.toList());
        assertTrue(String.join("", expressionsCaptureToStr).contains("employees[0].name=Mohammad"));
        assertTrue(String.join("", expressionsCaptureToStr).contains("employees[1].name=Mohammad"));
    }

    @Test
    @Transactional
    public void complexFileUploadTest() throws Exception {
        City city = new City();
        city.setName("Tehran");
        city = cityRepository.save(city);
        House house = new House();
        house.setAddress("Karoon");
        house.setCity(city);
        house = houseRepository.save(house);
        Company company = new Company();
        company.setName("Your Company");
        company.setName2("Your Company2");
        company.setCity(city);
        companyRepository.save(company);

        Employee employee = new Employee();
        employee.setCompany(company);
        employee.setName("Gholi");
        employee.setHouse(house);
        employeeRepository.save(employee);

        Parking parking1 = new Parking();
        parking1.setName("parking1");
        parkingRepository.save(parking1);
        Parking parking2 = new Parking();
        parking2.setName("parking2");
        parkingRepository.save(parking2);

        File f = new File("src/test/resources/data.sql.ignore");
        assertTrue(f.exists());
        FileInputStream fi1 = new FileInputStream(f);
        File f2 = new File("src/test/resources/application.properties");
        assertTrue(f2.exists());
        FileInputStream fi2 = new FileInputStream(f2);
        MockMultipartFile fstmp = new MockMultipartFile("employeeParkings[0].file", f.getName(), "multipart/form-data", fi1);
        MockMultipartFile secmp = new MockMultipartFile("employeeParkings[1].file", f2.getName(), "multipart/form-data", fi2);

        List<String> bindingList = new ArrayList<>();
        bindingList.add("id=" + employee.getId());
        bindingList.add(String.format("employeeParkings[%d].id.employeeId=%d", 0, employee.getId()));
        bindingList.add(String.format("employeeParkings[%d].id.parkingId=%d", 0, parking1.getId()));
        bindingList.add(String.format("employeeParkings[%d].parking.id=%d", 0, parking1.getId()));

        bindingList.add(String.format("employeeParkings[%d].id.employeeId=%d", 1, employee.getId()));
        bindingList.add(String.format("employeeParkings[%d].id.parkingId=%d", 1, parking2.getId()));
        bindingList.add(String.format("employeeParkings[%d].parking.id=%d", 1, parking2.getId()));

        MvcResult mvcResult = mockMvc.perform(multipart("/employeeParking/create")
                .file(fstmp)
                .file(secmp)
                .content(String.join("&", bindingList))
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn();
        Employee e = employeeRepository.findOne(employee.getId());
        List<EmployeeParking> employeeParkingList = new ArrayList<>(e.getEmployeeParkings());
        if (employeeParkingList.get(0).getId().getParkingId().equals(parking1.getId())) {
            assertEquals(f.getName(), employeeParkingList.get(0).getNameFromFile());
            assertEquals(f2.getName(), employeeParkingList.get(1).getNameFromFile());
        } else {
            assertEquals(f.getName(), employeeParkingList.get(1).getNameFromFile());
            assertEquals(f2.getName(), employeeParkingList.get(0).getNameFromFile());
        }
    }

    @Test
    public void emptyPostRequestTest() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post(BASE_URL + "create"))
                .andExpect(status().isBadRequest())
                .andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        assertTrue(result.contains("name"));
    }

    @Test
    public void emptyPostRequest2Test() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post(BASE_URL + "create"))
                .andExpect(status().isBadRequest())
                .andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        assertTrue(result.contains("name"));
    }

    @Test
    public void dtoBindTest() throws Exception {
        final List<Map.Entry<String, Object>> expressionsCapture = new ArrayList<>();
        new MockUp(Ognl.class) {
            @Mock
            public Object getValue(Invocation invocation, List<Map.Entry<String, Object>> expressions, Map context, Class cls) {
                expressionsCapture.addAll(expressions);
                return invocation.proceed();
            }
        };
        final SomeDto someDto = new SomeDto();
        List<String> bindingList = new ArrayList<>();
        bindingList.add("name=Mohammad");
        bindingList.add("family=Mohammad2");
        MvcResult mvcResult = mockMvc.perform(post(BASE_URL + "dto")
                .content(String.join("&", bindingList))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn();
        List<String> expressionsCaptureToStr = expressionsCapture.stream().map(x -> x.getKey() + "=" + x.getValue()).collect(Collectors.toList());
        assertTrue(String.join("", expressionsCaptureToStr).contains("name=Mohammad"));
        assertTrue(String.join("", expressionsCaptureToStr).contains("family=Mohammad2"));
    }

    @Test
    public void arrayWithNoIndexNumberMultiSelectTest() throws Exception {
        final List<Map.Entry<String, Object>> expressionsCapture = new ArrayList<>();
        new MockUp(Ognl.class) {
            @Mock
            public Object getValue(Invocation invocation, List<Map.Entry<String, Object>> expressions, Map context, Class cls) {
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
        Company company = new Company();
        company.setName("Your Company");
        company.setName2("Your Company2");
        company.setCity(city);
        Employee employee = new Employee();
        employee.setCompany(company);
        employee.setName("Gholi");
        employee.setHouse(house);
        company.setEmployees(new HashSet<>(Collections.singleton(employee)));
        company = companyRepository.save(company);

        List<String> bindingList = new ArrayList<>();
        bindingList.add("id=" + company.getId());
        bindingList.add("name=Company1");
        bindingList.add("name2=Company2");
        bindingList.add("city.id=" + city.getId());
        bindingList.add(URLEncoder.encode("employees[(;)(=)]", "UTF-8") + "=" + URLEncoder.encode(".name=Mohammad1;.id=" + employee.getId(), "UTF-8"));
        bindingList.add(URLEncoder.encode("employees[(;)(=)]", "UTF-8") + "=" + URLEncoder.encode(".name=Mohammad2", "UTF-8"));
        MvcResult mvcResult = mockMvc.perform(post(BASE_URL + "create")
                .content(String.join("&", bindingList))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isCreated())
                .andReturn();
        List<String> expressionsCaptureToStr = expressionsCapture.stream().map(x -> x.getKey() + "=" + x.getValue()).collect(Collectors.toList());
        assertTrue(String.join("", expressionsCaptureToStr).contains("employees[0].name=Mohammad1"));
        assertTrue(String.join("", expressionsCaptureToStr).contains("employees[1].name=Mohammad2"));
    }

    @Test
    public void bindWithGetTest() throws Exception {
        final List<Map.Entry<String, Object>> expressionsCapture = new ArrayList<>();
        new MockUp(Ognl.class) {
            @Mock
            public Object getValue(Invocation invocation, List<Map.Entry<String, Object>> expressions, Map context, Class cls) {
                expressionsCapture.addAll(expressions);
                return invocation.proceed();
            }
        };
        final SomeDto someDto = new SomeDto();
        List<String> bindingList = new ArrayList<>();
        bindingList.add("name=Mohammad");
        bindingList.add("family=Mohammad2");
        MvcResult mvcResult = mockMvc.perform(get(BASE_URL + "dto")
                .content(String.join("&", bindingList))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn();
        List<String> expressionsCaptureToStr = expressionsCapture.stream().map(x -> x.getKey() + "=" + x.getValue()).collect(Collectors.toList());
        assertTrue(String.join("", expressionsCaptureToStr).contains("name=Mohammad"));
        assertTrue(String.join("", expressionsCaptureToStr).contains("family=Mohammad2"));
    }

    @Test
    public void bindWithGetQueryStringTest() throws Exception {
        final List<Map.Entry<String, Object>> expressionsCapture = new ArrayList<>();
        new MockUp(Ognl.class) {
            @Mock
            public Object getValue(Invocation invocation, List<Map.Entry<String, Object>> expressions, Map context, Class cls) {
                expressionsCapture.addAll(expressions);
                return invocation.proceed();
            }
        };
        final SomeDto someDto = new SomeDto();
        List<String> bindingList = new ArrayList<>();
        bindingList.add("name=Mohammad");
        bindingList.add("family=Mohammad2");
        MvcResult mvcResult = mockMvc.perform(get(BASE_URL + "dto?name=Mohammad&family=Mohammad2")
                .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andReturn();
        List<String> expressionsCaptureToStr = expressionsCapture.stream().map(x -> x.getKey() + "=" + x.getValue()).collect(Collectors.toList());
        assertTrue(String.join("", expressionsCaptureToStr).contains("name=Mohammad"));
        assertTrue(String.join("", expressionsCaptureToStr).contains("family=Mohammad2"));
    }

    @Test
    public void bindWithPostAndQueryStringTest() throws Exception {
        final List<Map.Entry<String, Object>> expressionsCapture = new ArrayList<>();
        new MockUp(Ognl.class) {
            @Mock
            public Object getValue(Invocation invocation, List<Map.Entry<String, Object>> expressions, Map context, Class cls) {
                expressionsCapture.addAll(expressions);
                return invocation.proceed();
            }
        };
        mockMvc.perform(post(BASE_URL + "dto?family=Mohammad2")
                .content("name=Mohammad")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn();
        List<String> expressionsCaptureToStr = expressionsCapture.stream().map(x -> x.getKey() + "=" + x.getValue()).collect(Collectors.toList());
        assertTrue(String.join("", expressionsCaptureToStr).contains("name=Mohammad"));
        assertTrue(String.join("", expressionsCaptureToStr).contains("family=Mohammad2"));
    }

    @Test
    public void mapOrderBindTest() throws Exception {
        List<String> bindingList = new ArrayList<>();
        bindingList.add(URLEncoder.encode("someMap[test1]", "UTF-8") + "=" + URLEncoder.encode("Mohammad1", "UTF-8"));
        bindingList.add(URLEncoder.encode("someMap[test2]", "UTF-8") + "=" + URLEncoder.encode("Mohammad2", "UTF-8"));
        MvcResult mvcResult = mockMvc.perform(post(BASE_URL + "dto")
                .content(String.join("&", bindingList))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().startsWith("{\"result\":{\"someMap\":{\"test1\":\"Mohammad1"));
        bindingList = new ArrayList<>();
        bindingList.add(URLEncoder.encode("someMap[test2]", "UTF-8") + "=" + URLEncoder.encode("Mohammad2", "UTF-8"));
        bindingList.add(URLEncoder.encode("someMap[test1]", "UTF-8") + "=" + URLEncoder.encode("Mohammad1", "UTF-8"));
        mvcResult = mockMvc.perform(post(BASE_URL + "dto")
                .content(String.join("&", bindingList))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().startsWith("{\"result\":{\"someMap\":{\"test2\":\"Mohammad2"));
    }
}

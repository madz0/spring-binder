package com.github.madzo.springbinder.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.madz0.springbinder.binding.BindUtils;
import com.github.madz0.springbinder.binding.property.IProperty;
import com.github.madz0.springbinder.config.Config;
import com.github.madz0.springbinder.model.BaseGroups;
import com.github.madzo.springbinder.App;
import com.github.madzo.springbinder.model.*;
import com.github.madzo.springbinder.repository.CarManufactureRepository;
import com.github.madzo.springbinder.repository.CityRepository;
import com.github.madzo.springbinder.repository.CompanyRepository;
import com.github.madzo.springbinder.repository.HouseRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = {App.class, Config.class})
public class JsonBindingTest extends BaseTest {

    @Autowired
    EntityManager em;
    @Autowired
    HouseRepository houseRepository;
    @Autowired
    CityRepository cityRepository;
    @Autowired
    CarManufactureRepository carManufactureRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    ObjectMapper mapper;

    @Test
    public void bindEntityWithGroups() throws IOException {
        City city = new City();
        city.setName("Tehran");
        city = cityRepository.save(city);
        House house = new House();
        house.setAddress("Karoon");
        house.setCity(city);
        house = houseRepository.save(house);
        CarManufacture carManufacture = new CarManufacture();
        carManufacture.setName("BMW");
        carManufacture = carManufactureRepository.save(carManufacture);

        LocalDate ld = LocalDate.of(2018, 7, 29);
        Date d = Date.valueOf(ld);
        LocalTime lt = LocalTime.of(10, 11, 11);
        Time t = Time.valueOf(lt);

        BindUtils.group.set(ICreate1.class);
        String data = "{\n" +
                "\"name\":\"My company\",\n" +
                "\"city\":{\n" +
                " \"id\":\""+city.getId()+"\"\n" +
                "},\n" +
                "\"employees\": [{\n" +
                " \"name\":\"Mohamad\",\n" +
                " \"house\":{\n" +
                "  \"id\":\""+house.getId()+"\"\n" +
                " },\n" +
                " \"cars\":[{\n" +
                "  \"model\":\"Benz clo\",\n" +
                "  \"date\":\""+ld.format(DateTimeFormatter.ofPattern("y-MM-dd"))+"\",\n" +
                "  \"time\":\"10:11:11\",\n" +
                "  \"manufacture\":{\n" +
                "   \"id\":\""+carManufacture.getId()+"\"\n" +
                "  }}]\n" +
                "}]}";
        Company root;
        try {
            BindUtils.group.set(ICreate1.class);
            BindUtils.idClass.set(Long.class);
            root = mapper.readValue(data, Company.class);
        }
        finally {
            BindUtils.group.remove();
            BindUtils.idClass.remove();
        }
        assertEquals("My company", root.getName());
        assertEquals(city.getId(), root.getCity().getId());
        assertEquals("Mohamad", root.getEmployees().iterator().next().getName());
        assertEquals("Benz clo", root.getEmployees().iterator().next().getCars().iterator().next().getModel());
        assertEquals(d.toLocalDate(), root.getEmployees().iterator().next().getCars().iterator().next().getDate().toLocalDate());
        assertEquals(t, root.getEmployees().iterator().next().getCars().iterator().next().getTime());
        assertEquals(carManufacture.getId(), root.getEmployees().iterator().next().getCars().iterator().next().getManufacture().getId());
    }

    @Test
    public void bindEntityWithGroups_NoCarModel() throws Exception {
        City city = new City();
        city.setName("Tehran");
        city = cityRepository.save(city);
        House house = new House();
        house.setAddress("Karoon");
        house.setCity(city);
        house = houseRepository.save(house);
        CarManufacture carManufacture = new CarManufacture();
        carManufacture.setName("BMW");
        carManufacture = carManufactureRepository.save(carManufacture);

        LocalDate ld = LocalDate.of(2018, 7, 29);
        Date d = Date.valueOf(ld);
        LocalTime lt = LocalTime.of(10, 11, 11);
        Time t = Time.valueOf(lt);

        Company root = new Company();
        root.setName("Your Company");
        root.setCity(city);

        Employee employee = new Employee();
        employee.setCompany(root);
        employee.setName("Gholi");
        employee.setHouse(house);
        Car car = new Car();
        car.setDate(d);
        car.setTime(t);
        car.setModel("206");
        car.setEmployee(employee);
        car.setManufacture(carManufacture);
        car.setHasSold(true);
        employee.setCars(new HashSet<>(Arrays.asList(car)));
        root.setEmployees(new HashSet<>(Arrays.asList(employee)));
        root = companyRepository.save(root);
        String data = "{\"id\":\""+root.getId()+"\"," +
                "\"name\":\"My company\",\n" +
                "\"city\":{\n" +
                " \"id\":\""+city.getId()+"\"\n" +
                "},\n" +
                "\"employees\": [{\n" +
                " \"id\":\""+employee.getId()+"\",\n" +
                " \"name\":\"Mohamad\",\n" +
                " \"house\":{\n" +
                "  \"id\":\""+house.getId()+"\"\n" +
                " },\n" +
                " \"cars\":[{\n" +
                "  \"id\":\""+car.getId()+"\",\n" +
                "  \"model\":\"Benz clo\",\n" +
                "  \"date\":\""+ld.format(DateTimeFormatter.ofPattern("y-MM-dd"))+"\",\n" +
                "  \"time\":\"10:11:11\",\n" +
                "  \"manufacture\":{\n" +
                "   \"id\":\""+carManufacture.getId()+"\"\n" +
                "  }}]\n" +
                "}]}";
        try {
            BindUtils.group.set(ICreate2.class);
            BindUtils.idClass.set(Long.class);
            BindUtils.updating.set(true);
            root = mapper.readValue(data, Company.class);
        }
        finally {
            BindUtils.group.remove();
            BindUtils.idClass.remove();
            BindUtils.updating.remove();
        }
        assertEquals("My company", root.getName());
        assertEquals(city.getId(), root.getCity().getId());
        assertEquals("Mohamad", root.getEmployees().iterator().next().getName());
        assertEquals("Since the group does not have mode, we expect it should not be changed",
                "206", root.getEmployees().iterator().next().getCars().iterator().next().getModel());
        assertEquals(d.toLocalDate(), root.getEmployees().iterator().next().getCars().iterator().next().getDate().toLocalDate());
        assertEquals(t, root.getEmployees().iterator().next().getCars().iterator().next().getTime());
        assertEquals(carManufacture.getId(), root.getEmployees().iterator().next().getCars().iterator().next().getManufacture().getId());
    }

    public interface ICreate1 extends BaseGroups.ICreate {
        @Override
        default Set<IProperty> getProperties() {
            return createProps(Company.class, field("name"),
                    model("employees", field("name"),
                            model("house", field("id")),
                            model("cars", field("name"),
                                    field("date"),
                                    field("time"),
                                    field("model"),
                                    model("manufacture", field("id")))),
                    model("city", field("id")));
        }
    }

    public interface ICreate2 extends BaseGroups.ICreate {
        @Override
        default Set<IProperty> getProperties() {
            return createProps(Company.class, field("id"), field("name"),
                    model("employees", field("id"), field("name"),
                            model("house", field("id")),
                            model("cars", field("id"), field("name"),
                                    field("date"), field("time"),
                                    model("manufacture", field("id")))),
                    model("city", field("id")));
        }
    }

    public interface ICreate3 extends BaseGroups.ICreate {
        @Override
        default Set<IProperty> getProperties() {
            return props(model("company", field("name"),
                    model("employees", field("name"),
                            model("house", field("id")),
                            model("cars", field("name"),
                                    field("date"),
                                    field("time"),
                                    model("manufacture", field("id")))),
                    model("city", field("id"))
            ));
        }
    }
}

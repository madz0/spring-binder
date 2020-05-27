package com.github.madz0.springbinder.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.madz0.springbinder.App;
import com.github.madz0.springbinder.binding.BindingUtils;
import com.github.madz0.springbinder.binding.property.IProperty;
import com.github.madz0.springbinder.config.Config;
import com.github.madz0.springbinder.model.*;
import com.github.madz0.springbinder.repository.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

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

@ComponentScan({"com.github.madz0.springbinder.test"})
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
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    ParkingRepository parkingRepository;
    @Autowired
    EmployeeParkingRepository employeeParkingRepository;

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

        String data = "{\n" +
            "\"name\":\"My company\",\n" +
            "\"city\":{\n" +
            " \"id\":\"" + city.getId() + "\"\n" +
            "},\n" +
            "\"employees\": [{\n" +
            " \"name\":\"Mohamad\",\n" +
            " \"house\":{\n" +
            "  \"id\":\"" + house.getId() + "\"\n" +
            " },\n" +
            " \"cars\":[{\n" +
            "  \"model\":\"Benz clo\",\n" +
            "  \"date\":\"" + ld.format(DateTimeFormatter.ofPattern("y-MM-dd")) + "\",\n" +
            "  \"time\":\"10:11:11\",\n" +
            "  \"manufacture\":{\n" +
            "   \"id\":\"" + carManufacture.getId() + "\"\n" +
            "  }}]\n" +
            "}]}";
        Company root;
        BindingUtils.setGroup(mapper.getDeserializationContext(), ICreate1.class);
        root = mapper.readValue(data, Company.class);
        assertEquals("My company", root.getName());
        assertEquals(city.getId(), root.getCity().getId());
        assertEquals("Mohamad", root.getEmployees().iterator().next().getName());
        assertEquals("Benz clo", root.getEmployees().iterator().next().getCars().iterator().next().getModel());
        assertEquals(d.toLocalDate(),
            root.getEmployees().iterator().next().getCars().iterator().next().getDate().toLocalDate());
        assertEquals(t, root.getEmployees().iterator().next().getCars().iterator().next().getTime());
        assertEquals(carManufacture.getId(),
            root.getEmployees().iterator().next().getCars().iterator().next().getManufacture().getId());
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
        String data = "{\"id\":\"" + root.getId() + "\"," +
            "\"name\":\"My company\",\n" +
            "\"city\":{\n" +
            " \"id\":\"" + city.getId() + "\"\n" +
            "},\n" +
            "\"employees\": [{\n" +
            " \"id\":\"" + employee.getId() + "\",\n" +
            " \"name\":\"Mohamad\",\n" +
            " \"house\":{\n" +
            "  \"id\":\"" + house.getId() + "\"\n" +
            " },\n" +
            " \"cars\":[{\n" +
            "  \"id\":\"" + car.getId() + "\",\n" +
            "  \"model\":\"Benz clo\",\n" +
            "  \"date\":\"" + ld.format(DateTimeFormatter.ofPattern("y-MM-dd")) + "\",\n" +
            "  \"time\":\"10:11:11\",\n" +
            "  \"manufacture\":{\n" +
            "   \"id\":\"" + carManufacture.getId() + "\"\n" +
            "  }}]\n" +
            "}]}";
        BindingUtils.setGroup(mapper.getDeserializationContext(), ICreate2.class);
        BindingUtils.setModifying(mapper.getDeserializationContext(), true);
        root = mapper.readValue(data, Company.class);
        assertEquals("My company", root.getName());
        assertEquals(city.getId(), root.getCity().getId());
        assertEquals("Mohamad", root.getEmployees().iterator().next().getName());
        assertEquals("Since the group does not have mode, we expect it should not be changed",
            "206", root.getEmployees().iterator().next().getCars().iterator().next().getModel());
        assertEquals(d.toLocalDate(),
            root.getEmployees().iterator().next().getCars().iterator().next().getDate().toLocalDate());
        assertEquals(t, root.getEmployees().iterator().next().getCars().iterator().next().getTime());
        assertEquals(carManufacture.getId(),
            root.getEmployees().iterator().next().getCars().iterator().next().getManufacture().getId());
    }

    @Test
    public void embeddedIdPropertyBindTest() throws IOException {
        City city = new City();
        city.setName("Tehran");
        city = cityRepository.save(city);
        House house = new House();
        house.setAddress("Karoon");
        house.setCity(city);
        house = houseRepository.save(house);
        Company company = new Company();
        company.setName("Your Company");
        company.setCity(city);

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

        EmployeeParking employeeParking = new EmployeeParking();
        employeeParking.setId(new EmployeeParkingId(employee.getId(), parking1.getId()));
        employeeParking.setEmployee(employee);
        employeeParking.setParking(parking1);
        employeeParkingRepository.save(employeeParking);

        employee.setEmployeeParkings(new HashSet<>(Arrays.asList(employeeParking)));
        employeeRepository.save(employee);

        String data = "{\n" +
            "\t\"id\":" + employee.getId() + ",\n" +
            "\t\"employeeParkings\":[{\n" +
            "\t\t\"id\":{\n" +
            "\t\t\t\"employeeId\":" + employee.getId() + ",\n" +
            "\t\t\t\"parkingId\":" + parking1.getId() + "\n" +
            "\t\t},\n" +
            "\t\t\"parking\": {\n" +
            "\t\t\t\"id\": " + parking1.getId() + "\n" +
            "\t\t}\n" +
            "\t},{\n" +
            "\t\t\"id\":{\n" +
            "\t\t\t\"employeeId\":" + employee.getId() + ",\n" +
            "\t\t\t\"parkingId\":" + parking2.getId() + "\n" +
            "\t\t},\n" +
            "\t\t\"parking\": {\n" +
            "\t\t\t\"id\": " + parking2.getId() +
            "\t\t}\n" +
            "\t}]\n" +
            "}";
        BindingUtils.setGroup(mapper.getDeserializationContext(), EditEmployee.class);
        BindingUtils.setModifying(mapper.getDeserializationContext(), true);
        Employee root = mapper.readValue(data, Employee.class);
        root = employeeRepository.save(root);
        assertEquals(2, root.getEmployeeParkings().size());
    }

    public interface ICreate1 extends Groups.ICreate {

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

    public interface ICreate2 extends Groups.ICreate {

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

    public interface ICreate3 extends Groups.ICreate {

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

    public interface EditEmployee extends Groups.IEdit {

        @Override
        default Set<IProperty> getProperties() {
            return editProps(Employee.class, model("employeeParkings",
                model("id", field("employeeId"), field("parkingId")),
                model("parking", field("id"))));
        }
    }
}

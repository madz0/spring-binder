package com.github.madzo.springbinder.test;

import com.github.madz0.springbinder.binding.property.IProperty;
import com.github.madz0.springbinder.binding.form.EntityModelObjectConstructor;
import com.github.madz0.springbinder.model.BaseGroups;
import com.github.madzo.springbinder.model.*;
import com.github.madzo.springbinder.repository.CarManufactureRepository;
import com.github.madzo.springbinder.repository.CityRepository;
import com.github.madzo.springbinder.repository.CompanyRepository;
import com.github.madzo.springbinder.repository.HouseRepository;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.extended.DefaultMemberAccess;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class BindingTest extends BaseTest {

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

    @Test
    public void bindEntityWithGroups() throws OgnlException {
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
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        context.extend();
        context.setObjectConstructor(new EntityModelObjectConstructor(em, Long.class,
                null, ICreate1.class));
        Company root = new Company();
        List<String> bindingList = new ArrayList<>();
        bindingList.add("name=My company");
        bindingList.add("city.id=" + city.getId());
        bindingList.add("employees[0].name=Mohamad");
        bindingList.add("employees[0].house.id=" + house.getId());
        bindingList.add("employees[0].cars[0].model=Benz clo");
        bindingList.add("employees[0].cars[0].date=" + ld.format(DateTimeFormatter.ofPattern("y-M-d")));
        bindingList.add("employees[0].cars[0].time=10:11:11");
        bindingList.add("employees[0].cars[0].manufacture.id=" + carManufacture.getId());

        Ognl.getValue(bindingList, context, root);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("Y-M-D");

        assertEquals("My company", root.getName());
        assertEquals(city.getId(), root.getCity().getId());
        assertEquals("Mohamad", root.getEmployees().iterator().next().getName());
        assertEquals("Benz clo", root.getEmployees().iterator().next().getCars().iterator().next().getModel());
        assertEquals(d, root.getEmployees().iterator().next().getCars().iterator().next().getDate());
        assertEquals(t, root.getEmployees().iterator().next().getCars().iterator().next().getTime());
        assertEquals(carManufacture.getId(), root.getEmployees().iterator().next().getCars().iterator().next().getManufacture().getId());
    }

    @Test
    public void bindEntityWithGroups_NoCarModel() throws OgnlException {
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
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        context.extend();
        context.setObjectConstructor(new EntityModelObjectConstructor(em, Long.class,
                null, ICreate2.class));
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

        List<String> bindingList = new ArrayList<>();
        bindingList.add("name=My company");
        bindingList.add("city.id=" + city.getId());
        bindingList.add("employees[0].id="+employee.getId());
        bindingList.add("employees[0].name=Mohamad");
        bindingList.add("employees[0].house.id=" + house.getId());
        bindingList.add("employees[0].cars[0].id="+car.getId());
        bindingList.add("employees[0].cars[0].model=Benz clo");
        bindingList.add("employees[0].cars[0].date=" + ld.format(DateTimeFormatter.ofPattern("y-M-d")));
        bindingList.add("employees[0].cars[0].time=10:11:11");
        bindingList.add("employees[0].cars[0].manufacture.id=" + carManufacture.getId());

        Ognl.getValue(bindingList, context, root);

        assertEquals("My company", root.getName());
        assertEquals(city.getId(), root.getCity().getId());
        assertEquals("Mohamad", root.getEmployees().iterator().next().getName());
        assertEquals("Since the group does not have mode, we expect it sould not be changed",
                "206", root.getEmployees().iterator().next().getCars().iterator().next().getModel());
        assertEquals(d, root.getEmployees().iterator().next().getCars().iterator().next().getDate());
        assertEquals(t, root.getEmployees().iterator().next().getCars().iterator().next().getTime());
        assertEquals(carManufacture.getId(), root.getEmployees().iterator().next().getCars().iterator().next().getManufacture().getId());
    }

    @Test
    public void bindDtoWithEntityWithGroups_NoDtoName_NoCarModel() throws OgnlException {
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
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        context.extend();
        context.setObjectConstructor(new EntityModelObjectConstructor(em, Long.class,
                null, ICreate3.class));

        Company company = new Company();
        company.setName("Your Company");
        company.setCity(city);

        Employee employee = new Employee();
        employee.setCompany(company);
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
        company.setEmployees(new HashSet<>(Arrays.asList(employee)));

        company = companyRepository.save(company);

        DtoContainsModel root = new DtoContainsModel();
        root.setCompany(company);
        root.setName("dto");

        List<String> bindingList = new ArrayList<>();
        bindingList.add("name=My company");
        bindingList.add("company.city.id=" + city.getId());
        bindingList.add("company.employees[0].id="+employee.getId());
        bindingList.add("company.employees[0].name=Mohamad");
        bindingList.add("company.employees[0].house.id=" + house.getId());
        bindingList.add("company.employees[0].cars[0].id="+car.getId());
        bindingList.add("company.employees[0].cars[0].model=Benz clo");
        bindingList.add("company.employees[0].cars[0].date=" + ld.format(DateTimeFormatter.ofPattern("y-M-d")));
        bindingList.add("company.employees[0].cars[0].time=10:11:11");
        bindingList.add("company.employees[0].cars[0].manufacture.id=" + carManufacture.getId());

        Ognl.getValue(bindingList, context, root);

        assertEquals("dto", root.getName());
        assertEquals(city.getId(), root.getCompany().getCity().getId());
        assertEquals("Mohamad", root.getCompany().getEmployees().iterator().next().getName());
        assertEquals("Since the group does not have mode, we expect that it sould not be changed",
                "206", root.getCompany().getEmployees().iterator().next().getCars().iterator().next().getModel());
        assertEquals(d, root.getCompany().getEmployees().iterator().next().getCars().iterator().next().getDate());
        assertEquals(t, root.getCompany().getEmployees().iterator().next().getCars().iterator().next().getTime());
        assertEquals(carManufacture.getId(), root.getCompany().getEmployees().iterator().next().getCars().iterator().next().getManufacture().getId());
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
            return createProps(Company.class, field("name"),
                    model("employees", field("name"),
                            model("house", field("id")),
                            model("cars", field("name"),
                                    field("date"),
                                    field("time"),
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

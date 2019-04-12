package com.github.madz0.springbinder.test;

import com.github.madz0.springbinder.binding.IdClassMapper;
import com.github.madz0.springbinder.binding.form.EntityModelObjectConstructor;
import com.github.madz0.springbinder.binding.property.IProperty;
import com.github.madz0.springbinder.model.*;
import com.github.madz0.springbinder.repository.*;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.extended.DefaultMemberAccess;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FormBindingTest extends BaseTest {

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
    @Autowired(required = false)
    IdClassMapper idClassMapper;
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    ParkingRepository parkingRepository;
    @Autowired
    EmployeeParkingRepository employeeParkingRepository;

    @Test
    public void bindEntityWithGroupsTest() throws OgnlException {
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
        context.setObjectConstructor(new EntityModelObjectConstructor(em,
                null, ICreate1.class, idClassMapper, false));
        Company root = new Company();
        List<Map.Entry<String, Object>> bindingList = new ArrayList<>();
        bindingList.add(new AbstractMap.SimpleEntry<>("name", "My company"));
        bindingList.add(new AbstractMap.SimpleEntry<>("city.id", city.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].name", "Mohamad"));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].house.id", house.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].cars[0].model", "Benz clo"));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].cars[0].date", ld.format(DateTimeFormatter.ofPattern("y-M-d"))));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].cars[0].time", "10:11:11"));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].cars[0].manufacture.id", carManufacture.getId()));

        Ognl.getValue(bindingList, context, root);

        assertEquals("My company", root.getName());
        assertEquals(city.getId(), root.getCity().getId());
        assertEquals("Mohamad", root.getEmployees().iterator().next().getName());
        assertEquals("Benz clo", root.getEmployees().iterator().next().getCars().iterator().next().getModel());
        assertEquals(d, root.getEmployees().iterator().next().getCars().iterator().next().getDate());
        assertEquals(t, root.getEmployees().iterator().next().getCars().iterator().next().getTime());
        assertEquals(carManufacture.getId(), root.getEmployees().iterator().next().getCars().iterator().next().getManufacture().getId());
    }

    @Test
    public void bindEntityWithGroups_NoCarModelTest() throws OgnlException {
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
        context.setObjectConstructor(new EntityModelObjectConstructor(em,
                null, ICreate2.class, idClassMapper, false));
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

        List<Map.Entry<String, Object>> bindingList = new ArrayList<>();
        bindingList.add(new AbstractMap.SimpleEntry<>("name", "My company"));
        bindingList.add(new AbstractMap.SimpleEntry<>("city.id", city.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].id", employee.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].name", "Mohamad"));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].house.id", house.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].cars[0].id", car.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].cars[0].model", "Benz clo"));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].cars[0].date", ld.format(DateTimeFormatter.ofPattern("y-M-d"))));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].cars[0].time", "10:11:11"));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].cars[0].manufacture.id", carManufacture.getId()));

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
    public void bindDtoWithEntityWithGroups_NoDtoName_NoCarModelTest() throws OgnlException {
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
        context.setObjectConstructor(new EntityModelObjectConstructor(em,
                null, ICreate3.class, idClassMapper, false));

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

        List<Map.Entry<String, Object>> bindingList = new ArrayList<>();
        bindingList.add(new AbstractMap.SimpleEntry<>("name", "My company"));
        bindingList.add(new AbstractMap.SimpleEntry<>("company.city.id", city.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>("company.employees[0].id", employee.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>("company.employees[0].name", "Mohamad"));
        bindingList.add(new AbstractMap.SimpleEntry<>("company.employees[0].house.id", house.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>("company.employees[0].cars[0].id", car.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>("company.employees[0].cars[0].model", "Benz clo"));
        bindingList.add(new AbstractMap.SimpleEntry<>("company.employees[0].cars[0].date", ld.format(DateTimeFormatter.ofPattern("y-M-d"))));
        bindingList.add(new AbstractMap.SimpleEntry<>("company.employees[0].cars[0].time", "10:11:11"));
        bindingList.add(new AbstractMap.SimpleEntry<>("company.employees[0].cars[0].manufacture.id", carManufacture.getId()));

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

    @Test
    public void malformedRequestTest() throws OgnlException {
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
        context.setObjectConstructor(new EntityModelObjectConstructor(em,
                null, ICreate1.class, idClassMapper, false));
        Company root = new Company();
        List<Map.Entry<String, Object>> bindingList = new ArrayList<>();
        bindingList.add(new AbstractMap.SimpleEntry<>("gk6q${“zkz”.toString().replace(\"k\", \"x\")}doap2", null));
        bindingList.add(new AbstractMap.SimpleEntry<>("city.id", city.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].name", "Mohamad"));
        bindingList.add(new AbstractMap.SimpleEntry<>(".employees[0].house.id", house.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].cars[0].model", "Benz clo"));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0]..cars[0].date", ld.format(DateTimeFormatter.ofPattern("y-M-d"))));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].cars[0].time", "10:11:11"));
        bindingList.add(new AbstractMap.SimpleEntry<>("employees[0].cars[0].manufacture.id", carManufacture.getId()));

        try {
            Ognl.getValue(bindingList, context, root);
            throw new AssertionError();
        } catch (Exception e) {
            assertTrue(e instanceof OgnlException);
        }
    }

    @Test
    public void propertyNameInValueDuplicateFixTest() throws OgnlException {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        context.extend();
        context.setObjectConstructor(new EntityModelObjectConstructor(em,
                null, ICreate1.class, idClassMapper, false));
        Company root = new Company();

        List<Map.Entry<String, Object>> bindingList = new ArrayList<>();
        bindingList.add(new AbstractMap.SimpleEntry<>("name", "name"));
        Ognl.getValue(bindingList, context, root);

        assertEquals("name", root.getName());
    }

    @Test
    public void emptyPropertyNameTest() throws OgnlException {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        context.extend();
        context.setObjectConstructor(new EntityModelObjectConstructor(em,
                null, ICreate1.class, idClassMapper, false));
        Company root = new Company();
        root.setName("hi");
        List<Map.Entry<String, Object>> bindingList = new ArrayList<>();
        bindingList.add(new AbstractMap.SimpleEntry<>("name", null));
        Ognl.getValue(bindingList, context, root);

        assertEquals(null, root.getName());
    }

    @Test
    public void embeddedIdPropertyBindTest() throws OgnlException {
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

        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        context.extend();
        context.setObjectConstructor(new EntityModelObjectConstructor(em,
                null, EditEmployee.class, idClassMapper, false));
        List<Map.Entry<String, Object>> bindingList = new ArrayList<>();
        bindingList.add(new AbstractMap.SimpleEntry<>("id", employee.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>(String.format("employeeParkings[%d].id.employeeId", 0), employee.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>(String.format("employeeParkings[%d].id.parkingId", 0), parking1.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>(String.format("employeeParkings[%d].parking.id", 0), parking1.getId()));
        //
        bindingList.add(new AbstractMap.SimpleEntry<>(String.format("employeeParkings[%d].id.employeeId", 1), employee.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>(String.format("employeeParkings[%d].id.parkingId", 1), parking2.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>(String.format("employeeParkings[%d].parking.id", 1), parking2.getId()));

        Employee root = Ognl.getValue(bindingList, context, Employee.class);
        assertEquals(2, root.getEmployeeParkings().size());
        root = employeeRepository.save(root);
        assertEquals(2, root.getEmployeeParkings().size());
    }

    @Test
    public void clearingOneToManyTest() throws OgnlException {
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

        employee.setEmployeeParkings(new HashSet<>(Arrays.asList(employeeParking)));
        employeeRepository.save(employee);
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        context.extend();
        context.setObjectConstructor(new EntityModelObjectConstructor(em,
                null, EditEmployee.class, idClassMapper, false));
        List<Map.Entry<String, Object>> bindingList = new ArrayList<>();
        bindingList.add(new AbstractMap.SimpleEntry<>("id", employee.getId()));
        bindingList.add(new AbstractMap.SimpleEntry<>(String.format("employeeParkings[%d]", 0), ""));
        Employee root = Ognl.getValue(bindingList, context, Employee.class);
        assertEquals(0, root.getEmployeeParkings().size());
    }

    @Test
    public void bindingAsDtoTest() throws OgnlException {
        City city = new City();
        city.setName("Tehran");
        city = cityRepository.save(city);
        Company company = new Company();
        company.setName("Your Company");
        company.setCity(city);

        OgnlContext context = (OgnlContext) Ognl.createDefaultContext(null, new DefaultMemberAccess(false));
        context.extend();
        context.setObjectConstructor(new EntityModelObjectConstructor(em,
                null, BindAsDtoTestGroup.class, idClassMapper, true));
        List<Map.Entry<String, Object>> bindingList = new ArrayList<>();
        bindingList.add(new AbstractMap.SimpleEntry<>(String.format("employees[%d].house.address", 0), "testtest"));
        bindingList.add(new AbstractMap.SimpleEntry<>(String.format("employees[%d].house.city.name", 0), "testCity"));
        bindingList.add(new AbstractMap.SimpleEntry<>("name", "test"));
        bindingList.add(new AbstractMap.SimpleEntry<>(String.format("employees[%d].house.address", 1), "testtest2"));
        Company root = Ognl.getValue(bindingList, context, Company.class);
        assertEquals(2, root.getEmployees().size());
        assertEquals("test", root.getName());
        List<String> houseAddressList = root.getEmployees().stream().map(Employee::getHouse).map(House::getAddress).collect(Collectors.toList());
        assertTrue(houseAddressList.contains("testtest"));
        assertTrue(houseAddressList.contains("testtest2"));
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

    public interface EditEmployee extends BaseGroups.IEdit {
        @Override
        default Set<IProperty> getProperties() {
            return editProps(Employee.class, model("employeeParkings",
                    model("id", field("employeeId"), field("parkingId")),
                    model("parking", field("id"))));
        }
    }

    public interface BindAsDtoTestGroup extends BaseGroups.ICreate {
        @Override
        default Set<IProperty> getProperties() {
            return createProps(Company.class, field("name"),
                    model("employees", field("name"),
                            model("house", field("id"),
                                    field("address"),
                                    model("city", field("name"))),
                            model("cars", field("name"),
                                    field("date"),
                                    field("time"),
                                    field("model"),
                                    model("manufacture", field("id")))),
                    model("city", field("id")));
        }
    }
}

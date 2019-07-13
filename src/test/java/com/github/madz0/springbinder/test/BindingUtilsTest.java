package com.github.madz0.springbinder.test;

import com.github.madz0.springbinder.model.Company;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import static com.github.madz0.springbinder.binding.BindingUtils.*;
import static org.junit.Assert.assertNotNull;

public class BindingUtilsTest extends BaseTest {

    @Autowired
    EntityManager em;

    @Test
    public void getPathOfOneToManyDotManyToOneDotIdTest() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Company> query = cb.createQuery(Company.class);
        Root<Company> root = query.from(Company.class);
        assertNotNull(findPath(root, "employees.house.id"));
    }

    @Test
    public void getPathIdTest() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Company> query = cb.createQuery(Company.class);
        Root<Company> root = query.from(Company.class);
        assertNotNull(findPath(root, "id"));
    }

    @Test
    public void getPathOfManyToOneIdTest() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Company> query = cb.createQuery(Company.class);
        Root<Company> root = query.from(Company.class);
        assertNotNull(findPath(root, "city.id"));
    }
}

package com.github.madz0.springbinder.validation.validator;

import com.github.madz0.springbinder.binding.rest.serialize.ContextAwareObjectMapper;
import com.github.madz0.springbinder.model.IdModel;
import com.github.madz0.springbinder.repository.BaseRepository;
import com.github.madz0.springbinder.validation.constraint.Unique;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

import static com.github.madz0.springbinder.binding.BindingUtils.findPath;

public class UniqueValidator implements ConstraintValidator<Unique, Object> {
    final static public String message = "validation.error.unique";

    private Unique unique;

    @Override
    public void initialize(Unique constraintAnnotation) {
        this.unique =constraintAnnotation;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        ContextAwareObjectMapper.getBean(EntityManager.class).unwrap(Session.class).setHibernateFlushMode(FlushMode.MANUAL);
        try{
            BeanWrapper beanWrapper = new BeanWrapperImpl(object);
            BaseRepository repository = ContextAwareObjectMapper.getBean(unique.repositoryClass());
            long count = repository.count((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                Object idValue = beanWrapper.getPropertyValue(IdModel.ID_FIELD);
                if(idValue == null){
                    predicates.add(cb.isNotNull(root.get(IdModel.ID_FIELD)));
                }else{
                    predicates.add(cb.notEqual(root.get(IdModel.ID_FIELD), idValue));
                }
                Object value = beanWrapper.getPropertyValue(unique.field());
                predicates.add(cb.equal(root.get(unique.field()), value));
                for (String conditionField : unique.conditionFields()) {
                    Object conditionValue = beanWrapper.getPropertyValue(conditionField);
                    if(conditionValue == null){
                        predicates.add(cb.isNull(findPath(root, conditionField)));
                    }else{
                        predicates.add(cb.equal(findPath(root, conditionField), conditionValue));
                    }
                }
                return predicates.stream().reduce(cb::and).orElse(null);
            });
            return count == 0;
        }finally {
            ContextAwareObjectMapper.getBean(EntityManager.class).unwrap(Session.class).setHibernateFlushMode(FlushMode.AUTO);
        }
    }
}

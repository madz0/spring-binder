package com.github.madz0.springbinder.model;

import com.github.madz0.springbinder.binding.BindingUtils;
import com.github.madz0.springbinder.binding.MethodUtils;
import com.github.madz0.springbinder.binding.property.*;

import javax.persistence.metamodel.Attribute;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Groups {

    public interface IGroup {
        default Set<IProperty> getProperties() {
            return props();
        }

        default Set<IProperty> props(IProperty... props) {
            return new HashSet<>(Arrays.asList(props));
        }

        @SuppressWarnings("unchecked")
        default Set<IProperty> allProps(Class<? extends IModel> clazz) {
            return Arrays.stream(BindingUtils.getBeanWrapper(clazz).getPropertyDescriptors())
                    .filter(x ->
                            !Objects.equals(x.getName(), "class") &&
                                    !Objects.equals(x.getName(), "recordType")
                    )
                    .map(x -> {
                        if (Collection.class.isAssignableFrom(x.getPropertyType())) {
                            Class<?> genericClass = (Class) ((ParameterizedType) x.getReadMethod().getGenericReturnType()).getActualTypeArguments()[0];
                            if (IModel.class.isAssignableFrom(genericClass)) {
                                Class<? extends IModel> baseModelGeneric = (Class<? extends IModel>) genericClass;
                                return ComputedModelProperty.of(clazz, x.getReadMethod().getName(),
                                        props(field(IdModel.ID_FIELD), computed(baseModelGeneric, IModel::getPresentation)));
                            }
                        }
                        if (IModel.class.isAssignableFrom(x.getPropertyType())) {
                            Class<? extends IModel> genericClass = (Class<? extends IModel>) x.getPropertyType();
                            return ComputedModelProperty.of(clazz, x.getReadMethod().getName(),
                                    props(field(IdModel.ID_FIELD), computed(genericClass, IModel::getPresentation)));
                        }
                        return ComputedProperty.of(clazz, x.getReadMethod().getName());
                    })
                    .collect(Collectors.toSet());
        }

        default Set<IProperty> allPropsExcept(Class<? extends IModel> clazz, IProperty... ignore) {
            return allPropsExcept(clazz, props(ignore));
        }

        default Set<IProperty> allPropsExcept(Class<? extends IModel> clazz, Set<IProperty> ignore) {
            return allProps(clazz).stream()
                    .filter(x -> ignore.stream().noneMatch(y -> Objects.equals(x, y)))
                    .collect(Collectors.toSet());
        }

        default Set<IProperty> addProps(Class<? extends IGroup> clazz, IProperty... props) {
            Set<IProperty> set = new HashSet<>(BindingUtils.getPropertiesFromGroup(clazz));
            set.addAll(Arrays.asList(props));
            return set;
        }

        default Set<IProperty> removeProps(Class<? extends IGroup> clazz, IProperty... props) {
            Set<IProperty> set = new HashSet<>(BindingUtils.getPropertiesFromGroup(clazz));
            set.removeAll(Arrays.asList(props));
            return set;
        }

        default IProperty field(Attribute attribute) {
            return FieldProperty.of(attribute);
        }

        default IProperty field(String name) {
            return FieldProperty.of(name);
        }

        default IProperty model(Attribute attribute, IProperty... fields) {
            return ModelProperty.of(attribute, props(fields));
        }

        default IProperty model(String name, IProperty... fields) {
            return ModelProperty.of(name, props(fields));
        }

        default <T> IProperty computed(Class<T> clazz, Function<T, ?> mapFunc) {
            return ComputedProperty.of(clazz, MethodUtils.getMethodName(clazz, mapFunc));
        }

        default <T> IProperty computedModel(Class<T> clazz, Function<T, ?> mapFunc, IProperty... fields) {
            return ComputedModelProperty.of(clazz, MethodUtils.getMethodName(clazz, mapFunc), props(fields));
        }
    }

    public interface IList extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IdModel.ID_FIELD));
        }

        default Set<IProperty> listProps(Class<? extends IModel> clazz, IProperty... props) {
            return Stream.concat(Stream.of(field(IdModel.ID_FIELD), computed(clazz, IModel::getPresentation),
                    computed(clazz, AccessModel::getAccess)), Arrays.stream(props)).collect(Collectors.toSet());
        }

        default Set<IProperty> allListProps(Class<? extends IModel> clazz) {
            return allListPropsExcept(clazz);
        }

        default Set<IProperty> allListPropsExcept(Class<? extends IModel> clazz, IProperty... ignore) {
            return allPropsExcept(clazz, Stream.concat(Stream.of(field(IModel.VERSION_FIELD), field(IModel.CREATED_BY_FIELD),
                    field(IModel.MODIFIED_BY_FIELD), field(IModel.CREATED_DATE_FIELD), field(IModel.MODIFIED_DATE_FIELD)),
                    Arrays.stream(ignore)).collect(Collectors.toSet()));
        }
    }

    public interface ICreate extends IGroup {
        default Set<IProperty> createProps(Class<? extends IModel> clazz, IProperty... props) {
            return props(props);
        }

        default Set<IProperty> allCreateProps(Class<? extends IModel> clazz) {
            return allCreatePropsExcept(clazz);
        }

        default Set<IProperty> allCreatePropsExcept(Class<? extends IModel> clazz, IProperty... ignore) {
            return allPropsExcept(clazz,
                    Stream.concat(
                            Stream.of(
                                    field(IdModel.ID_FIELD),
                                    field(IModel.VERSION_FIELD),
                                    computed(clazz, AccessModel::getAccess),
                                    field(IModel.CREATED_BY_FIELD),
                                    computed(clazz, IModel::getPresentation),
                                    field(IModel.MODIFIED_BY_FIELD),
                                    field(IModel.CREATED_DATE_FIELD),
                                    field(IModel.MODIFIED_DATE_FIELD)),
                            Arrays.stream(ignore)).collect(Collectors.toSet()
                    )
            );
        }
    }

    public interface IEdit extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IdModel.ID_FIELD), field(IModel.VERSION_FIELD));
        }

        default Set<IProperty> editProps(Class<? extends IModel> clazz, IProperty... props) {
            return Stream.concat(Stream.of(
                    field(IdModel.ID_FIELD),
                    field(IModel.VERSION_FIELD),
                    computed(clazz, IModel::getPresentation),
                    computed(clazz, AccessModel::getAccess)
            ),
                    Arrays.stream(props)).collect(Collectors.toSet());
        }

        default Set<IProperty> allEditProps(Class<? extends IModel> clazz) {
            return allEditPropsExcept(clazz);
        }

        default Set<IProperty> allEditPropsExcept(Class<? extends IModel> clazz, IProperty... ignore) {
            return allPropsExcept(clazz, Stream.concat(
                    Stream.of(
                            field(IModel.CREATED_BY_FIELD),
                            field(IModel.MODIFIED_BY_FIELD),
                            field(IModel.CREATED_DATE_FIELD),
                            field(IModel.MODIFIED_DATE_FIELD)),
                    Arrays.stream(ignore)).collect(Collectors.toSet()));
        }
    }

    public interface IView extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IdModel.ID_FIELD), field(IModel.CREATED_DATE_FIELD),
                    field(IModel.MODIFIED_DATE_FIELD), field(IModel.ACCESS_FIELD),
                    model(IModel.CREATED_BY_FIELD, field(IModel.PRESENTATION_FIELD)),
                    model(IModel.MODIFIED_BY_FIELD, field(IModel.PRESENTATION_FIELD)));
        }

        default Set<IProperty> viewProps(Class<? extends IModel> clazz, IProperty... props) {
            return Stream.concat(
                    Stream.of(
                            field(IdModel.ID_FIELD),
                            field(IModel.CREATED_DATE_FIELD),
                            field(IModel.MODIFIED_DATE_FIELD),
                            computed(clazz, IModel::getPresentation),
                            computed(clazz, AccessModel::getAccess),
                            model(IModel.CREATED_BY_FIELD, field(IModel.PRESENTATION_FIELD)),
                            model(IModel.MODIFIED_BY_FIELD, field(IModel.PRESENTATION_FIELD))
                    ),
                    Arrays.stream(props)).collect(Collectors.toSet()
            );
        }

        default Set<IProperty> allViewProps(Class<? extends IModel> clazz) {
            return allViewPropsExcept(clazz);
        }

        default Set<IProperty> allViewPropsExcept(Class<? extends IModel> clazz, IProperty... ignore) {
            return allPropsExcept(clazz, props(ignore));
        }
    }

    public interface IDelete extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IdModel.ID_FIELD));
        }
    }

    public interface IDto extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IdModel.ID_FIELD), field(IModel.RECORD_TYPE_FIELD));
        }
    }
}

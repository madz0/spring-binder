package com.github.madz0.springbinder.model;

import com.github.madz0.springbinder.binding.BindUtils;
import com.github.madz0.springbinder.binding.MethodUtils;
import com.github.madz0.springbinder.binding.property.*;

import javax.persistence.metamodel.Attribute;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BaseGroups {

    public interface IGroup {
        default Set<IProperty> getProperties() {
            return props();
        }

        default Set<IProperty> props(IProperty... props) {
            return new HashSet<>(Arrays.asList(props));
        }

        @SuppressWarnings("unchecked")
        default Set<IProperty> allProps(Class<? extends IBaseModel> clazz) {
            return Arrays.stream(BindUtils.getBeanWrapper(clazz).getPropertyDescriptors())
                    .filter(x ->
                            !Objects.equals(x.getName(), "class") &&
                                    !Objects.equals(x.getName(), "recordType")
                    )
                    .map(x -> {
                        if (Collection.class.isAssignableFrom(x.getPropertyType())) {
                            Class<?> genericClass = (Class) ((ParameterizedType) x.getReadMethod().getGenericReturnType()).getActualTypeArguments()[0];
                            if (IBaseModel.class.isAssignableFrom(genericClass)) {
                                Class<? extends IBaseModel> baseModelGeneric = (Class<? extends IBaseModel>) genericClass;
                                return ComputedModelProperty.of(clazz, x.getReadMethod().getName(),
                                        props(field(IBaseModelId.ID_FIELD), computed(baseModelGeneric, IBaseModel::getPresentation)));
                            }
                        }
                        if (IBaseModel.class.isAssignableFrom(x.getPropertyType())) {
                            Class<? extends IBaseModel> genericClass = (Class<? extends IBaseModel>) x.getPropertyType();
                            return ComputedModelProperty.of(clazz, x.getReadMethod().getName(),
                                    props(field(IBaseModelId.ID_FIELD), computed(genericClass, IBaseModel::getPresentation)));
                        }
                        return ComputedProperty.of(clazz, x.getReadMethod().getName());
                    })
                    .collect(Collectors.toSet());
        }

        default Set<IProperty> allPropsExcept(Class<? extends IBaseModel> clazz, IProperty... ignore) {
            return allPropsExcept(clazz, props(ignore));
        }

        default Set<IProperty> allPropsExcept(Class<? extends IBaseModel> clazz, Set<IProperty> ignore) {
            return allProps(clazz).stream()
                    .filter(x -> ignore.stream().noneMatch(y -> Objects.equals(x, y)))
                    .collect(Collectors.toSet());
        }

        default Set<IProperty> addProps(Class<? extends IGroup> clazz, IProperty... props) {
            Set<IProperty> set = new HashSet<>(BindUtils.getPropertiesFromGroup(clazz));
            set.addAll(Arrays.asList(props));
            return set;
        }

        default Set<IProperty> removeProps(Class<? extends IGroup> clazz, IProperty... props) {
            Set<IProperty> set = new HashSet<>(BindUtils.getPropertiesFromGroup(clazz));
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
            return props(field(IBaseModelId.ID_FIELD));
        }

        default Set<IProperty> listProps(Class<? extends IBaseModel> clazz, IProperty... props) {
            return Stream.concat(Stream.of(field(IBaseModelId.ID_FIELD), computed(clazz, IBaseModel::getPresentation),
                    computed(clazz, IBaseModelAccess::getAccess)), Arrays.stream(props)).collect(Collectors.toSet());
        }

        default Set<IProperty> allListProps(Class<? extends IBaseModel> clazz) {
            return allListPropsExcept(clazz);
        }

        default Set<IProperty> allListPropsExcept(Class<? extends IBaseModel> clazz, IProperty... ignore) {
            return allPropsExcept(clazz, Stream.concat(Stream.of(field(IBaseModel.VERSION_FIELD), field(IBaseModel.CREATED_BY_FIELD),
                    field(IBaseModel.MODIFIED_BY_FIELD), field(IBaseModel.CREATED_DATE_FIELD), field(IBaseModel.MODIFIED_DATE_FIELD)),
                    Arrays.stream(ignore)).collect(Collectors.toSet()));
        }
    }

    public interface ICreate extends IGroup {
        default Set<IProperty> createProps(Class<? extends IBaseModel> clazz, IProperty... props) {
            return props(props);
        }

        default Set<IProperty> allCreateProps(Class<? extends IBaseModel> clazz) {
            return allCreatePropsExcept(clazz);
        }

        default Set<IProperty> allCreatePropsExcept(Class<? extends IBaseModel> clazz, IProperty... ignore) {
            return allPropsExcept(clazz,
                    Stream.concat(
                            Stream.of(
                                    field(IBaseModelId.ID_FIELD),
                                    field(IBaseModel.VERSION_FIELD),
                                    computed(clazz, IBaseModelAccess::getAccess),
                                    field(IBaseModel.CREATED_BY_FIELD),
                                    computed(clazz, IBaseModel::getPresentation),
                                    field(IBaseModel.MODIFIED_BY_FIELD),
                                    field(IBaseModel.CREATED_DATE_FIELD),
                                    field(IBaseModel.MODIFIED_DATE_FIELD)),
                            Arrays.stream(ignore)).collect(Collectors.toSet()
                    )
            );
        }
    }

    public interface IEdit extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IBaseModelId.ID_FIELD), field(IBaseModel.VERSION_FIELD));
        }

        default Set<IProperty> editProps(Class<? extends IBaseModel> clazz, IProperty... props) {
            return Stream.concat(Stream.of(
                    field(IBaseModelId.ID_FIELD),
                    field(IBaseModel.VERSION_FIELD),
                    computed(clazz, IBaseModel::getPresentation),
                    computed(clazz, IBaseModelAccess::getAccess)
            ),
                    Arrays.stream(props)).collect(Collectors.toSet());
        }

        default Set<IProperty> allEditProps(Class<? extends IBaseModel> clazz) {
            return allEditPropsExcept(clazz);
        }

        default Set<IProperty> allEditPropsExcept(Class<? extends IBaseModel> clazz, IProperty... ignore) {
            return allPropsExcept(clazz, Stream.concat(
                    Stream.of(
                            field(IBaseModel.CREATED_BY_FIELD),
                            field(IBaseModel.MODIFIED_BY_FIELD),
                            field(IBaseModel.CREATED_DATE_FIELD),
                            field(IBaseModel.MODIFIED_DATE_FIELD)),
                    Arrays.stream(ignore)).collect(Collectors.toSet()));
        }
    }

    public interface IView extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IBaseModelId.ID_FIELD), field(IBaseModel.CREATED_DATE_FIELD),
                    field(IBaseModel.MODIFIED_DATE_FIELD), field(IBaseModel.ACCESS_FIELD),
                    model(IBaseModel.CREATED_BY_FIELD, field(IBaseModel.PRESENTATION_FIELD)),
                    model(IBaseModel.MODIFIED_BY_FIELD, field(IBaseModel.PRESENTATION_FIELD)));
        }

        default Set<IProperty> viewProps(Class<? extends IBaseModel> clazz, IProperty... props) {
            return Stream.concat(
                    Stream.of(
                            field(IBaseModelId.ID_FIELD),
                            field(IBaseModel.CREATED_DATE_FIELD),
                            field(IBaseModel.MODIFIED_DATE_FIELD),
                            computed(clazz, IBaseModel::getPresentation),
                            computed(clazz, IBaseModelAccess::getAccess),
                            model(IBaseModel.CREATED_BY_FIELD, field(IBaseModel.PRESENTATION_FIELD)),
                            model(IBaseModel.MODIFIED_BY_FIELD, field(IBaseModel.PRESENTATION_FIELD))
                    ),
                    Arrays.stream(props)).collect(Collectors.toSet()
            );
        }

        default Set<IProperty> allViewProps(Class<? extends IBaseModel> clazz) {
            return allViewPropsExcept(clazz);
        }

        default Set<IProperty> allViewPropsExcept(Class<? extends IBaseModel> clazz, IProperty... ignore) {
            return allPropsExcept(clazz, props(ignore));
        }
    }

    public interface IDelete extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IBaseModelId.ID_FIELD));
        }
    }

    public interface IDto extends IGroup {
        @Override
        default Set<IProperty> getProperties() {
            return props(field(IBaseModelId.ID_FIELD), field(IBaseModel.RECORD_TYPE_FIELD));
        }
    }
}
